package org.autotdd.carers

import scala.xml.NodeSeq
import scala.xml.Elem
import org.joda.time.DateTime
import org.joda.time.format._
import scala.concurrent.stm._
import org.autotdd.engine.Engine
import org.junit.runner.RunWith
import org.autotdd.engine.tests.AutoTddJunitRunner

case class CarersXmlSituation(w: World, e: Elem) extends XmlSituation {
  import XmlFragment._
  lazy val birthdate = date(e) \ "ClaimantData" \ "ClaimantBirthDate" \ "PersonBirthDate"
  lazy val DependantNino = string(e) \ "DependantData" \ "DependantNINO"
  lazy val ClaimAlwaysUK = yesNo(e, default = false) \ "ClaimData" \ "ClaimAlwaysUK"
  lazy val Claim35Hours = yesNo(e, default = false) \ "ClaimData" \ "Claim35Hours"
  lazy val ClaimCurrentResidentUK = yesNo(e, default = false) \ "ClaimData" \ "ClaimCurrentResidentUK"
  lazy val ClaimEducationFullTime = yesNo(e, default = false) \ "ClaimData" \ "ClaimEducationFullTime"
  lazy val ClaimRentalIncome = yesNo(e, default = false) \ "ClaimData" \ "ClaimRentalIncome"
  lazy val ClaimRentalIncome2 = integer(e) \ "ClaimData" \ "ClaimRentalIncome"
//  lazy val genderAtRegistration = strsing(e) \ "ClaimantData" \ "ClaimantGenderAtRegistration"

  lazy val dependantXml: Elem = DependantNino.get() match {
    case Some(s) => w.ninoToCis(s);
    case None => <NoDependantXml/>
  }
  lazy val DependantAwardComponent = string(dependantXml, default = "") \\ "AwardComponent"

  lazy val expenses = Expenses.expenses(w, e)
  lazy val income = Income.income(w, e)
  
  lazy val nettIncome: Option[Double] =
    for (e <- expenses.amount; i <- income.amount)
      yield i - e
      
  lazy val incomeOk =
    nettIncome match {
      case Some(i) => i < 100
      case _ => false
    }

}
@RunWith(classOf[AutoTddJunitRunner])
object Carers2 {
  implicit def worldElemToCarers(x: Tuple2[World, Elem]) = CarersXmlSituation(x._1, x._2)
  implicit def worldStringToCarers(x: Tuple2[World, String]) = CarersXmlSituation(x._1, Xmls.validateClaim(x._2))
  //  implicit def carersToWorld(x: CarersXmlSituation) = x.w
  //  implicit def carersToElem(x: CarersXmlSituation) = x.e

  val engine = Engine[CarersXmlSituation, ReasonAndAmount]().
    withDefaultCode((c: CarersXmlSituation) => ReasonAndAmount("carer.default.notPaid")).
    useCase("Customers under age 16 are not entitled to CA").
    scenario((World("2010-6-9"), "CL100104A"), "Cl100104A-Age Under 16").
    expected(ReasonAndAmount("carer.claimant.under16")).
    because((c: CarersXmlSituation) => c.birthdate.get() match {
      case Some(bd) => bd.plusYears(16).isAfter(c.w.today)
      case _ => false
    }).

    useCase("Hours1 - Customers with Hours of caring must be 35 hours or more in any one week").
    scenario((World("2010-1-1"), "CL100105A"), "CL100105A-lessThen35Hours").
    expected(ReasonAndAmount("carer.claimant.under35hoursCaring")).
    because((c: CarersXmlSituation) => !c.Claim35Hours()).

    useCase("Qualifying Benefit 3 - DP's without the required level of qualyfing benefit will result in the disallowance of the claim to CA.").
    scenario((World("2010-6-23"), "CL100106A"), "CL100106A-?????? ").
    expected(ReasonAndAmount("carer.qualifyingBenefit.dpWithoutRequiredLevelOfQualifyingBenefit")).
    because((c: CarersXmlSituation) => c.DependantAwardComponent() != "DLA Middle Rate Care").

