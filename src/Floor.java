import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * Floor to represent the floors the elevator car stops at
 */

public class Floor implements Runnable {

    /**
     * A Scheduler representing the elevator scheduler to receive and send events to.
     */
    private Scheduler scheduler;

    /**
     * Initializes a new Floor object
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Reads input from text file and creates a HardwareDevice objects to pass into Scheduler
     * to add to the queue
     */
    public void run() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("input.txt"));
            scheduler.setNumReqs(lines.size());

            for (String s : lines) {
                String[] info = s.split(" ");
                sleep(10);
                System.out.println(info[2] + " button pushed at floor " + info[1]);
                scheduler.addFloorEvent(createHardwareDevice(info));
            }
        } catch (IOException e) {
            System.err.println(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a HardwareDevice with the information provided
     * @param info Array of information including time elevator requester, floor from where it was requested
     *             whether it is going up or down, and floor it is going to
     * @return new HardwareDevice with properties given
     */
    public HardwareDevice createHardwareDevice(String[] info) {
        LocalTime l = LocalTime.parse(info[0]);
        int floorFrom = Integer.parseInt(info[1]);
        FloorButton button = info[2].equalsIgnoreCase("up") ? FloorButton.UP : FloorButton.DOWN;
        int floorTo = Integer.parseInt(info[3]);
        return new HardwareDevice(l, floorFrom, button, floorTo);
    }

}
