import com.skarna3.hw2.helpers.AuthorshipHelper
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.slf4j.{Logger, LoggerFactory}

class AuthorshipHelperTest extends FunSuite with BeforeAndAfter {

  //Initialize Config and Logger objects from 3rd party libraries
  val configuration: Config = ConfigFactory.load("configuration.conf")
  val LOG: Logger = LoggerFactory.getLogger(getClass)

  test("AuthorshipHelperTest.getAuthorshipScore") {

    val total1 = configuration.getInt("configuration.tests.autoScore1N")
    val total2 = configuration.getInt("configuration.tests.autoScore2N")

    val auto1st = AuthorshipHelper.getAuthorshipScore(1, total1)
    val auto1last = AuthorshipHelper.getAuthorshipScore(total1, total1)

    val auto2st = AuthorshipHelper.getAuthorshipScore(1, total2)
    val auto2middle = AuthorshipHelper.getAuthorshipScore(2, total2)
    val auto2last = AuthorshipHelper.getAuthorshipScore(total2, total2)

    LOG.debug("Computed autorship score 1 -> pos1:"+auto1st+" posend: "+auto1last)
    LOG.debug("Computed autorship score 1 -> pos1:"+auto2st+" posany: "+auto2middle+" posend: "+auto2last)

    assert(auto1st==0.625 && auto1last== 0.375)
    assert(auto2st==0.3125 && auto2middle===0.25 && auto2last== 0.1875)
  }
}
