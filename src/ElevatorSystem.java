import java.time.LocalTime;

/**
 * A class to test the ElevatorSystem.
 */
public class ElevatorSystem {

    public static void main(String[] args) {
        Scheduler scheduler = new Scheduler();
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        Thread floor = new Thread(new Floor(scheduler),"Floor");
        Thread elevator = new Thread(new Elevator(scheduler),"Elevator");

        schedulerThread.start();
        elevator.start();
        floor.start();
    }
}

