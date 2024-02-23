public class NotifyFloorState implements SchedulerState {
    @Override
    public void handleRequest(Scheduler scheduler) {
        scheduler.notifyFloorSubsystem();
        scheduler.setState("WaitingForFloorEvent");
    }

}
