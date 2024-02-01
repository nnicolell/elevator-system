import java.time.LocalTime;

/**
 * A class to test the ElevatorSystem.
 */
public class ElevatorSystem {

    public static void main(String[] args) {
        // TODO: delete this chunk of code before pushing to master
        // testing how LocalTime can be created using a CharSequence
        LocalTime time = LocalTime.parse("14:05:15.0");
        System.out.println(time);
        // testing accessor methods for HardwareDevice
        HardwareDevice device = new HardwareDevice(time, 2, FloorButton.UP, 4);
        System.out.println(device.time());
        System.out.println(device.floor());
        System.out.println(device.floorButton());
        System.out.println(device.carButton());

        // TODO: create a test case showing that your program can read the input file and pass the data back and forth
        // Scheduler is only being used as a communication channel from the Floor to the Elevator and back again
    }
}

