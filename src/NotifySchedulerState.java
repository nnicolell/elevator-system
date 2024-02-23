public class NotifySchedulerState implements ElevatorState{
    @Override
    public void handleRequest(Elevator context, HardwareDevice request) {
        context.setState("WaitingForElevatorRequest");
    }

    @Override
    public void displayState() {
        System.out.print("State: Notifying Scheduler");
    }
}
