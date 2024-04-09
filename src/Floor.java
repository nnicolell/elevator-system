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
     * Sends a DatagramPacket to the Scheduler with the specified message.
     *
     * @param message A String representing a message to send to the Scheduler.
     */
    private void sendPacket(String message) {
        byte[] messageBytes = message.getBytes();
        try {
            DatagramPacket sendPacket = new DatagramPacket(messageBytes, messageBytes.length,
                    InetAddress.getLocalHost(), 5000);
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Sending " + message + " to Scheduler.");
    }

    /**
     * Receives a DatagramPacket from the Scheduler. Returns a String representing the message from the Scheduler.
     *
     * @return A String representing the message from the Scheduler.
     */
    private String receivePacket() {
        byte[] receiveBytes = new byte[200];
        DatagramPacket receivePacket = new DatagramPacket(receiveBytes, receiveBytes.length);
        try {
            sendReceiveSocket.receive(receivePacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // process the received message from the Scheduler
        String message = new String(receivePacket.getData(), 0, receivePacket.getLength());
        logger.info("Received " + message + " from Scheduler.");
        return message;
    }

    /**
     * Reads input.txt and creates a HardwareDevice objects to send to the Scheduler subsystem.
     */
    @Override
    public void run() {
        // send all floor events from input.txt to the Scheduler
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

                // send the floor event to the Scheduler and receive an acknowledgment
//                sendPacket(createHardwareDevice(info).toString());
//                receivePacket();

                HardwareDevice floorEvent = createHardwareDevice(info);
                logger.info("Sending " + floorEvent + " to Scheduler.");
                scheduler.addFloorEvent(createHardwareDevice(info));
                logger.info("Received ACK " + floorEvent + " from Scheduler.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println(e);
        }

        logger.info("All floor events from input.txt have been sent to Scheduler.");

        // receive DatagramPackets from the Scheduler once a floor event has finished running and send an acknowledgment
        while (scheduler.getNumReqsHandled() <= scheduler.getNumReqs()) {
            logger.info("Waiting for a completed floor event from Scheduler...");
            String completedFloorEvent = receivePacket();
            sendPacket("ACK " + completedFloorEvent);
        }

        logger.info("All floor events have been completed.");
        sendReceiveSocket.close(); // close socket once all floor events have been fulfilled
    }

    /**
     * Creates a HardwareDevice with the specified information.
     *
     * @param info An array of String information to initialize the HardwareDevice.
     * @return A HardwareDevice representing the specified information.
     */
    public HardwareDevice createHardwareDevice(String[] info) {
        // process the time, floor, floor button, and car button that was selected
        LocalTime time = LocalTime.parse(info[0]);
        int floor = Integer.parseInt(info[1]);
        FloorButton floorButton = info[2].equalsIgnoreCase("up") ? FloorButton.UP : FloorButton.DOWN;
        int carButton = Integer.parseInt(info[3]);
        int numPassengers = Integer.parseInt(info[4]); //number of passengers for the floor event

        // process the specified Elevator fault
        StringBuilder faultStringBuilder = new StringBuilder();
        for (int i = 5; i < info.length; i++) {
            faultStringBuilder.append(info[i]).append(" ");
        }
        String fault = faultStringBuilder.toString().trim();

        return new HardwareDevice("Elevator?", time, floor, floorButton, carButton, numPassengers, Fault.stringToFault(fault));
    }

}
