import static java.lang.Thread.sleep;

/**
 * An Elevator to represent the elevator car moving either up or down floors.
 */
public class Elevator implements Runnable {
    /**
     * A scheduler representing the elevator scheduler to give and send events to
     */
    private Scheduler scheduler;

    /**
     * Constructor for the elevator
     * @param scheduler A Scheduler representing a scheduler for the elevator car
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Prints a message which floor and if the elevator car is going up or down
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
        while(scheduler.getNumReqsHandled() < scheduler.getNumReqs()) {
            HardwareDevice hardwareDevice = scheduler.getElevatorRequest();
            movingMessage(hardwareDevice);
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            hardwareDevice.setArrived();
            scheduler.checkElevatorStatus(hardwareDevice);
        }
    }
}