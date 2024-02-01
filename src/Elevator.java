import static java.lang.Thread.sleep;

/**
 * An Elevator to represent the elevator car moving either up or down floors.
 */
public class Elevator implements Runnable {
    private Scheduler scheduler;
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Prints a message on where the elevator car is going
     * @param hardwareDevice to pass necessary information
     */
    private void movingMessage(HardwareDevice hardwareDevice) {
        System.out.println("Elevator Car Currently Moving " + hardwareDevice.getFloorButton() + " to the " + hardwareDevice.getCarButton() + " floor...");
    }

    /**
     * Reads from Scheduler and changes the arrived variable in the HardwareDevice to true and sends it back to the Scheduler.
     */
    @Override
    public void run() {
        HardwareDevice hardwareDevice = scheduler.getElevatorRequest();
        movingMessage(hardwareDevice);
        try {
            sleep(10);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        hardwareDevice.setArrived();
        scheduler.checkElevatorStatus(hardwareDevice);
    }
}