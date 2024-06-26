import java.util.ArrayList;

/**
 * A class to test the ElevatorSystem.
 */
public class ElevatorSystem {

    public static void main(String[] args) {
        ArrayList<Integer> elevatorPortNumbers = new ArrayList<>();
        elevatorPortNumbers.add(70);
        elevatorPortNumbers.add(64);
        elevatorPortNumbers.add(67);
        elevatorPortNumbers.add(69);
        elevatorPortNumbers.add(96);
        Scheduler scheduler = new Scheduler(elevatorPortNumbers);
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        new ElevatorSystemUI(5, 22, scheduler.getAllElevators());
        schedulerThread.start();
    }
}

