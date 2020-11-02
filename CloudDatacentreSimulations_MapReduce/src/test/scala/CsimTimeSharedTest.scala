import java.util.Calendar

import com.karna3.{CustomCloudlet, CsimTimeShared}
import com.karna3.CsimTimeShared.{SIM, conf, createBroker, createCloudlet, createDatacenters, createHostImpl, createVM, getClass}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.Host
import org.cloudbus.cloudsim.core.CloudSim
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.{Logger, LoggerFactory}

class CsimTimeSharedTest extends FunSuite with BeforeAndAfter {

  val SIM = "simulation1";

  //Initialize Config and Logger objects from 3rd party libraries
  val conf: Config = ConfigFactory.load(SIM+".conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  before {
    val num_user = conf.getInt(SIM+".num_user")
    val calendar = Calendar.getInstance
    val trace_flag = conf.getBoolean("trace_flag")
    CloudSim.init(num_user, calendar, trace_flag)
  }

  test("CsimTimeShared.createDatacenter") {

    val name = "datacenter0"

    val datacenter0 = CsimTimeShared.createDatacenter("datacenter0", 0)

    val numHosts = conf.getInt(SIM+".datacenter0.numHosts")

    LOG.debug("Testing DC not null..")
    assert(datacenter0!=null)

    LOG.debug("Testing number of hosts matches..")
    assert(datacenter0.getHostList.size() == numHosts)
  }

  test("CsimTimeShared.createDatacenters") {

    val num_dcs = conf.getInt(SIM+".num_dcs")

    val datacenters = createDatacenters(num_dcs)

    LOG.debug("Testing DC not null..")
    assert(datacenters!=null)

    LOG.debug("Testing number of DCs matches..")
    assert(datacenters.size == num_dcs)
  }

  test("CsimTimeShared.createHostImpl") {

    val hostList = scala.collection.mutable.ListBuffer.empty[Host]

    //Read number of hosts to put in this DC from conf
    val numHosts = conf.getInt(SIM+".datacenter0.numHosts")

    //Create hosts inside this datacenter
    createHostImpl(numHosts, 0, hostList)

    LOG.debug("Testing hostList not empty..")
    assert(hostList.nonEmpty)

    LOG.debug("Testing number of hosts matches..")
    assert(hostList.length == numHosts)
  }

  test("CsimTimeShared.createVM") {

    val num_vms = conf.getInt(SIM+".num_vms")

    //Broker initialization
    val broker = CsimTimeShared.createBroker()
    val brokerId = broker.getId

    //Recursive call for VM creation
    val vmlist = createVM(brokerId, num_vms)

    LOG.debug("Testing vmlist not empty..")
    assert(vmlist.nonEmpty)

    LOG.debug("Testing number of VMs matches..")
    assert(vmlist.length == num_vms)
  }

  test("CsimTimeShared.createCloudlet") {

    val num_cl = 10

    //Broker initialization
    val broker = CsimTimeShared.createBroker()
    val brokerId = broker.getId

    val mappers = createCloudlet(brokerId, num_cl, 0, CustomCloudlet.Type.MAPPER)
    val reducers = createCloudlet(brokerId, num_cl, 0, CustomCloudlet.Type.REDUCER)

    LOG.debug("Testing mappers not empty..")
    assert(mappers.size == num_cl)

    LOG.debug("Testing reducers not empty..")
    assert(reducers.size == num_cl)

    LOG.debug("Testing cloudlets type..")
    //Testing only first one as they are all created equal
    assert(mappers(0).getType == CustomCloudlet.Type.MAPPER)
    assert(reducers(0).getType == CustomCloudlet.Type.REDUCER)
  }

}
