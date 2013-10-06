package olim7t

import Protocol._

trait ElevatorController {
  import ElevatorController._

  private var state = defaultState

  def handle(event: Event) {
    state = register(state, event)
  }

  def nextCommand: Command = {
    val command = nextCommandFor(state)
    state = step(state, command)
    command
  }

  def dumpState = state
}

object ElevatorController {
  val log = org.slf4j.LoggerFactory.getLogger(classOf[ElevatorController])

  val MinFloor = 0
  val MaxFloor = 5
  val MaxInactivity = 10

  case class Task(floor: Int, toDrop: Int = 0, toPick: Int = 0, direction: Option[Direction] = None) {
    require((toPick == 0) ^ direction.isDefined)
  }
  object Task {
    def pick1(floor: Int, direction: Direction) = Task(floor, toPick = 1, direction = Some(direction))
    def pick2(floor: Int, direction: Direction) = Task(floor, toPick = 2, direction = Some(direction))
    def drop1(floor: Int) = Task(floor, toDrop = 1)
  }
  case class State(floor: Int, doors: DoorStatus, pendingTasks: List[Task], inactiveSince: Int = 0)

  def defaultState = State(0, Close, List())

  def register(oldState: State, event: Event): State = event match {
    case Reset(message) =>
      log.error(s"Resetting because ${message}")
      defaultState
    case _ if oldState.inactiveSince < MaxInactivity =>
      val newTasks = insert(event, oldState.floor, oldState.pendingTasks)
      oldState.copy(pendingTasks = newTasks)
  }

  private def insert(event: Event, currentFloor: Int, tasks: List[Task]): List[Task] = (tasks, event) match {
    // Temporary workaround for https://github.com/xebia-france/code-elevator/issues/13
    case (
      (task@Task(targetFloor, 0, 0, None)) :: otherTasks,
      Call(callFloor, _)
    ) if targetFloor == currentFloor && callFloor == targetFloor => {
      task :: insert(event, currentFloor, otherTasks)
    }

    // get call when no pending task
    case (
      List(),
      Call(callFloor, callDirection)
    ) => {
      Task.pick1(callFloor, callDirection) :: Nil
    }
    // get button press when no pending task
    case (
      List(),
      Go(floorToGo)
    ) => {
      Task.drop1(floorToGo) :: Nil
    }
    // count expected user entry
    case (
      (task@Task(targetFloor, _, toPickBefore, directionBefore)) :: otherTasks,
      UserHasEntered
    ) if targetFloor == currentFloor && toPickBefore > 0 => {
      // preserve invariant if count reaches 0
      val newDirection = directionBefore.filter(_ => toPickBefore > 1)
      task.copy(toPick = toPickBefore - 1, direction = newDirection) :: otherTasks
    }
    // count expected user exit
    case (
      (task@Task(targetFloor, toDropBefore, _, _)) :: otherTasks,
      UserHasExited
    ) if targetFloor == currentFloor && toDropBefore > 0 => {
      task.copy(toDrop = toDropBefore - 1) :: otherTasks
    }
    // get call from target floor. We merge even if the call is in the opposite direction,
    // because we have no way to prevent the other user to get in
    case (
      (task@Task(targetFloor, _, toPickBefore, targetDirection)) :: otherTasks,
      Call(callFloor, callDirection)
      ) if callFloor == targetFloor => {
      val newDirection = targetDirection orElse Some(callDirection)
      task.copy(toPick = toPickBefore + 1, direction = newDirection) :: otherTasks
    }
    // get call from intermediate floor in the same direction
    case (
      Task(targetFloor, _, _, _) :: otherTasks,
      Call(callFloor, Up)
    ) if targetFloor > callFloor && callFloor >= currentFloor => {
      Task.pick1(callFloor, Up) :: tasks
    }
    case (
      Task(targetFloor, _, _, _) :: otherTasks,
      Call(callFloor, Down)
    ) if targetFloor < callFloor && callFloor <= currentFloor => {
      Task.pick1(callFloor, Down) :: tasks
    }
    // get press to intermediate floor
    case (
      Task(targetFloor, _, _, _) :: otherTasks,
      Go(goFloor)
    ) if currentFloor <= goFloor && goFloor < targetFloor ||
         targetFloor < goFloor && goFloor <= currentFloor => {
      Task.drop1(goFloor) :: tasks
    }
    // get press to target floor
    case (
      (task@Task(targetFloor, toDropBefore, _, _)) :: otherTasks,
      Go(goFloor)
    ) if goFloor == targetFloor => {
      task.copy(toDrop = toDropBefore + 1) :: otherTasks
    }

    // recurse to insert further down the list
    case (
      (task@Task(targetFloor, _, _, _)) :: otherTasks,
      _
      ) => {
      task :: insert(event, targetFloor, otherTasks)
    }
    case _ =>
      log.error(s"Inconsistent state when inserting: ${event}, floor=${currentFloor}, ${tasks}")
      tasks
  }

  def nextCommandFor(state: State): Command = state match {
    // if we've been inactive for too long, keep sending the same command to trigger a reset from the server
    case State(_, _, _, MaxInactivity) => Open

    case State(_, _, List(), _) => Nothing

    case State(currentFloor, Close, Task(taskFloor, toDrop, toPick, _) :: _, _)
      if taskFloor == currentFloor && (toDrop > 0 || toPick > 0) =>
      Open

    case State(currentFloor, Open, Task(taskFloor, toDrop, toPick, _) :: _, _)
      if taskFloor == currentFloor && toDrop == 0 && toPick == 0 =>
      Close

    case State(currentFloor, Open, Task(taskFloor, toDrop, toPick, _) :: _, _)
      if taskFloor == currentFloor && (toDrop > 0 || toPick > 0) =>
      Nothing

    case State(currentFloor, Close, Task(taskFloor, _, _, _) :: _, _)
      if taskFloor > currentFloor =>
      Up

    case State(currentFloor, Close, Task(taskFloor, _, _, _) :: _, _)
      if taskFloor < currentFloor =>
      Down

    case _ =>
      log.error(s"Inconsistent state when computing next command: ${state}")
      Nothing
  }
  
  def step(state: State, command: Command): State = (state, command) match {
    case (State(_, _, _, MaxInactivity), _) =>
      state

    case (_, Nothing) =>
      state.copy(inactiveSince = state.inactiveSince + 1)

    case (State(floorBefore, _, _, _), Up) if floorBefore < MaxFloor =>
      state.copy(floor = floorBefore + 1, inactiveSince = 0)

    case (State(floorBefore, _, _, _), Down) if floorBefore > MinFloor =>
      state.copy(floor = floorBefore - 1, inactiveSince = 0)

    case (
      State(currentFloor, Close, Task(taskFloor, toDrop, toPick, _) :: _, _),
      Open
    ) if taskFloor == currentFloor && (toDrop > 0 || toPick > 0) =>
      state.copy(doors = Open, inactiveSince = 0)

    case (
      State(currentFloor, Open, Task(taskFloor, toDrop, toPick, _) :: otherTasks, _),
      Close
    ) if taskFloor == currentFloor && toDrop == 0 && toPick == 0 =>
      state.copy(doors = Close, pendingTasks = otherTasks, inactiveSince = 0)

    case _ =>
      log.error(s"Don't know how to apply ${command} to ${state}")
      state.copy(inactiveSince = state.inactiveSince + 1)
  }
}
