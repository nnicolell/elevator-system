import java.util.*;

/**
 * A Scheduler to handle the messaging between the elevator and floor.
 */

public class Scheduler implements Runnable {

    /**
     * The queue to store the floor events
     */
    private Queue<HardwareDevice> floorQueue;

    /**
     * The current floor event that is being handled
     */
    private HardwareDevice currentFloorEvent;
    /**
     * The number of requests
     */
    private int numReqs;
    /**
     * The number of requests that have been handled
     */
    private int numReqsHandled;

    public Scheduler() {
        floorQueue = new ArrayDeque<>();
        currentFloorEvent = null;
        numReqsHandled = 1;
        numReqs = 10000;
    }

    /**
     * This method receives the next floor event to be processed. The method will run as long as
     * there are more requests pending, it will wait until there is an event added to the floor queue and no floor event is current
     * being processed.
     ** @throws InterruptedException
     */
    public synchronized void checkForFloorEvent() throws InterruptedException { // get next pending request from floor
        while ((floorQueue.isEmpty() || currentFloorEvent != null)
                && (numReqsHandled <= numReqs || currentFloorEvent == null)) {
            try {
                wait();
            }
            catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        currentFloorEvent = floorQueue.poll();
        System.out.println("Scheduler received floor request : " + currentFloorEvent.toString());
        notifyAll();
    }

    /**
     * Once the elevator subsystem finishes its task, the floor subsystem will be notified.
     * The number of requests handled will be incremented and the current floor event is cleared.
     */
    private synchronized void notifyFloorSubsystem() { // send alert back to floor thread
        System.out.println("Floor Event : " + currentFloorEvent + " has been completed");
        currentFloorEvent = null;
        numReqsHandled++;
        notifyAll();
    }

    /**
     * Add floor event into the floor queue
     * @param hardwareDevice the hardware device representing the floor event
     */
    public synchronized void addFloorEvent(HardwareDevice hardwareDevice) { // add request to the floor queue
        floorQueue.add(hardwareDevice);
        notifyAll();
    }

    /**
     * Constantly checks the elevator status, waiting for the elevator to complete its task. If the elevator
     * is still running and the number of requests handled is lower than the number of requests or the
     * currentFloorEvent is null, the thread should wait. Once the elevator has arrived, the floor subsystem should be
     * notified.
     * @param hardwareDevice The updated hardwareDevice
     */
    public synchronized void checkElevatorStatus(HardwareDevice hardwareDevice) {
        while (!hardwareDevice.getArrived() && (numReqsHandled <= numReqs || currentFloorEvent == null)) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("Elevator has arrived.");
        notifyFloorSubsystem();
    }

    /**
     * Returns the currentFloorEvent to the elevator if it is not null. If it is null, the thread
     * should wait.
     * @return the currentFloorEvent to the elevator
     */
    public synchronized HardwareDevice getElevatorRequest() {
        while (currentFloorEvent == null) {
            try {
                wait();
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }

        notifyAll();
        return currentFloorEvent;
    }

    /**
     * Sets the number of requests
     * @param numReqs The number of requests
     */
    public void setNumReqs(int numReqs) {
        this.numReqs = numReqs;
    }

    /**
     * Gets the number of requests
     * @return The number of requests
     */
    public int getNumReqs()  {
        return numReqs;
    }

    /**
     * Gets the number of requests that have been handled
     * @return The number of handled requests
     */
    public int getNumReqsHandled() {
        return numReqsHandled;
    }

    @Override
    public void run() {
        while (numReqsHandled < numReqs) {
            try {
                checkForFloorEvent();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Returns the floorQueue field
     * @return the floorQueue
     */
    public Queue<HardwareDevice> getFloorQueue() {
        return floorQueue;
    }

    /**
     * Returns the currentFloorEvent field
     * @return What the currentFloorEvent is
     */
    public HardwareDevice getCurrentFloorEvent() {
        return currentFloorEvent;
    }

}
