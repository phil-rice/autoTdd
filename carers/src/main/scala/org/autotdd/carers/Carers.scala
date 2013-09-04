package org.autotdd.carers

import java.net._
import scala.concurrent.stm._
import scala.xml._
import org.apache.commons.dbcp.BasicDataSource
import org.autotdd.engine._
import org.joda.time._
import org.joda.time.format._
import org.springframework.jdbc.core.JdbcTemplate
import org.junit.runner.RunWith
import org.autotdd.engine.tests.AutoTddRunner

object Xmls {

  def validateClaim(id: String) = {
    try {
      val url = getClass.getClassLoader.getResource("ValidateClaim/CL100104A.xml")
      val xmlString = scala.io.Source.fromURL(url).mkString
      val xml = XML.loadString(xmlString)
      xml
    } catch {
      case e: Exception => throw new RuntimeException("Cannot load " + id, e)
    }
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

class Cache[K, V] extends {
  val data = Ref[Map[K, V]](Map())
  def findOrCreate(k: K, v: V) = {
    val f = (m: Map[K, V]) => m + (k -> v)
    //    data.transform(f);
  }
}

trait NinoToValidateClaim {
  def ninoToValidateClaim(w: World, nino: String): Elem
}

class NinoToValidateClaimFile extends NinoToValidateClaim {
  def ninoToValidateClaim(w: World, nino: String): Elem = {
    val xmlString = scala.io.Source.fromURL(getClass.getClassLoader.getResource(s"ValidateClaim/${nino}.xml")).mkString
    val xml = XML.loadString(xmlString)
    xml
  }

}

trait NinoToDecision {
  def ninoToDecision(implicit i: InTxn, w: World, nino: String): Elem
}

class NinoToDecisionMysql() extends NinoToDecision {
  def addToCache(implicit i: InTxn, w: World, nino: String, result: Elem) = {
    w.caches.ninoToDecision.transform((m) => m + (nino -> result))
    result
  }

  def apply(implicit i: InTxn, w: World, nino: String): Elem = ninoToDecision(i, w, nino)

  def ninoToDecision(implicit i: InTxn, w: World, nino: String): Elem = {
    val l = Dbase.template.queryForList("SELECT *FROM   INFORMATION_SCHEMA.SYSTEM_TABLES")
    l.size match {
      case 0 => addToCache(i, w, nino, <noDecision/>)
      case 1 => addToCache(i, w, nino, XML.loadString(l.get(0).toString))
      case _ => throw new IllegalStateException("Have two decisions for nino " + nino);
    }
  }
}

object World {
  implicit def worldToNanoToValidateClaim(w: World) = w.toValidateClaim
  implicit def worldToNanoToDecision(w: World) = w.toDecision

}

case class Caches(ninoToDecision: Ref[Map[String, Elem]] = Ref(Map()))

case class World(today: DateTime, toDecision: NinoToDecision, toValidateClaim: NinoToValidateClaim, caches: Caches = Caches()) {

}
@RunWith(classOf[AutoTddRunner])
object Carers {

  def blankTestWorld = World(Xmls.asDate("2010-1-1"), new NinoToDecisionMysql(), new NinoToValidateClaimFile())

  def hasQualifyingBenefit = Engine.stm[World, String, Boolean]().
    useCase("To have a valid claim the dependent must have data present in the database").
    scenario(blankTestWorld, "CL100104A", "Data present in database").
    expected(true).

    scenario(blankTestWorld, "CL100106A", "Data not present in database").
    expected(false).
    because((i: InTxn, w: World, nino: String) => {
      w.ninoToDecision(i, w, nino) != <noDecision/>
    });

  def engine = Engine[World, Elem, Option[Integer]]().
    useCase("DP's without the required level of qualifing benefit will result in the disallowance of the claim to CA.").
    scenario(blankTestWorld, Xmls.dpWithoutLevelOfQualifyingBenefit).
    expected(None).

    useCase("Customers under age 16 are not entitled to CA").
    scenario(blankTestWorld, Xmls.ageUnder16).
    expected(None).
    useCase("Customers with Hours of caring must be 35 hours or more in any one week").
    scenario(blankTestWorld, Xmls.lessThen35Hours).
    expected(None).
    because((w: World, e: Elem) => {
      val birthDate = Xmls.asDate((e \\ "ClaimantBirthDate" \ "PersonBirthDate") text)
      val d =birthDate.plusYears(16)
      val result=d.isAfter(w.today)
      println("Date: " + d +"\nToday: " + w.today+"\nResult: " + result)
      result
    }).

    build;

  def main(args: Array[String]) {
    val x = Xmls.ageUnder16
    println(x)
    println(Carers.engine(Carers.blankTestWorld, x))
    println(Dbase.template)
  }
}