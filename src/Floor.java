import java.io.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * A Floor to represent the floors the elevator car stops at.
 */
public class Floor implements Runnable {

    /**
     * A Scheduler representing the elevator scheduler to receive and send events to.
     */
    private final Scheduler scheduler;

    /**
     * Initializes a new Floor with a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Reads input from text file and creates a HardwareDevice objects to pass into Scheduler to add to the queue.
     */
    @Override
    public void run() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("input.txt"));
            scheduler.setNumReqs(lines.size());

            if (lines.isEmpty()) {
                System.out.println("No requests to handle!");
                System.exit(0);
            }

            for (String s : lines) {
                String[] info = s.split(" ");
                sleep(100);
                System.out.println("[Floor] Elevator requested to go " + info[2] + " at floor " + info[1] + ".");
                scheduler.addFloorEvent(createHardwareDevice(info));
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
        }
    }

    /**
     * Returns a HardwareDevice with the information provided.
     *
     * @param info An array of String information including time elevator requester, floor from where it was requested
     * whether it is going up or down, and floor it is going to
     * @return A HardwareDevice with the specified properties.
     */
    public HardwareDevice createHardwareDevice(String[] info) {
        LocalTime l = LocalTime.parse(info[0]);
        int floorFrom = Integer.parseInt(info[1]);
        FloorButton button = info[2].equalsIgnoreCase("up") ? FloorButton.UP : FloorButton.DOWN;
        int floorTo = Integer.parseInt(info[3]);
        return new HardwareDevice(l, floorFrom, button, floorTo);
    }

}
