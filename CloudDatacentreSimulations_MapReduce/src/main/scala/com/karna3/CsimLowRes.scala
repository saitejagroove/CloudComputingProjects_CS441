package com.karna3

import java.io.FileWriter
import java.text.DecimalFormat
import java.util.Calendar

import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim._
import org.cloudbus.cloudsim.core.CloudSim
import org.cloudbus.cloudsim.network.datacenter.NetworkDatacenter
import org.cloudbus.cloudsim.provisioners.{BwProvisionerSimple, PeProvisionerSimple, RamProvisionerSimple}
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.javaapi.CollectionConverters.{asJava, asScala}

/**
 * This is the low resource simulation with
 *
 */

object CsimLowRes {

  val SIM = "simulation2low"

  //Initialize Config and Logger objects from 3rd party libraries
  val conf: Config = ConfigFactory.load(SIM+".conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {

    //Save start simulation time (used to compute simulation total time)
    val startTime = System.currentTimeMillis

    LOG.debug("Starting simulation 1..")

    /*
    * Load general config options
     */
    val num_user = conf.getInt(SIM+".num_user")
    val calendar = Calendar.getInstance
    val trace_flag = conf.getBoolean("trace_flag")
    val num_vms = conf.getInt(SIM+".num_vms")
    val num_dcs = conf.getInt(SIM+".num_dcs")
    val num_mappers = conf.getInt(SIM+".num_mappers")
    val num_reducers = conf.getInt(SIM+".num_reducers")

    //Init cloudsim framework
    CloudSim.init(num_user, calendar, trace_flag)
    LOG.info("Cloudsim framework initialized")

    //Recursive call for datacenter generation
    val datacenters = createDatacenters(num_dcs)

    LOG.debug("N. Hosts: "+datacenters(0).getHostList.size())
    LOG.debug("N. Hosts: "+datacenters(1).getHostList.size())

    //Broker initialization
    val broker = createBroker()
    val brokerId = broker.getId

    //Recursive call for VM creation
    val vmlist = createVM(brokerId, num_vms)

    //Set list of available VMs on broker
    broker.submitVmList(asJava[Vm](vmlist))

    /*
    * This is a simulation specific thing.
    * Two different types of Cloudlets are creates, Mappers and Reducers.
    * Please see createCloudlet method for additional info.
     */
    val mappers = createCloudlet(brokerId, num_mappers, 0, CustomCloudlet.Type.MAPPER)
    val reducers = createCloudlet(brokerId, num_reducers, num_mappers, CustomCloudlet.Type.REDUCER)

    /*
     * This is a simulation specific thing.
     * Two different types of Cloudlets can be submitted to the broker. Those are handled completely differently.
     * Please see CustomBroker.processEvent for additional info.
     */
    broker.submitMapperList(asJava[CustomCloudlet](mappers))
    broker.submitReducerList(asJava[CustomCloudlet](reducers))

    //Not implemented: We can add network delays. (latency or limit bandwidth)
    //NetworkTopology.addLink(datacenter0.getId, broker.getId, 1000.0, 10)
    //NetworkTopology.addLink(datacenter1.getId, broker.getId, 1000.0, 10)

    // Start the simulation
    CloudSim.startSimulation()
    LOG.info("Starting simulation")

    // Stop simulation
    CloudSim.stopSimulation()
    LOG.info("Simulation finished")

    //Retrieve list of finished cloudlets from broker.
    val newList: List[CustomCloudlet] = asScala(broker.getCloudletReceivedList[CustomCloudlet]).toList

    // Print results of simulation
    printCloudletList(newList)

    //Compute total simulation time
    val endTime = System.currentTimeMillis
    val totalTimeTaken = (endTime - startTime) / 1000.0
    //LOG.info("The total time taken for the simulation: " + totalTimeTaken + " s.")
  }

  /**
   *
   * This methods is used to create multiple datacenters, it relies on createDatacentersImpl which provides the recursive implementation.
   *
   * @param numDc Number of Datacenters to be created.
   * @return List of created Datacenters.
   */
  def createDatacenters(numDc: Int) : List[NetworkDatacenter] = {

    val dclist = scala.collection.mutable.ListBuffer.empty[NetworkDatacenter]

    createDatacentersImpl(numDc, 0, dclist)

    return dclist.toList
  }

