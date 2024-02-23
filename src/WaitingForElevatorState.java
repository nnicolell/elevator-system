public class WaitingForElevatorState implements State {
    @Override
    public void handleRequest(Scheduler scheduler) {
        //im pre sure this is where we switch to the first elevator state and wait for that to finish???

//        scheduler.checkElevatorStatus(scheduler.getCurrentFloorEvent());
        //the last elevator state should call ^ and then set state to notify floor
        scheduler.setState("NotifyFloor");
    }

}
