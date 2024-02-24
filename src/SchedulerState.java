/**
 * An interface to represent the states in the Scheduler state machine.
 */
public interface SchedulerState {

    /**
     * Handles the event in the Scheduler state machine.
     *
     * @param scheduler A Scheduler representing the context of the state machine.
     */
    void handleRequest(Scheduler scheduler);

    /**
     * Displays the current state information.
     */
    void displayState();

}
