import java.util.*;

public class Scheduler implements Runnable {

    private Queue<HardwareDevice> floorQueue; // Queue to store the floor events
    private HardwareDevice currentFloorEvent;
    private int numReqs;
    private int numReqsHandled;

    public Scheduler() {
        floorQueue = new ArrayDeque<>();
        numReqsHandled = 1;
        numReqs = 10000 ;
    }

    public synchronized void checkForFloorEvent() throws InterruptedException { // get next pending request from floor
        while((floorQueue.isEmpty() || currentFloorEvent != null)
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

    private synchronized void notifyFloorSubsystem() { // send alert back to floor thread
        System.out.println("Floor Event : " + currentFloorEvent + " has been completed");
        currentFloorEvent = null;
        numReqsHandled++;
        notifyAll();
    }

    public synchronized void addFloorEvent(HardwareDevice hardwareDevice) { // add request to the floor queue
        floorQueue.add(hardwareDevice);
        notifyAll();
    }

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

    public void setNumReqs(int numReqs) {
        this.numReqs = numReqs;
    }

    public int getNumReqs()  {
        return numReqs;
    }

    public int getNumReqsHandled() {
        return numReqsHandled;
    }

    @Override
    public void run() {
        while(numReqsHandled < numReqs) {
            try {
                checkForFloorEvent();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

}
