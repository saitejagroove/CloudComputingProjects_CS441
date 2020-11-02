import com.skarna3.hw2.Constants
import com.skarna3.hw2.helpers.StatisticsHelper
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.mutable.ListBuffer

class StatisticsHelperTest extends FunSuite with BeforeAndAfter {

  //Initialize Config and Logger objects from 3rd party libraries
  val configuration: Config = ConfigFactory.load("configuration.conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  test("StatisticsHelperTest.computeMedian") {

    val evenArray = configuration.getString("configuration.tests.evenArray")
    val oddArray = configuration.getString("configuration.tests.oddArray")

    val evenSplit = evenArray.split(Constants.COMMA)
    val oddSplit = oddArray.split(Constants.COMMA)

    val even: ListBuffer[Int] = new ListBuffer[Int]
    evenSplit.foreach(e => even.append(e.toInt))

    val odd: ListBuffer[Int] = new ListBuffer[Int]
    oddSplit.foreach(e => odd.append(e.toInt))

    val medianEven = StatisticsHelper.computeMedian(even)
    val medianOdd = StatisticsHelper.computeMedian(odd)

    LOG.debug("medianEven: "+medianEven)
    LOG.debug("medianOdd: "+medianOdd)

    assert(medianEven==2.0)
    assert(medianOdd==3.0)
  }

  test("StatisticsHelperTest.computeAvg") {

    val evenArray = configuration.getString("configuration.tests.evenArray")

    val evenSplit = evenArray.split(Constants.COMMA)

    val even: ListBuffer[Int] = new ListBuffer[Int]
    evenSplit.foreach(e => even.append(e.toInt))

    val average = StatisticsHelper.computeAvg(even)

    LOG.debug("Average: "+average)

    assert(average==2.0)
  }

}
