public class WaitingForElevatorState implements SchedulerState {
    @Override
    public void handleRequest(Scheduler scheduler) {
        //im pre sure this is where we switch to the first elevator state and wait for that to finish???
//        if(scheduler.getCurrentFloorEvent().getArrived()) {scheduler.setState("NotifyFloor");}
        //the last elevator state should call ^ and then set state to notify floor
//        scheduler.setState("NotifyFloor");
//        displayState();
    }

    @Override
    public void displayState() {
        System.out.println("State: Waiting for Elevator");
    }

}
