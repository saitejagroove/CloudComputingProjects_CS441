import com.skarna3.hw2.helpers.BinHelper
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.{Logger, LoggerFactory}

class BinHelperTest extends FunSuite with BeforeAndAfter {

  //Initialize Config and Logger objects from 3rd party libraries
  val configuration: Config = ConfigFactory.load("configuration.conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  test("BinHelperTest.getBinForYears") {

    val year1 = configuration.getInt("configuration.tests.year1")
    val year2 = configuration.getInt("configuration.tests.year2")

    val bin1 = BinHelper.getBinForYears(year1)
    val bin2 = BinHelper.getBinForYears(year2)

    LOG.debug("Computed bin1: "+bin1)
    LOG.debug("Computed bin2: "+bin2)

    assert(bin1.equals("1901-1910"))
    assert(bin2.equals("1911-1920"))
  }

  test("BinHelperTest.getBinFromCount") {

    val count0 = configuration.getInt("configuration.tests.count0")
    val count1 = configuration.getInt("configuration.tests.count1")
    val count2 = configuration.getInt("configuration.tests.count2")

    val bin0 = BinHelper.getBinFromCount(count0)
    val bin1 = BinHelper.getBinFromCount(count1)
    val bin2 = BinHelper.getBinFromCount(count2)

    LOG.debug("Computed bin1: "+bin1)
    LOG.debug("Computed bin2: "+bin2)

    assert(bin0==null)
    assert(bin1.equals("1"))
    assert(bin2.equals("7-10"))
  }
}
