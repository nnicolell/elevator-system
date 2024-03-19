import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

import static java.lang.Thread.sleep;

/**
 * A Floor to represent the floors the elevator car stops at.
 */
public class Floor implements Runnable {
    private DatagramPacket sendPacket, receivePacket;
    private DatagramSocket sendReceiveSocket;

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
        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException se) {
            se.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Create a DatagramPacket to send to the Scheduler
     * @param hardwareDevice the HardwareDevice representing the floor event we want to send
     */
    private void  prepareSendPacket(HardwareDevice hardwareDevice) {
        String message = hardwareDevice.toString(); //create message
        byte msg[] = message.getBytes(); //convert message to byte array
        try {
            sendPacket = new DatagramPacket(msg, msg.length,
                    InetAddress.getLocalHost(), 5000); //create packet with destination port 5000
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    /**
     * Send and Receive DatagramPacket to and from the Scheduler.
     */
    private void sendAndReceive() {
        ///////SENDING TO SCHEDULER//////////////////
        try {
            sendReceiveSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }


        ////////////RECEIVING FROM SCHEDULER///////////////////////
        // creates a byte array given a capacity of bytes as 150
        byte receiveData[] = new byte[150];
        // creates new receive datagram packet
        receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            // receives the datagram packet
            sendReceiveSocket.receive(receivePacket);
        } catch(IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // creates a new byte array and copy's the bytes into the new array giving the new array that specific size
        byte temp[] = new byte[receivePacket.getLength()];
        for (int j = 0; j < receivePacket.getLength(); j++) {
            temp[j] = receiveData[j];
        }
        // prints that the Floor received the packet from scheduler
        System.out.println("[Floor] Receives Packet Containing: \n" + new String(temp));

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
                sleep(1000);
                System.out.println("[Floor] Elevator requested to go " + info[2] + " at floor " + info[1] + ".");
                prepareSendPacket(createHardwareDevice(info));
                sendAndReceive();
                //scheduler.addFloorEvent(createHardwareDevice(info));
            }
            // close socket
            sendReceiveSocket.close();
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
        return new HardwareDevice("Elevator?", l, floorFrom, button, floorTo);
    }

}