  /**
   *
   * This method is used to create a single Datacenter, the configuration for the datacenter is loaded from the config file depending on the name given.
   * (i.e. is name is datacenter0, the block datacenter0 will be loaded.)
   * Furthermore, this method relies on createHostImpl in order to create Host nodes inside a DC.
   *
   * @param name Name of the DC (Datacenter) to be created.
   * @param startId first ID of the first DC, usually 0 and filled in by calling method.
   * @return Created Datacenter instance.
   */
  //This method dynamically builds a datacenter
  def createDatacenter(name:String, startId: Int) : NetworkDatacenter = {

    val hostList = scala.collection.mutable.ListBuffer.empty[Host]

    //Read number of hosts to put in this DC from conf
    val numHosts = conf.getInt(SIM+"."+name+".numHosts")

    //Create hosts inside this datacenter
    createHostImpl(numHosts, startId, hostList)

    val arch = conf.getString(SIM+"."+name+".arch")
    val os = conf.getString(SIM+"."+name+".os")
    val vmm = conf.getString(SIM+"."+name+".vmm")
    val time_zone = conf.getDouble(SIM+"."+name+".time_zone") // time zone this resource located
    val cost = conf.getDouble(SIM+"."+name+".cost") // the cost of using processing in this resource
    val costPerMem = conf.getDouble(SIM+"."+name+".costPerMem") // the cost of using memory in this resource
    val costPerStorage = conf.getDouble(SIM+"."+name+".costPerStorage") // the cost of using storage in this
    // resource
    val costPerBw = conf.getDouble(SIM+"."+name+".costPerBw") // the cost of using bw in this resource

    val storageList = scala.collection.mutable.ListBuffer.empty[Storage] // we are not adding SAN
    val characteristics = new DatacenterCharacteristics(arch, os, vmm, asJava[Host](hostList), time_zone, cost, costPerMem, costPerStorage, costPerBw)

    val datacenter = new NetworkDatacenter(name, characteristics, new VmAllocationPolicySimple(asJava[Host](hostList)), asJava[Storage](storageList), 0)

    return datacenter
  }

  /**
   *
   * This method is used to recursively create multiple datacenters.
   *
   * @param numDc Number of Datacenters to be created.
   * @param startId startId first ID of the first DC, usually 0 and filled in by calling method.
   * @param dcList List of created Datacenters.
   */
  @scala.annotation.tailrec
  def createDatacentersImpl(numDc: Int, startId: Int, dcList: scala.collection.mutable.ListBuffer[NetworkDatacenter]) : Unit = {

    if(numDc==0) {
      return
    }

    val datacenter = createDatacenter("datacenter"+startId, startId)

    dcList += datacenter

    createDatacentersImpl(numDc-1, startId+1, dcList)
  }

  /**
   *
   * This method is used to recursively create CPUs on hosts.
   *
   * @param num Number of CPUs to be created
   * @param startId first ID of the first PE, usually 0 and filled in by calling method.
   * @param mips Mips of CPU
   * @param cpuList Result list containing all created CPUs
   */
  @scala.annotation.tailrec
  def createCPUs(num: Int, startId: Int, mips: Int, cpuList: scala.collection.mutable.ListBuffer[Pe]) : Unit = {

    // Finished creating CPUs, return
    if (num==0) {
      return
    }

    //Add PE unit
    cpuList += new Pe(startId, new PeProvisionerSimple(mips))

    createCPUs(num-1, startId+1, mips, cpuList)
  }

  /**
   *
   * This is the recursive method to create hosts, shall be invoked from createDatacenter.
   *
   * @param num Number of Hosts to be created
   * @param startId first ID of the first Host, usually 0 and filled in by calling method.
   * @param hostList Result list containing all created Hosts
   */
  @scala.annotation.tailrec
  def createHostImpl(num: Int, startId: Int, hostList: scala.collection.mutable.ListBuffer[Host]) : Unit = {
    val mips = conf.getInt(SIM+".host.mips")
    val numCPU = conf.getInt(SIM+".host.numCpu")

    // Finished creating hosts, return
    if (num==0) {
      return
    }

    val peList = scala.collection.mutable.ListBuffer.empty[Pe] // CPUs/Cores.

    //Create CPUs (Processing Units)
    createCPUs(numCPU, 0, mips, peList)

    val ram = conf.getInt(SIM+".host.ram") // host memory (MB)
    val storage = conf.getInt(SIM+".host.storage") // host storage
    val bw = conf.getInt(SIM+".host.bw")
    val diskSpeed = conf.getDouble(SIM+".host.diskSpeed") //todo: finish adding disk speed

    //Create Host, set default provisioners
    val host: CustomHost = new CustomHost(startId,
      new RamProvisionerSimple(ram),
      new BwProvisionerSimple(bw),
      storage,
      asJava[Pe](peList),
      new VmSchedulerTimeShared(asJava[Pe](peList)))

    /*
    * This method is a custom implementation that aims at simulating the disk speed of a certain host.
    * Please see related extended class (CustomHost.java and override method in CustomBroker.java)
     */
    host.setDiskSpeed(diskSpeed)

    hostList += host

    createHostImpl(num-1, startId+1, hostList)
  }

