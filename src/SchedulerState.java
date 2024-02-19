public interface SchedulerState {
    void handleFloorRequest(int floorNumber, Scheduler scheduler);
    void handleElevatorArrival(int floorNumber, Scheduler scheduler);
}
