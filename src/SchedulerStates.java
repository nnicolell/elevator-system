/**
 * This class represents a state in the Scheduler state machine where the Scheduler is waiting for the Elevator to
 * arrive.
 */
class NotifyElevatorState implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        scheduler.distributeFloorEvents();
        scheduler.receiveElevatorMessage();
        scheduler.setState("WaitingForFloorEvent");
    }

    @Override
    public void displayState() {
        System.out.println("[SchedulerState] Notifying Elevator of request");
    }

}

/**
 * This class represents a state in the Scheduler state machine where the Scheduler is waiting to receive a floor event.
 */
class WaitingForFloorEventState implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        try {
            scheduler.checkForFloorEvent();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        scheduler.setState("NotifyElevator");
    }

    @Override
    public void displayState() {
        System.out.println("[SchedulerState] Waiting for Floor Event");
    }

}

/**
 * This class represents a state in Scheduler that notifies a floor after an elevator has finished moving and reaches
 * that floor.
 */
class NotifyFloorState implements SchedulerState {

    @Override
    public void handleRequest(Scheduler scheduler) {
        scheduler.setState("WaitingForFloorEvent");
    }

    @Override
    public void displayState() {
        System.out.println("[SchedulerState] Notifying Floor of Elevator arrival");
    }

}