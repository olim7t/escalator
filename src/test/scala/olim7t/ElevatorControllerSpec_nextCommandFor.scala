package olim7t

import org.scalatest.WordSpec
import org.scalatest.matchers.MustMatchers

import ElevatorController._
import Protocol._

class ElevatorControllerSpec_nextCommandFor extends WordSpec
                                            with MustMatchers {

  "The elevator controller's nextCommandFor method" must {

    "do nothing when it has no pending tasks" in {
      val state = State(1, Close, List())
      nextCommandFor(state) must be === Nothing
    }

    "open the doors when doors are closed and there are people to pick up" in {
      val state = State(1, Close, List(Task.pick1(1, Up)))
      nextCommandFor(state) must be === Open
    }

    "open the doors when doors are closed and there are people to drop" in {
      val state = State(1, Close, List(Task.drop1(1)))
      nextCommandFor(state) must be === Open
    }

    "close the doors when doors are open and there are no more people to pick up or drop" in {
      val state = State(1, Open, List(Task(1)))
      nextCommandFor(state) must be === Close
    }

    "do nothing when doors are open and there are still people to pick up" in {
      val state = State(1, Open, List(Task.pick1(1, Up)))
      nextCommandFor(state) must be === Nothing
    }

    "do nothing when doors are open and there are still people to drop" in {
      val state = State(1, Open, List(Task.drop1(1)))
      nextCommandFor(state) must be === Nothing
    }

    "go up when the next task is to drop someone at an upper floor" in {
      val state = State(1, Close, List(Task.drop1(4)))
      nextCommandFor(state) must be === Up
    }

    "go up when the next task is to pick someone at an upper floor" in {
      val state = State(1, Close, List(Task(2, 0, 1, Some(Up)), Task(5, 1, 0, None)))
      nextCommandFor(state) must be === Up
    }

    "go down when the next task is to drop someone at a lower floor" in {
      val state = State(4, Close, List(Task.drop1(1)))
      nextCommandFor(state) must be === Down
    }

    "go down when the next task is to pick someone at a lower floor" in {
      val state = State(4, Close, List(Task.pick1(1, Up)))
      nextCommandFor(state) must be === Down
    }
  }
}
