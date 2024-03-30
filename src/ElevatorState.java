/**
 * An interface to represent the states in the Elevator state machine.
 */
public interface ElevatorState {

    /**
     * Handles an event in the Elevator state machine based on the specified context and main floor event.
     *
     * @param context An Elevator representing the context of the state machine.
     * @param mainFloorEvent A HardwareDevice representing the main floor event to execute.
     */
    void handleRequest(Elevator context, HardwareDevice mainFloorEvent);

    /**
     * Displays the current state information.
     */
    void displayState();

}
