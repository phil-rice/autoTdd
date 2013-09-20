package org.autotdd.carers

import org.scalatest.FlatSpec
import org.scalatest.junit.JUnitRunner
import org.scalatest.junit.ShouldMatchersForJUnit
import org.junit.runner.RunWith
import org.autotdd.engine.tests.AutoTddJunitRunner



@RunWith(classOf[JUnitRunner])
class XmlTests extends FlatSpec with ShouldMatchersForJUnit {
  def blankTestWorld = World(Xmls.asDate("2010-1-1"), new NinoToDecisionMysql(), new NinoToValidateClaimFile())
  

}
@RunWith(classOf[JUnitRunner])
class WorldTests extends FlatSpec with ShouldMatchersForJUnit {
	def blankTestWorld = World(Xmls.asDate("2010-1-1"), new NinoToDecisionMysql(), new NinoToValidateClaimFile())
			
			
}
@RunWith(classOf[JUnitRunner])
class CarersTests extends FlatSpec with ShouldMatchersForJUnit {

}