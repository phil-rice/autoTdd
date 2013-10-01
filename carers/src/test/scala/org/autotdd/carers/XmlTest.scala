package org.autotdd.carers

import org.autotdd.engine.AbstractTest
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.autotdd.engine.XmlSituation
import org.autotdd.engine.XmlFragment

trait XmlTestMother {
  val xml = <Root><one>1</one><two>2</two><repeated>3</repeated><repeated>4</repeated></Root>

  val xmlSituationNoFragments = new XmlSituation() {}
  val xmlSituationOneFragmentFound = new XmlSituation() {
    val one = XmlFragment.string(xml) \ "one"
  }

  val xmlSituationOneFragmentNotFound = new XmlSituation() {
    val one = XmlFragment.string(xml) \ "NotIn"

  }
  val xmlSituationThreeFragment = new XmlSituation() {
    val one = XmlFragment.string(xml) \ "one"
    val two = XmlFragment.string(xml) \ "two"
    val repeated = XmlFragment.string(xml) \ "repeated"
  }

}

@RunWith(classOf[JUnitRunner])
class XmlTest extends AbstractTest with XmlTestMother {

  "An Xml Fragment with no fragments" should "Produce an empty Path Map" in {
  }

}