  /**
   *
   * This method provides the recursive implementation to create multiple VMs.
   * As of the current implementation all VMs are created equal, however this can easily be changed and different parameters can be loaded from the config file.
   *
   * @param userId The userId who owns this VM.
   * @param num The number of VMs to be created.
   * @param startId first ID of the first VM, usually 0 and filled in by calling method.
   * @param vmList The list of created VMs.
   */
  @scala.annotation.tailrec
  def createVMImpl(userId: Int, num: Int, startId: Int, vmList: scala.collection.mutable.ListBuffer[Vm]): Unit = {

    //Base case
    if (num==0) {
      return
    }

    val mips = conf.getInt(SIM+".vm.mips")
    val size = conf.getInt(SIM+".vm.size") // image size (MB)
    val ram = conf.getInt(SIM+".vm.ram") // vm memory (MB)
    val bw = conf.getInt(SIM+".vm.bw")
    val pesNumber = conf.getInt(SIM+".vm.pesNumber") // number of cpus
    val vmm = conf.getString(SIM+".vm.vmm") // VMM name

    // create VM, the CloudletSchedulerTimeShared policy
    val vm = new Vm(startId, userId, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerSpaceShared)
    LOG.info("VM"+startId+" (mips="+mips+",size="+size+",ram="+ram+",bw="+bw+",numCPUS="+pesNumber+") created")

    //Add VM to list
    vmList += vm

    createVMImpl(userId, num-1, startId+1, vmList)
  }

  /**
   *
   * This method is called to create one/more VMs.
   *
   * @param userId The userId who owns this VM.
   * @param vms The number of VMs to be created.
   * @return
   */

  def createVM(userId: Int, vms: Int) : List[Vm] = {

    val vmlist = scala.collection.mutable.ListBuffer.empty[Vm]

    createVMImpl(userId, vms, 0, vmlist)

    return vmlist.toList
  }

  /**
   * This method initializes an instance of CustomBroker.
   * @return
   */
  def createBroker() : CustomBroker = {
    val broker = new CustomBroker("Broker")
    return broker
  }

  @scala.annotation.tailrec
  private def createCloudletImpl(userId: Int, num: Int, cloudletType: CustomCloudlet.Type, startId: Int, cloudletList: scala.collection.mutable.ListBuffer[CustomCloudlet]):Unit = {

    if (num==0) {
      return
    }

    // Cloudlet properties
    val pesNumber = conf.getInt(SIM+".cloudlet.pesNumber") // number of cpus
    val length = conf.getInt(SIM+".cloudlet.length")
    val fileSize =  conf.getInt(SIM+".cloudlet.fileSize")
    val outputSize = conf.getInt(SIM+".cloudlet.outputSize")
    val utilizationModel = new UtilizationModelFull

    val cloudlet: CustomCloudlet = new CustomCloudlet(startId, length*2, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel)
    LOG.info("Cloudlet"+startId+" (length="+length+",fileSize="+fileSize+",outputSize="+outputSize+") created")
    cloudlet.setUserId(userId)
    cloudlet.setType(cloudletType) //Set custom type

    cloudletList += cloudlet

    createCloudletImpl(userId, num-1, cloudletType, startId+1, cloudletList)
  }

  def createCloudlet(userId: Int, cloudlets: Int, startId: Int, cloudletType: CustomCloudlet.Type) : List[CustomCloudlet] = {

    val cloudletlist = scala.collection.mutable.ListBuffer.empty[CustomCloudlet]

    createCloudletImpl(userId, cloudlets, cloudletType, startId, cloudletlist)

    return cloudletlist.toList
  }

