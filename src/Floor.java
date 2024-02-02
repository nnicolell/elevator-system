import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.util.List;

import static java.lang.Thread.sleep;

public class Floor implements Runnable {
    //need to press button
    //receive message when elevator arrives to the floor --> send message that it wants to go to this floor
    //sending message elevator was requested
    //have to send the data about which floor to go after to the scheduler
    //need to know when the last one is read

    private Scheduler scheduler;
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Read data and push to queue
     */
    public void run() {
        readData();
    }
    /**
     * Reads input from text file and creates a HardwareDevice object to pass into Scheduler
     * to add to the queue
     */
    private void readData() {
        try {

            List<String> lines = Files.readAllLines(Paths.get("input.txt"));
            scheduler.setNumReqs(lines.size());

            File file = new File("input.txt");
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                String[] info = line.split(" ");
                sleep(10);
                System.out.println(info[2] + " button pushed at floor " + info[1]);
                scheduler.addFloorEvent(createHardwareDevice(info));
            }
            scheduler.setLastRequest(true);
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
    private HardwareDevice createHardwareDevice(String[] info) {
        LocalTime l = LocalTime.parse(info[0]);
        int floorFrom = Integer.parseInt(info[1]);
        FloorButton button = info[2].equalsIgnoreCase("up") ? FloorButton.UP : FloorButton.DOWN;
        int floorTo = Integer.parseInt(info[3]);
        return new HardwareDevice(l, floorFrom, button, floorTo);
    }
}

