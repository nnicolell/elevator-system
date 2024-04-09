import java.util.List;

/**
 * Contains the Views for the ElevatorSystemUI
 */
public interface ElevatorSystemView {
    /**
     * Updates the close-up of the specified elevator
     * @param elevator The elevator that is to be updated
     */
    void updateElevator(Elevator elevator);

    /**
     * Updates the floors for the specified elevator
     * @param elevator The elevator that is to be updated
     */
    void updateFloor(Elevator elevator);

    /**
     * Adds a request
     * @param request The new request
     */
    void addRequests(HardwareDevice request);
}