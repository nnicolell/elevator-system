import java.util.ArrayList;
import java.util.List;

public class ElevatorSystemController {
    private Elevator elevator;
    private Scheduler scheduler;
    private List<Elevator> listElevators;
    private ElevatorSystemUI ui;

    public ElevatorSystemController(Elevator elevator, Scheduler scheduler, ElevatorSystemUI ui) {
        this.elevator = elevator;
        this.scheduler = scheduler;
        this.ui = ui;
    }

    public void setListElevators() {
        listElevators = scheduler.getAllElevators();
    }

    public void updateElevators() {
        //ui.update();
    }

}
