import java.util.ArrayList;
import java.util.List;

public class ElevatorSystemController {
//    private Elevator elevator;
//    private Scheduler scheduler;
    private List<Elevator> elevatorsList;
    private ElevatorSystemUI ui;

    public ElevatorSystemController(ElevatorSystemUI ui, List<Elevator> elevatorsList) {
        this.elevatorsList = elevatorsList;
        this.ui = ui;
    }

//    public void setListElevators() {
//        listElevators = scheduler.getAllElevators();
//    }

    public void updateElevators(Elevator elevator) {
        ui.updateElevator(elevator);
        //ui.update();
    }

    public void updateFloor(Elevator elevator) throws InterruptedException {
        ui.updateFloor(elevator);
    }

}
