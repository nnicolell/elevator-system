public class IdleState implements SchedulerState {
    public void handleFloorRequest(int floorNumber, Scheduler scheduler) {
        System.out.println("Received floor request. Dispatching elevator to floor " + floorNumber);
        scheduler.setState(new WaitingForElevatorState());
    }

    public void handleElevatorArrival(int floorNumber, Scheduler scheduler) {
    }
}
