public class NotifyFloorState implements State {
    @Override
    public void handleRequest(Scheduler scheduler) {
        scheduler.notifyFloorSubsystem();
        scheduler.setState("WaitingForFloorEvent");
    }

}
