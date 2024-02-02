import java.time.LocalTime;

/**
 * A class to test the ElevatorSystem.
 */
public class ElevatorSystem {

    public static void main(String[] args) {
        // TODO: delete this chunk of code before pushing to master
        // testing how LocalTime can be created using a CharSequence
        LocalTime time = LocalTime.parse("14:05:15.0");
//        System.out.println(time);
        // testing accessor methods for HardwareDevice
        HardwareDevice device = new HardwareDevice(time, 2, FloorButton.UP, 4);
//        System.out.println(device.getTime());
//        System.out.println(device.getFloor());
//        System.out.println(device.getFloorButton());
//        System.out.println(device.getCarButton());
//        System.out.println(device.getArrived());

        Scheduler scheduler = new Scheduler();
        Thread schedulerThread = new Thread(scheduler, "Scheduler");
        Thread floor = new Thread(new Floor(scheduler),"Floor");
        Thread elevator = new Thread(new Elevator(scheduler),"Elevator Thread");


        schedulerThread.start();
        elevator.start();
        floor.start();

        // TODO: create a test case showing that your program can read the input file and pass the data back and forth
        // Scheduler is only being used as a communication channel from the Floor to the Elevator and back again
    }
}

