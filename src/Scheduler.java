import java.util.*;
public class Scheduler implements Runnable {
    private Queue<HardwareDevice> floorQueue = new ArrayDeque<>(); //Queue to store the floor events
    private HardwareDevice currentFloorEvent;


    public synchronized void checkForFloorEvent() throws InterruptedException { //get next pending request from floor
        while(floorQueue.isEmpty() && currentFloorEvent != null){
            try{
                wait();
            }
            catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        }
        currentFloorEvent = floorQueue.poll();
        System.out.print("Scheduler received floor request : " + currentFloorEvent.toString());

    }

    private synchronized void notifyFloorSubsystem() {//send alert back to floor thread
        System.out.print("Floor Event : " + currentFloorEvent + " has been completed");
        currentFloorEvent = null;
        notifyAll();
    }
    public synchronized void addFloorEvent(HardwareDevice hd){ //add request to the floor queue
        floorQueue.add(hd);
        notifyAll();
    }


    @Override
    public void run() {
        while(true){
            try {
                checkForFloorEvent();
                notifyFloorSubsystem();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
