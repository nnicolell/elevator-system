public interface ElevatorState {

    void waitingForRequest();

    void receivedRequest();

    void notifyScheduler();

    /**
     * Displays the current state information.
     */
    void displayState();
}
