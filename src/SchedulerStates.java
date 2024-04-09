/**
 * This class represents a state in the Scheduler state machine where the Scheduler is waiting for the Elevator to
 * arrive.
 */
class NotifyElevator implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        //scheduler.receiveElevatorFloorEvent();
        scheduler.setState("WaitingForElevator");

    }

    @Override
    public String displayState() {
        return "NotifyElevator";
    }

}

/**
 * This class represents a state in the Scheduler state machine where the Scheduler is waiting to receive a floor event.
 */
class WaitingForFloorEvent implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        scheduler.setState("SelectElevator");
        scheduler.distributeFloorEvents();
    }

    @Override
    public String displayState() {
        return "WaitingForFloorEvent";
    }

}

/**
 * This class represents a state in Scheduler that notifies a floor after an elevator has finished moving and reaches
 * that floor.
 */
class NotifyFloor implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        if (scheduler.getBusyElevators().isEmpty()){
            scheduler.setState("WaitingForFloorEvent");
        }
        else {
            scheduler.setState("SelectElevator");
        }
    }

    @Override
    public String displayState() {
        return "NotifyFloor";
    }

}

/**
 * This class represents a state in Scheduler that notifies a floor after an elevator has finished moving and reaches
 * that floor.
 */
class SelectElevator implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        scheduler.setState("NotifyElevator");
    }

    @Override
    public String displayState() {
        return "SelectElevator";
    }

}

/**
 * This class represents a state in Scheduler that waits for an Elevator to complete its event.
 */
class WaitingForElevator implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        if (scheduler.getArrived()){
            scheduler.setState("NotifyFloor");
        }
    }


    @Override
    public String displayState() {
        return "WaitingForElevator";
    }
}