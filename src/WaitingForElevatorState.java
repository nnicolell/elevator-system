public class WaitingForElevatorState implements SchedulerState {

    public void handleFloorRequest(int floorNumber, Scheduler scheduler) {
        //add new additional floor events and queue them into the array deque
    }

    public void handleElevatorArrival(int floorNumber, Scheduler scheduler) {
        System.out.println("Elevator arrived at floor " + floorNumber);
        //set the state back to the IdleState when the elevator arrives
        scheduler.setState(new IdleState());
        //i am not sure what to add here for handling the arrival event
    }
}
