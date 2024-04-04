import java.io.*;
import java.net.*;
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
     * A DatagramSocket to send and receive messages to and from the Scheduler subsystem.
     */
    private DatagramSocket sendReceiveSocket;

    /**
     * An ElevatorSystemLogger to log events.
     */
    private final ElevatorSystemLogger logger;

    /**
     * Initializes a new Floor with a Scheduler representing the elevator scheduler to receive and send events to.
     *
     * @param scheduler A Scheduler representing the elevator scheduler to receive and send events to.
     */
    public Floor(Scheduler scheduler) {
        this.scheduler = scheduler;
        logger = new ElevatorSystemLogger("Floor");

        // initialize the DatagramSocket to send and receive messages to and from the Scheduler subsystem
        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Sends a DatagramPacket containing information about the specified HardwareDevice to the Scheduler subsystem, and
     * receives an acknowledgment from the Scheduler subsystem.
     *
     * @param hardwareDevice A HardwareDevice representing the floor event to send to the Scheduler subsystem.
     */
    private void sendFloorEventAndReceiveAck(HardwareDevice hardwareDevice) {
        // send a packet to the Scheduler subsystem containing information about hardwareDevice
        String hardwareDeviceString = hardwareDevice.toString();
        byte[] hardwareDeviceBytes = hardwareDeviceString.getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(hardwareDeviceBytes, hardwareDeviceBytes.length,
                    InetAddress.getLocalHost(), 5000); // create packet with destination port 5000 (Scheduler)
            logger.info("Sending " + hardwareDeviceString + " to Scheduler.");
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // receive an acknowledgment from the Scheduler subsystem
        byte[] receiveData = new byte[200];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Received "
                + new String(receivePacket.getData(), 0, receivePacket.getLength()) + " from Scheduler.");
    }

    /**
     * Reads input.txt and creates a HardwareDevice objects to send to the Scheduler subsystem.
     */
    @Override
    public void run() {
        try {
            List<String> lines = Files.readAllLines(Paths.get("input.txt"));
            scheduler.setNumReqs(lines.size()); // notify Scheduler of how many floor events it will be receiving

            if (lines.isEmpty()) {
                logger.info("No requests to handle... Exiting.");
                System.exit(0);
            }

            // send each floor event to the Scheduler subsystem
            for (String s : lines) {
                String[] info = s.split(" ");
                sleep(1000);
                logger.info("Elevator requested to go " + info[2] + " at floor " + info[1] + ".");
                sendFloorEventAndReceiveAck(createHardwareDevice(info));
            }

            sendReceiveSocket.close(); // close socket once all floor events have been sent to the Scheduler
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
        }
    }

    /**
     * Creates a HardwareDevice with the specified information.
     *
     * @param info An array of String information to initialize the HardwareDevice.
     * @return A HardwareDevice with the specified information.
     */
    public HardwareDevice createHardwareDevice(String[] info) {
        // process the time, floor, floor button, and car button that was selected
        LocalTime time = LocalTime.parse(info[0]);
        int floor = Integer.parseInt(info[1]);
        FloorButton floorButton = info[2].equalsIgnoreCase("up") ? FloorButton.UP : FloorButton.DOWN;
        int carButton = Integer.parseInt(info[3]);

        // process the specified Elevator fault
        StringBuilder faultStringBuilder = new StringBuilder();
        for (int i = 4; i < info.length; i++) {
            faultStringBuilder.append(info[i]).append(" ");
        }
        String fault = faultStringBuilder.toString().trim();

        return new HardwareDevice("Elevator?", time, floor, floorButton, carButton, Fault.stringToFault(fault));
    }

}
