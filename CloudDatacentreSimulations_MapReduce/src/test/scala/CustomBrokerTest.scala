import java.util.Calendar

import com.karna3.{CustomCloudlet}
import com.karna3.CsimTimeShared.{createBroker, createCloudlet, createDatacenters, createVM}
import com.typesafe.config.{Config, ConfigFactory}
import org.cloudbus.cloudsim.Vm
import org.cloudbus.cloudsim.core.{CloudSim, CloudSimTags, SimEvent}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.{Logger, LoggerFactory}

import scala.jdk.javaapi.CollectionConverters.asJava

class CustomBrokerTest extends FunSuite with BeforeAndAfter {

  val SIM = "simulation1";

  //Initialize Config and Logger objects from 3rd party libraries
  val conf: Config = ConfigFactory.load("simulation1.conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  before {
    val num_user = conf.getInt(SIM+".num_user")
    val calendar = Calendar.getInstance
    val trace_flag = conf.getBoolean("trace_flag")
    CloudSim.init(num_user, calendar, trace_flag)
  }

  def testCloudlet(cloudlet: CustomCloudlet, exp_type: CustomCloudlet.Type) : Unit = {
    LOG.debug("Testing cloudlet "+cloudlet.getCloudletId+" is "+exp_type+"..")
    assert(cloudlet.getType==exp_type)
  }

  test("CustomBroker.submitMapperList") {

    val num_mappers = conf.getInt(SIM+".num_mappers")

    //Broker initialization
    val broker = createBroker()
    val brokerId = broker.getId

    val mappers = createCloudlet(brokerId, num_mappers, 0, CustomCloudlet.Type.MAPPER)
    broker.submitMapperList(asJava[CustomCloudlet](mappers))

    LOG.debug("Testing there all cloudlets were received..")
    assert(broker.getCloudletList.size()==num_mappers)

    val submittedCloudlets = broker.getCloudletList
    submittedCloudlets.forEach((e:CustomCloudlet) => testCloudlet(e, CustomCloudlet.Type.MAPPER))
  }

  test("CustomBroker.submitReducerList") {

    val num_reducers = conf.getInt(SIM+".num_reducers")

    //Broker initialization
    val broker = createBroker()
    val brokerId = broker.getId

    val reducers = createCloudlet(brokerId, num_reducers, 0, CustomCloudlet.Type.REDUCER)
    broker.submitReducerList(asJava[CustomCloudlet](reducers))

    LOG.debug("Testing there all cloudlets were received..")
    assert(broker.getReducerArrayList.size()==num_reducers)

    val submittedCloudlets = broker.getReducerArrayList
    submittedCloudlets.forEach((e:CustomCloudlet) => testCloudlet(e, CustomCloudlet.Type.REDUCER))
  }

  /**
   * This test is to show the functioning on the processEvent method.
   * For this purpose a sample simulation with 1 DC, 1 VM and only 3 mappers is run, at the end it is checked that the last submitted cloudlet is a REDUCER.
   * */
  test("CustomBroker.processEvent") {

    LOG.debug("Testing processEvent method..")

    //Recursive call for datacenter generation
    val datacenters = createDatacenters(1)

    //Broker initialization
    val broker = createBroker()
    val brokerId = broker.getId

    //Recursive call for VM creation
    val vmlist = createVM(brokerId, 3)
    //Set list of available VMs on broker
    broker.submitVmList(asJava[Vm](vmlist))

    val mappers = createCloudlet(brokerId, 3, 0, CustomCloudlet.Type.MAPPER)
    val reducers = createCloudlet(brokerId, 1, 3, CustomCloudlet.Type.REDUCER)

    broker.submitMapperList(asJava(mappers))
    broker.submitReducerList(asJava(reducers))

    CloudSim.startSimulation()
    CloudSim.stopSimulation()

    val recCloudlets = broker.getCloudletReceivedList

    val lastCloudlet = recCloudlets.get(recCloudlets.size()-1).asInstanceOf[CustomCloudlet]

    LOG.debug("Check last cloudlet submitted is a REDUCER..")
    assert(lastCloudlet.getType==CustomCloudlet.Type.REDUCER)
  }

}
