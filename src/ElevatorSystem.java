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
        Scheduler scheduler = new Scheduler();
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
//        Thread floor = new Thread(new Floor(scheduler),"Floor");
//        Thread elevator = new Thread(new Elevator(scheduler, 69),"Elevator");

        schedulerThread.start();
//        floor.start();
//        elevator.start();
    }
}

