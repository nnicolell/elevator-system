/**
 * Interface to represent Scheduler States
 */
public interface SchedulerState {
    void handleRequest(Scheduler scheduler);
    void displayState();
}
