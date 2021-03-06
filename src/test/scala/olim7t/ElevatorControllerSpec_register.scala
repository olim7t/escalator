package olim7t

import org.scalatest.matchers.MustMatchers
import org.scalatest.WordSpec

import ElevatorController._
import Protocol._

class ElevatorControllerSpec_register extends WordSpec
                                      with MustMatchers {

  "The elevator controller's register method" when {

    "idle at floor 3" must {
      val state = State(3, Close, List())

      "accept a pickup at floor 3" in {
        register(state, Call(3, Down)) must be ===
          State(3, Close, List(Task.pick1(3, Down)))
      }

      "accept a pickup at floor 5" in {
        register(state, Call(5, Down)) must be ===
          State(3, Close, List(Task.pick1(5, Down)))
      }

      "accept a pickup at floor 0" in {
        register(state, Call(0, Up)) must be ===
          State(3, Close, List(Task.pick1(0, Up)))
      }

      "accept a drop at floor 0" in {
        register(state, Go(0)) must be ===
          State(3, Close, List(Task.drop1(0)))
      }
    }

    "with doors open at floor 3, waiting for 2 users to come in and 2 to exit" must {
      val state = State(3, Open, List(Task(3, toPick = 2, toDrop = 2, direction = Some(Up))))

      "decrement count to pick if a user enters" in {
        register(state, UserHasEntered) must be ===
          State(3, Open, List(
            Task(3, toDrop = 2, toPick = 1, Some(Up))))
      }

      "decrement count to drop a user exits" in {
        register(state, UserHasExited) must be ===
          State(3, Open, List(
            Task(3, toDrop = 1, toPick = 2, Some(Up))))
      }
    }

    "with doors open at floor 3, waiting for 1 user to come in" must {
      val state = State(3, Open, List(Task(3, toPick = 1, direction = Some(Up))))

      "decrement count to pick if a user enters" in {
        register(state, UserHasEntered) must be ===
          State(3, Open, List(
            Task(3, toPick = 0, direction = None)))
      }
    }

    "at floor 2 going up to floor 4 for a ride down" must {
      val state = State(2, Close, List(Task.pick1(4, Down)))

      "merge a pickup at floor 4 to go down" in {
        register(state, Call(4, Down)) must be ===
          State(2, Close, List(Task.pick2(4, Down)))
      }

      "merge a pickup at floor 4 to go up, but keep the intention to go down" in {
        register(state, Call(4, Up)) must be ===
          State(2, Close, List(Task.pick2(4, Down)))
      }

      "prepend a pickup at floor 2 to go up" in {
        register(state, Call(2, Up)) must be ===
          State(2, Close, List(Task.pick1(2, Up), Task.pick1(4, Down)))
      }

      "prepend a pickup at floor 3 to go up" in {
        register(state, Call(3, Up)) must be ===
          State(2, Close, List(Task.pick1(3, Up), Task.pick1(4, Down)))
      }

      "append a pickup at floor 2 to go down" in {
        register(state, Call(2, Down)) must be ===
          State(2, Close, List(
            Task.pick1(4, Down),
            Task.pick1(2, Down)))
      }

      "append a pickup at floor 3 to go down" in {
        register(state, Call(3, Down)) must be ===
          State(2, Close, List(
            Task.pick1(4, Down),
            Task.pick1(3, Down)))
      }

      "prepend a drop at floor 2" in {
        register(state, Go(2)) must be ===
          State(2, Close, List(
            Task.drop1(2),
            Task.pick1(4, Down)))
      }

      "prepend a drop at floor 3" in {
        register(state, Go(3)) must be ===
          State(2, Close, List(
            Task.drop1(3),
            Task.pick1(4, Down)))
      }

      "merge a drop at floor 4" in {
        register(state, Go(4)) must be ===
          State(2, Close, List(
            Task(4, toDrop = 1, toPick = 1, Some(Down))))
      }

      "append a drop at floor 1" in {
        register(state, Go(1)) must be ===
          State(2, Close, List(
            Task.pick1(4, Down),
            Task.drop1(1)
          ))
      }
    }

    "at floor 3 going down to floor 1 for a ride up" must {
      val state = State(3, Close, List(Task.pick1(1, Up)))

      "merge a pickup at floor 1 to go up" in {
        register(state, Call(1, Up)) must be ===
          State(3, Close, List(Task.pick2(1, Up)))
      }

      "merge a pickup at floor 1 to go down, but keep the intention to go up" in {
        register(state, Call(1, Down)) must be ===
          State(3, Close, List(Task.pick2(1, Up)))
      }

      "prepend a pickup at floor 3 to go down" in {
        register(state, Call(3, Down)) must be ===
          State(3, Close, List(Task.pick1(3, Down), Task.pick1(1, Up)))
      }

      "prepend a pickup at floor 2 to go down" in {
        register(state, Call(2, Down)) must be ===
          State(3, Close, List(Task.pick1(2, Down), Task.pick1(1, Up)))
      }

      "append a pickup at floor 3 to go up" in {
        register(state, Call(3, Up)) must be ===
          State(3, Close, List(
            Task.pick1(1, Up),
            Task.pick1(3, Up)))
      }

      "append a pickup at floor 2 to go up" in {
        register(state, Call(2, Up)) must be ===
          State(3, Close, List(
            Task.pick1(1, Up),
            Task.pick1(2, Up)))
      }

      "prepend a drop at floor 3" in {
        register(state, Go(3)) must be ===
          State(3, Close, List(
            Task.drop1(3),
            Task.pick1(1, Up)))
      }

      "prepend a drop at floor 2" in {
        register(state, Go(2)) must be ===
          State(3, Close, List(
            Task.drop1(2),
            Task.pick1(1, Up)))
      }

      "merge a drop at floor 1" in {
        register(state, Go(1)) must be ===
          State(3, Close, List(
            Task(1, toDrop = 1, toPick = 1, Some(Up))))
      }

      "append a drop at floor 4" in {
        register(state, Go(4)) must be ===
          State(3, Close, List(
            Task.pick1(1, Up),
            Task.drop1(4)
          ))
      }
    }

    "going down to drop someone at floor 0" must {
      "merge a pickup at floor 0 to go up" in {
        val state = State(5, Close, List(Task(0, 1, 0, None)))
        register(state, Call(0, Up)) must be ===
          State(5, Close, List(Task(0, 1, 1, Some(Up))))
      }
    }

    "going up to drop someone at floor 5" must {
      "merge a pickup at floor 5 to go down" in {
        val state = State(5, Close, List(Task(5, 1, 0, None)))
        register(state, Call(5, Down)) must be ===
          State(5, Close, List(Task(5, 1, 1, Some(Down))))
      }
    }

    "at floor 2 with doors open" must {
      "enqueue a call from the same floor" in {
        val state = State(2, Open, List(Task(2, 0, 0, None), Task(5, 2, 0, None)))
        register(state, Call(2, Up)) must be ===
          State(2, Open, List(Task(2, 0, 0, None), Task(2, 0, 1, Some(Up)), Task(5, 2, 0, None)))
      }
    }

    "inserting task at an intermediate floor" must {
      "merge a call with a call from the same floor further down the list" in {
        val state = State(4, Close, List(Task(5, 2, 0, None),
                                         Task(0, 0, 1, Some(Up)),
                                         Task(1, 0, 1, Some(Up))
        ), 0)
        register(state, Call(1, Down)) must be ===
          State(4, Close, List(Task(5, 2, 0, None),
            Task(0, 0, 1, Some(Up)),
            Task(1, 0, 2, Some(Up))
          ), 0)
      }

      "merge a press with a call from the same floor further down the list" in {
        val state = State(4, Close, List(Task(5, 2, 0, None),
                                         Task(0, 0, 1, Some(Up)),
                                         Task(1, 0, 1, Some(Up))
                                    ), 0)
        register(state, Go(1)) must be ===
          State(4, Close, List(Task(5, 2, 0, None),
                               Task(0, 0, 1, Some(Up)),
                               Task(1, 1, 1, Some(Up))
                          ), 0)
      }
    }
  }
}
