package org.autotdd.carers

import org.junit.runner.RunWith

import org.autotdd.engine._
import org.autotdd.engine.tests.AutoTddJunitRunner
import scala.xml.Elem
import scala.concurrent.stm.InTxn
import scala.xml.XML
import scala.xml.NodeSeq

@RunWith(classOf[AutoTddJunitRunner])
object Carers {

  def calculateExpenses(w: World, e: Elem): ReasonsAndAmount = {
    val expenses = (e \\ "ExpensesData").headOption match {
      case Some(ed) => Expenses.expenses(w, ed.asInstanceOf[Elem])
      case _ => ReasonsAndAmount(None, List(KeyAndParams("claim.expenses.noExpensesData")))
    }
    expenses
  }
  def calculateWeeklyIncome(w: World, e: Elem): Double = {
    val i = Income.income(w, e)
    i.amount.getOrElse(0)
  }

  trait Fragments {
    def fragment(): Fragment;

  }
  //  val eng = 
  //  new XmlBuilderFactory2[World, Elem, ReasonAndAmount]{
  //    val a = 1; 
  //    val b = true
  //  }.builder.because((w: World, e: Elem) => b);

  trait Fragment {
    def \(s: String): Fragment
    def \\(s: String): Fragment
  }

  trait EngineFragments extends Fragments {
    val birthdate = fragment() \ "ValidateClaim" \ "ClaimantBirthDate" \ "PersonBirthDate"
    val DependantNino = fragment() \ "ValidateClaim" \ "DependantNINO"
    val ClaimAlwaysUK = fragment() \ "ValidateClaim" \ " ClaimData" \ "ClaimAlwaysUK"
    val ClaimCurrentResidentUK = fragment() \ "ValidateClaim" \ "ClaimData" \ "ClaimCurrentResidentUK"
    val ClaimEducationFullTime = fragment() \ "ValidateClaim" \ "ClaimData" \ "ClaimEducationFullTime"
    val ClaimRentalIncome = fragment() \ "ValidateClaim" \ "ClaimData" \ "ClaimRentalIncome"
  }

  val engine = XmlEngine[World, Elem, ReasonAndAmount]().
    withDefaultCode((w: World, e: Elem) => ReasonAndAmount("carer.default.notPaid")).

    fragment("BirthDate").\("ValidateClaim").\("ClaimantBirthDate").\("PersonBirthDate").
    fragment("DependantNino").\("ValidateClaim").\("DependantNINO").
    fragment("ClaimAlwaysUK").\("ValidateClaim").\("ClaimData").\\("ClaimAlwaysUK").
    fragment("ClaimCurrentResidentUK").\("ValidateClaim").\("ClaimData").\("ClaimCurrentResidentUK").
    fragment("ClaimEducationFullTime").\("ValidateClaim").\("ClaimData").\("ClaimEducationFullTime").
    fragment("ClaimRentalIncome").\("ValidateClaim").\("ClaimData").\("ClaimRentalIncome").

    useCase("Customers under age 16 are not entitled to CA").
    scenario(World("2010-6-9"), Xmls.validateClaim("CL100104A"), "Cl100104A-Age Under 16").
    expected(ReasonAndAmount("carer.claimant.under16")).
    because((w: World, e: Elem) => {
      val date = (e \\ "ClaimantBirthDate" \ "PersonBirthDate").text
      if (date != "") {
        val birthDate = Xmls.asDate(date)
        val d = birthDate.plusYears(16)
        val result = d.isAfter(w.today)
        result
      } else
        false
    }).
    useCase("Hours1 - Customers with Hours of caring must be 35 hours or more in any one week").
    scenario(World("2010-1-1"), Xmls.validateClaim("CL100105A"), "CL100105A-lessThen35Hours").
    expected(ReasonAndAmount("carer.claimant.under35hoursCaring")).
    because((w: World, e: Elem) => !Xmls.asYesNo((e \\ "ClaimData" \ "Claim35Hours").text)).

    useCase("Qualifying Benefit 3 - DP's without the required level of qualyfing benefit will result in the disallowance of the claim to CA.").
    scenario(World("2010-6-23"), Xmls.validateClaim("CL100106A"), "CL100106A-?????? ").
    expected(ReasonAndAmount("carer.qualifyingBenefit.dpWithoutRequiredLevelOfQualifyingBenefit")).
    because((w: World, e: Elem) => {
      val dependantNino = (e \\ "DependantNINO").text;
      val dependantXml = w.ninoToCis(dependantNino);
      val length = dependantXml.child.length
      length > 1 && (dependantXml \\ "AwardComponent").text != "DLA Middle Rate Care"
    }).

    useCase("Residence 3- Customer who is not considered resident and present in GB is not entitled to CA.").
    scenario(World("2010-6-7"), Xmls.validateClaim("CL100107A"), "CL100107A-notInGB").
    expected(ReasonAndAmount("carers.claimant.notResident")).
    because((w: World, e: Elem) => {
      !Xmls.asYesNo((e \\ "ClaimData" \ "ClaimAlwaysUK").text)
    }).

    useCase("Presence 2- Customers who have restrictions on their immigration status will be disallowed CA.").
    scenario(World("2010-6-7"), Xmls.validateClaim("CL100108A"), "CL100108A-restriction on immigration status").
    expected(ReasonAndAmount("carers.claimant.restriction.immigrationStatus")).
    because((w: World, e: Elem) => {
      !Xmls.asYesNo((e \\ "ClaimData" \ "ClaimCurrentResidentUK").text)
    }).

