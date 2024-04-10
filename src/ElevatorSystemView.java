/**
 * An interface representing the Views for the ElevatorSystemUI
 */
public interface ElevatorSystemView {

    /**
     * Updates the close-up of the specified elevator.
     *
     * @param elevator An Elevator representing the elevator that is to be updated.
     */
    void updateElevator(Elevator elevator);

    /**
     * Updates the floors for the specified elevator.
     *
     * @param elevator An Elevator representing the elevator that is to be updated.
     */
    void updateFloor(Elevator elevator);

    /**
     * Adds a request.
     * @param request A HardwareDevice representing the new request.
     */
    void addRequests(HardwareDevice request);

}