  val indent = "    "
  val dft = new DecimalFormat("###.##")
  private def printSingleResultLine(cloudlet: CustomCloudlet): Unit = {
    if (cloudlet.getCloudletStatus == Cloudlet.SUCCESS) {

      val costPerSec = cloudlet.getCostPerSec(cloudlet.getResourceId) *  (cloudlet.getActualCPUTime()+cloudlet.getDelay)
      val ramUtilization = cloudlet.getUtilizationOfRam(0)
      //LOG.info("costPerSec: "+costPerSec)
      //LOG.info("ramUtilization: "+ramUtilization)

      if (cloudlet.getType==CustomCloudlet.Type.REDUCER) {
        LOG.info(indent + cloudlet.getCloudletId + indent + indent + "  SUCCESS" + indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime) + indent + indent + indent + dft.format(cloudlet.getSubmissionTime) + indent + indent + indent + indent + indent + dft.format(costPerSec) + indent + indent + indent + cloudlet.getType+"("+cloudlet.getAssociatedMappers+")" + indent + indent + indent + cloudlet.getHost.getId)
      } else {
        LOG.info(indent + cloudlet.getCloudletId + indent + indent + "  SUCCESS" + indent + indent + cloudlet.getResourceId + indent + indent + indent + cloudlet.getVmId + indent + indent + dft.format(cloudlet.getActualCPUTime) + indent + indent + dft.format(cloudlet.getExecStartTime) + indent + indent + indent + dft.format(cloudlet.getFinishTime) + indent + indent + indent + dft.format(cloudlet.getSubmissionTime) + indent + indent + indent + indent + indent + dft.format(costPerSec) + indent + indent + indent + cloudlet.getType  + indent + indent + indent + indent + indent + cloudlet.getHost.getId)
      }

    } else {
      LOG.info(indent + cloudlet.getCloudletId + indent + indent)
    }
  }

  /**
   *
   * This method computes the total cost of the simulation.
   * It takes into account the fact that we also pay for cloudlets not yet started, but we are paying for during data transfer.
   *
   * @param list Cloudlet list
   * @return Total simulation cost
   */
  private def computeTotalCost(list: List[CustomCloudlet]): Double = {

    if (list.isEmpty) {
      return 0.0
    }

    val cloudlet = list.last
    val costPerSec = cloudlet.getCostPerSec(cloudlet.getResourceId) *  (cloudlet.getActualCPUTime()+cloudlet.getDelay)
    val newList = list.splitAt(list.size-2)._1

    return costPerSec + computeTotalCost(newList)

  }

  private def printCloudletList(list: List[CustomCloudlet]): Unit = {
    LOG.info("");
    LOG.info("========== OUTPUT (CloudletSchedulerTimeShared) ==========")
    LOG.info("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time" + indent + "Submission Time" + indent + "Total cost of cloudlet" + indent + "Cloudlet type" + indent + "Host ID")

    //Call list item print function (functional programming technique)
    list.foreach(printSingleResultLine)

    LOG.info("Total cost: "+computeTotalCost(list))

    //Now writing results to CSV file
    writeResultsToCSV(list)
  }

  /**
   *
   * @param cloudlet Current Cloudlet
   * @param csvWriter FileWriter object used in order to write to CSV the results
   */
  private def writeCSVLine(cloudlet: CustomCloudlet, csvWriter: FileWriter) : Unit = {
    val costPerSec = cloudlet.getCostPerSec(cloudlet.getResourceId) *  (cloudlet.getActualCPUTime()+cloudlet.getDelay)
    csvWriter.append(cloudlet.getCloudletId+","+"SUCCESS"+","+cloudlet.getResourceId+","+cloudlet.getVmId+","+dft.format(cloudlet.getActualCPUTime)+","+dft.format(cloudlet.getExecStartTime)+","+dft.format(cloudlet.getFinishTime)+","+dft.format(cloudlet.getSubmissionTime)+","+dft.format(costPerSec)+","+cloudlet.getType+","+cloudlet.getHost.getId);
    csvWriter.append("\n");
  }

  /**
   *
   * This method generates a .csv file with the simulation results
   *
   * @param list Cloudlet list
   */
  private def writeResultsToCSV(list: List[CustomCloudlet]) : Unit = {

    val csvWriter = new FileWriter("BothShared.csv")
    val stringslist = Vector("CloudletId", ",","Status", ",", "Status",",","DatacenterId", ",","VmId", ",","Time", ",","StartTime", ",", "FinishTime",",","SubTime",", ","SubTime",",","TotalCost",",","CloudletType",",","HostId","\n")
    for(word <- stringslist) csvWriter.append(word)

    //Write list to CSV
    list.foreach(e => writeCSVLine(e, csvWriter))

    csvWriter.flush()
    csvWriter.close()
  }
}