    useCase("Full Time Eduction 2  -Customer in FTE 21 hours or more each week are not entitled to CA.").
    scenario(World("2010-2-10"), Xmls.validateClaim("CL100109A"), "CL100109A-full time education").
    expected(ReasonAndAmount("carers.claimant.fullTimeEduction.moreThan21Hours")).
    because((w: World, e: Elem) => {
      Xmls.asYesNo((e \\ "ClaimData" \ "ClaimEducationFullTime").text)
    }).

    useCase("Employment 4  - Customer's claiming CA may claim an allowable expense of up to 50% of their childcare expenses where the child care is not being undertaken by a direct relative. This amount may then be deducted from their gross pay.").
    scenario(World("2010-3-22"), Xmls.validateClaim("CL100110A"), "CL100110A-child care allowance").
    expected(ReasonAndAmount("carers.validClaim", Some(95))).
    code((w: World, e: Elem) => {
      val expenses: Double = calculateExpenses(w, e).amount.getOrElse(0);
      val weeklyIncome: Double = calculateWeeklyIncome(w, e)
      ReasonAndAmount("carers.validClaim", Some(weeklyIncome - expenses))
    }).
    because((w: World, e: Elem) => {
      val expenses: Double = calculateExpenses(w, e).amount.getOrElse(0);
      val weeklyIncome: Double = calculateWeeklyIncome(w, e)
      val nett = weeklyIncome - expenses
      nett <= 100
    }).

    useCase("Employment 5 - Customers claiming CA may claim an allowable expense of up to 50% of their Private Pension contributions. This amount may then be deducted from their gross pay figure.").
    scenario(World("2010-3-8"), Xmls.validateClaim("CL100111A"), "CL100111A-private pension").
    expected(ReasonAndAmount("carers.validClaim", Some(95))).

    useCase("Employment 6 - Customers claiming CA may claim an allowable expense of up to 50% of their Occupational Pension contributions. This amount may then be deducted from their gross pay figure.").
    scenario(World("2010-3-8"), Xmls.validateClaim("CL100112A"), "CL100112A-occupational pension").
    expected(ReasonAndAmount("carers.validClaim", Some(95))).
    //    useCase("DP's without the required level of qualifing benefit will result in the disallowance of the claim to CA.").
    //    scenario(World.blankTestWorld, Xmls.validateClaim("CL100106A"), "CL100106A-No Qualifying Benefit").
    //    expected(ReasonAndPayment("carer.dp.withoutLevelOfQualifyingBenefit")).
    //    because((w: World, e: Elem) => true).

    useCase("Employment 7 - Customer in paid employment exceeding �100 (after allowable expenses) per week is not entitled to CA.").
    scenario(World("2010-6-1"), Xmls.validateClaim("CL100113A"), "CL100113A-paid employment earning too much").
    expected(ReasonAndAmount("carers.nettIncome.moreThan100PerWeek", None)).
    because((w: World, e: Elem) => {
      val expenses: Double = calculateExpenses(w, e).amount.getOrElse(0);
      val weeklyIncome: Double = calculateWeeklyIncome(w, e)
      val nett = weeklyIncome - expenses
      nett > 100
    }).

    useCase("Self employment 2 - Customer in Self employed work earning more than the prescribed limit of �100 per week (after allowable expenses) are not entitled to CA.").
    scenario(World("2010-3-1"), Xmls.validateClaim("CL100114A"), "CL114A-self employed earning too much").
    expected(ReasonAndAmount("carers.nettIncome.moreThan100PerWeek", None)).

    useCase("Sublet 2- Customers receiving payment for subletting their property for board and lodgings receiving more than the prescribed limit of �100 (after allowable expenses) will be disallowed for CA.").
    scenario(World("2010-3-1"), Xmls.validateClaim("CL100115A"), "CL115A-sub let").
    expected(ReasonAndAmount("carers.income.rental", None)).
    because((w: World, e: Elem) => Xmls.asYesNo((e \\ "ClaimRentalIncome").text)).

    useCase("Prop 2- Customer receiving an Income from the renting of another property or land in the UK or abroad either their own name or a share in a partners profit is above £100 per week(after allowable expenses) is not entitled to CA.").
    scenario(World("2010-3-1"), Xmls.validateClaim("CL100116A"), "CL116A-income from renting").
    expected(ReasonAndAmount("carers.income.rental", None)).

    //    useCase("Overlapping benefit 2 - Customer receiving certain other benefits at a rate lower than the rate of CA will reduce the payable amount of CA.").
    //    scenario(World("2010-5-5"), Xmls.validateClaim("CL100117A"), "CL100117A-self employed earning too much").
    //    expected(ReasonAndAmount("carers.overlappingBenefit.higherRatePension", None)).
    //    because((w: World, e: Elem) => {
    //      val dependantNino = (e \\ "DependantNINO").text;
    //      val dependantXml = w.ninoToCis(dependantNino);
    //      (dependantXml \\ "AwardComponent").text == "DLA Middle Rate Care"
    //    }).
    //
    //    useCase("Break 6 - Break in care, at a hospital for 2 weeks - still gets paid").
    //    scenario(World("2010-5-5"), Xmls.validateClaim("CL100119A"), "CL100119A-dp in hospital for 2 weeks").
    //    expected(ReasonAndAmount("carers.validClaim", Some(95))).
    build;

  def main(args: Array[String]) {
    println(Xmls.validateClaim("CL100113A"))

    val sharedWord = World("2010-1-1")
    //    val myWorld = sharedWorld.makeMeOne;
    println(Carers.engine(sharedWord, Xmls.validateClaim("CL100113A")))
    //    println(Dbase.template)
    println("done")

  }
}