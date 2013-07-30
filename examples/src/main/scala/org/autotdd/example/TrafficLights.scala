package org.autotdd.example

import org.autotdd.engine.Engine
import org.junit.runner.RunWith
import org.autotdd.engine.tests.AutoTddRunner

case class TrafficLight(red: Boolean = false, orange: Boolean = false, green: Boolean = false)
@RunWith(classOf[AutoTddRunner])
object DecideAction {

  //This isn't complex enough to really need use cases
  val decide = Engine[TrafficLight, String]().
    useCase("Cars need to obey traffic signals").
    scenario(TrafficLight(red = true)).
    expected("Stop").

    scenario(TrafficLight(red = true, orange = true)).
    expected("Stop").

    scenario(TrafficLight(green = true)).
    because((l: TrafficLight) => l.green).
    expected("Go").

    scenario(TrafficLight(orange = true)).
    because((l: TrafficLight) => l.orange & !l.red).
    expected("Stop").

    build
}
