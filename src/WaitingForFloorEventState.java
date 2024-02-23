public class WaitingForFloorEventState implements SchedulerState {
    @Override
    public void handleRequest(Scheduler scheduler) {
        try {
            scheduler.checkForFloorEvent();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        scheduler.setState("WaitingForElevator");
    }


}