    useCase("Residence 3- Customer who is not considered resident and present in GB is not entitled to CA.").
    scenario((World("2010-6-7"), "CL100107A"), "CL100107A-notInGB").
    expected(ReasonAndAmount("carers.claimant.notResident")).
    because((c: CarersXmlSituation) => !c.ClaimAlwaysUK()).

    useCase("Presence 2- Customers who have restrictions on their immigration status will be disallowed CA.").
    scenario((World("2010-6-7"), "CL100108A"), "CL100108A-restriction on immigration status").
    expected(ReasonAndAmount("carers.claimant.restriction.immigrationStatus")).
    because((c: CarersXmlSituation) => !c.ClaimCurrentResidentUK()).

    useCase("Full Time Eduction 2  -Customer in FTE 21 hours or more each week are not entitled to CA.").
    scenario((World("2010-2-10"), "CL100109A"), "CL100109A-full time education").
    expected(ReasonAndAmount("carers.claimant.fullTimeEduction.moreThan21Hours")).
    because((c: CarersXmlSituation) => c.ClaimEducationFullTime()).

    useCase("Employment 4  - Customer's claiming CA may claim an allowable expense of up to 50% of their childcare expenses where the child care is not being undertaken by a direct relative. This amount may then be deducted from their gross pay.").
    scenario((World("2010-3-22"), "CL100110A"), "CL100110A-child care allowance").
    expected(ReasonAndAmount("carers.validClaim", Some(95.0))).
    code((c: CarersXmlSituation) => ReasonAndAmount("carers.validClaim", c.nettIncome)).
    because((c: CarersXmlSituation) => c.incomeOk).

    useCase("Employment 5 - Customers claiming CA may claim an allowable expense of up to 50% of their Private Pension contributions. This amount may then be deducted from their gross pay figure.").
    scenario((World("2010-3-8"), "CL100111A"), "CL100111A-private pension").
    expected(ReasonAndAmount("carers.validClaim", Some(95.0))).

    useCase("Employment 6 - Customers claiming CA may claim an allowable expense of up to 50% of their Occupational Pension contributions. This amount may then be deducted from their gross pay figure.").
    scenario((World("2010-3-8"), "CL100112A"), "CL100112A-occupational pension").
    expected(ReasonAndAmount("carers.validClaim", Some(95.0))).
    //    useCase("DP's without the required level of qualifing benefit will result in the disallowance of the claim to CA.").
    //    scenario(World.blankTestWorld, Xmls.validateClaim("CL100106A"), "CL100106A-No Qualifying Benefit").
    //    expected(ReasonAndPayment("carer.dp.withoutLevelOfQualifyingBenefit")).
    //    because((w: World, e: Elem) => true).

    useCase("Employment 7 - Customer in paid employment exceeding �100 (after allowable expenses) per week is not entitled to CA.").
    scenario((World("2010-6-1"), "CL100113A"), "CL100113A-paid employment earning too much").
    expected(ReasonAndAmount("carers.nettIncome.moreThan100PerWeek", None)).
    because((c: CarersXmlSituation) => !c.incomeOk).

    useCase("Self employment 2 - Customer in Self employed work earning more than the prescribed limit of �100 per week (after allowable expenses) are not entitled to CA.").
    scenario((World("2010-3-1"), "CL100114A"), "CL114A-self employed earning too much").
    expected(ReasonAndAmount("carers.nettIncome.moreThan100PerWeek", None)).

    useCase("Sublet 2- Customers receiving payment for subletting their property for board and lodgings receiving more than the prescribed limit of �100 (after allowable expenses) will be disallowed for CA.").
    scenario((World("2010-3-1"), "CL100115A"), "CL115A-sub let").
    expected(ReasonAndAmount("carers.income.rental", None)).
    because((c: CarersXmlSituation) => c.ClaimRentalIncome()).

    useCase("Prop 2- Customer receiving an Income from the renting of another property or land in the UK or abroad either their own name or a share in a partners profit is above £100 per week(after allowable expenses) is not entitled to CA.").
    scenario((World("2010-3-1"), "CL100116A"), "CL116A-income from renting").
    expected(ReasonAndAmount("carers.income.rental", None)).

    build

  def main(args: Array[String]) {
	  val situation: CarersXmlSituation= (World("2010-6-1"), "CL100113A")
	  println(situation)
  }

}