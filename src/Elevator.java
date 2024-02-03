import static java.lang.Thread.sleep;

/**
 * An Elevator to represent the elevator car moving up or down floors.
 */
public class Elevator implements Runnable {

    /**
     * A Scheduler representing the elevator scheduler to receive and send events to.
     */
    private final Scheduler scheduler;

    /**
     * Initializes an Elevator with a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Elevator(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Prints a message describing which floor the Elevator is moving to, and if the Elevator is going up or down.
     *
     * @param hardwareDevice A HardwareDevice representing the current event.
     */
    private void printMovingMessage(HardwareDevice hardwareDevice) {
        System.out.println("[Elevator]" + " Elevator Car Currently Moving " + hardwareDevice.getFloorButton() + " to the "
                + hardwareDevice.getCarButton() + " floor...");
    }

    /**
     * Receives an Elevator event from the Scheduler and executes it. Sends a message back to the Scheduler once it is
     * done executing the event.
     */
    @Override
    public void run() {
        while (scheduler.getNumReqsHandled() <= scheduler.getNumReqs()) {
            HardwareDevice hardwareDevice = scheduler.getElevatorRequest();
            printMovingMessage(hardwareDevice);
            try {
                sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            hardwareDevice.setArrived();
            scheduler.checkElevatorStatus(hardwareDevice);
        }
    }

    /**
     * Returns a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @return A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Scheduler getScheduler() {
        return scheduler;
    }
}