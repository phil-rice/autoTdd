package org.autotdd.carers

import java.net._
import scala.io.Source
import scala.xml._
import org.joda.time._
import org.joda.time.format._
import org.joda.convert._
import org.autotdd.engine._
import org.apache.commons.dbcp.BasicDataSource
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

object Xmls {

  def validateClaim(id: String) = {
    val xmlString = Source.fromURL(getClass.getClassLoader.getResource(s"ValidateClaim/{id}.xml")).mkString
    val xml = XML.loadString(xmlString)
    xml
  }

  lazy val ageUnder16 = validateClaim("CL100104A")
  lazy val lessThen35Hours = validateClaim("CL100105A")
  lazy val dpWithoutLevelOfQualifyingBenefit = validateClaim("CL100106A")
  lazy val customerNotGbResidentAndResident = validateClaim("CL100105A")

  private val formatter = DateTimeFormat.forPattern("yyyy-MM-dd");
  def asDate(s: String): DateTime = formatter.parseDateTime(s);

}

object Dbase {
  val dataSource = new BasicDataSource();

  dataSource.setDriverClassName("com.mysql.jdbc.Driver");
  dataSource.setUsername("root");
  dataSource.setPassword("iwtbde");
  dataSource.setUrl("jdbc:mysql://localhost/ca");
  dataSource.setMaxActive(10);
  dataSource.setMaxIdle(5);
  dataSource.setInitialSize(5);

  val template = new JdbcTemplate(dataSource)

  println("Result: " + template.queryForList("SELECT *FROM   INFORMATION_SCHEMA.SYSTEM_TABLES"))

}

trait NinoToValidateClaim {
  def ninoToValidateClaim(w: World, nino: String): (World, Elem)
}

class NinoToValidateClaimFile extends NinoToValidateClaim {
  def ninoToDecision(nino: String): Elem = {
    val xmlString = Source.fromURL(getClass.getClassLoader.getResource(s"ValidateClaim/${nino}.xml")).mkString
    val xml = XML.loadString(xmlString)
    xml
  }

}

trait NinoToDecision {
  def ninoToDecision(w: World, nino: String): (World, Elem)
}

class NinoToDecisionMysql() extends NinoToDecision {
  def addToCache(w: World, nino: String, result: Elem): (World, Elem) = {
    (World.ninoToDecisionL.mod(w, (c) => c + (nino -> result)), result)
  }

  def ninoToDecision(w: World, nino: String): (World, Elem) = {
    val l = Dbase.template.queryForList("SELECT *FROM   INFORMATION_SCHEMA.SYSTEM_TABLES")
    l.size match {
      case 0 => addToCache(w, nino, <noDecision/>)
      case 1 => addToCache(w, nino, XML.loadString(l.get(0).toString))
      case _ => throw new IllegalStateException("Have two decisions for nino " + nino);
    }
  }
}

object World {
  implicit def worldToNanoToValidateClaim(w: World) = w.toValidateClaim
  implicit def worldToNanoToDecision(w: World) = w.toDecision

  val ninoToDecisionL = Lens(
    get = (w: World) => w.caches.ninoToDecision,
    set = (w: World, c: Map[String, Elem]) => w.copy(caches = w.caches.copy(ninoToDecision = c)))

}

case class Caches(ninoToDecision: Map[String, Elem] = Map())

case class World(today: DateTime, toDecision: NinoToDecision, toValidateClaim: NinoToValidateClaim, caches: Caches = Caches()) {

}

object Carers {

  def blankTestWorld = World(Xmls.asDate("2010-1-1"), new NinoToDecisionMysql(), new NinoToValidateClaimFile())

  def hasQualifyingBenefit = Engine.state[World, String, Boolean]().
    useCase("To have a valid claim the dependent must have data present in the database").
    scenario(blankTestWorld, "CL100104A", "Data present in database").
    expected {
      val (newWorld, decision) = blankTestWorld.ninoToDecision(blankTestWorld, "CL100105A")
      (newWorld, true)
    }.code((w: World, nino: String) => {
      val (newWorld, decision) = w.ninoToDecision(w, nino);
      (newWorld, decision match {
        case <noDecision/> => false;
        case _ => true;
      })
    });

  def engine = Engine[World, Elem, Option[Integer]]().
    useCase("Customers under age 16 are not entitled to CA").
    scenario(blankTestWorld, Xmls.ageUnder16).
    expected(None).
    useCase("Customers with Hours of caring must be 35 hours or more in any one week").
    scenario(blankTestWorld, Xmls.lessThen35Hours).
    expected(None).
    because((w: World, e: Elem) => {
      val birthDate = Xmls.asDate((e \\ "ClaimantBirthDate" \ "PersonBirthDate") text)
      birthDate.plusYears(16).isBefore(w.today)
    }).

    useCase("DP's without the required level of qualifing benefit will result in the disallowance of the claim to CA.").
    scenario(blankTestWorld, Xmls.dpWithoutLevelOfQualifyingBenefit).
    expected(None).
    build;

  def main(args: Array[String]) {
    val x = Xmls.ageUnder16
    println(x)
    println(Carers.engine(Carers.testWorld, x))
    println(Dbase.template)
  }
}