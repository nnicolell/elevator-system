import java.util.ArrayList;
import java.util.List;

/**
 * A class that contains the Controller for the UI
 */
public class ElevatorSystemController {
    /**
     * List of elevators
     */
    private List<Elevator> elevatorsList;
    /**
     * ElevatorSystemUI object
     */
    private ElevatorSystemUI ui;

    /**
     * Creates a contoller object
     * @param ui
     * @param elevatorsList
     */
    public ElevatorSystemController(ElevatorSystemUI ui, List<Elevator> elevatorsList) {
        this.elevatorsList = elevatorsList;
        this.ui = ui;
    }

    /**
     * Updates the elevators
     * @param elevator The elevator that needs to be updated
     */
    public void updateElevators(Elevator elevator) {
        ui.updateElevator(elevator);
    }

    /**
     * Updates the floor for that elevator
     * @param elevator The elevator that gets updated
     * @throws InterruptedException Exception for interruptions
     */
    public void updateFloor(Elevator elevator) throws InterruptedException {
        ui.updateFloor(elevator);
    }

}
