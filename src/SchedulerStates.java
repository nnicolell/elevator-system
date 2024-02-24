/**
 * This class represents a state in the Scheduler state machine where the Scheduler is
 * waiting for the Elevator to arrive
 */
class NotifyElevatorState implements SchedulerState {
    @Override
    public void handleRequest(Scheduler scheduler) {
        scheduler.setState("WaitingForFloorEvent");
    }

    @Override
    public void displayState() {
        System.out.println("Scheduler State: Notifying Elevator of request");
    }
}

/**
 * This class represents a state in the Scheduler state machine where the Scheduler is
 * waiting to receive a floor event.
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
        System.out.println("Scheduler State: Waiting for Floor Event");
    }
}