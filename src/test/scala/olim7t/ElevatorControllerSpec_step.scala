package olim7t

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import ElevatorController._
import Protocol._

class ElevatorControllerSpec_step extends WordSpec
                                  with MustMatchers {

  "The elevator controller's step method" must {

    "return the same state when doing nothing" in {
      val state = State(1, Close, List(Task.drop1(5)))
      step(state, Nothing) must be === state
    }

    "increment the floor number when going up" in {
      val state = State(1, Close, List(Task.drop1(5)))
      step(state, Up) must be === State(2, Close, List(Task.drop1(5)))
    }

    "decrement the floor number when going down" in {
      val state = State(5, Close, List(Task.drop1(1)))
      step(state, Down) must be === State(4, Close, List(Task.drop1(1)))
    }

    "record the doors state when opening" in {
      val state = State(1, Close, List(Task.drop1(1)))
      step(state, Open) must be === State(1, Open, List(Task.drop1(1)))
    }

    "record the doors state and clean the completed task when closing" in {
      val state = State(1, Open, List(Task(1)))
      step(state, Close) must be === State(1, Close, List())
    }
  }
}
