import java.io.IOException;
import java.net.*;

/**
 * A class to receive floor events that the Floor subsystem is sending to the Scheduler subsystem.
 */
public class FloorListener implements Runnable {

    /**
     * A Scheduler representing the elevator scheduler that the Floor subsystem is sending floor events to.
     */
    private final Scheduler scheduler;

    /**
     * A DatagramSocket to receive messages from the Floor subsystem.
     */
    private final DatagramSocket receiveSocket;

    /**
     * A DatagramPacket to send acknowledgments to the Floor subsystem.
     */
    private DatagramPacket sendPacketFloor;

    /**
     * True, if the FloorListener should be running. False, if not.
     */
    private boolean running = true;

    /**
     * An ElevatorSystemLogger to log events.
     */
    private final ElevatorSystemLogger logger;

    /**
     * Initializes a FloorListener.
     *
     * @param scheduler A Scheduler representing the elevator scheduler that the Floor subsystem is sending floor events
     *                  to.
     * @param port An integer representing the port number to receive DatagramPackets from the Floor subsystem on.
     */
    public FloorListener(Scheduler scheduler, int port) {
        this.scheduler = scheduler;
        logger = new ElevatorSystemLogger("Scheduler");

        try {
            receiveSocket = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets the FloorListener to not be running.
     */
    public void setRunningToFalse() {
        running = false;
    }

    /**
     * Receives floor events from the Floor subsystem while the FloorListener should be running.
     */
    @Override
    public void run() {
        while (running) {
            receiveFloorEvent();
        }
        receiveSocket.close();
    }

    /**
     * Receives a floor event from the Floor subsystem, and sends an acknowledgment back to the Floor subsystem.
     */
    private void receiveFloorEvent() {
        // construct a DatagramPacket for receiving packets up to 100 bytes long
        byte[] floorData = new byte[150];
        DatagramPacket floorPacket = new DatagramPacket(floorData, floorData.length);

        // block until a DatagramPacket is received from receiveSocket
        logger.info("Waiting for packet from floor...");
        try {
            receiveSocket.receive(floorPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // process the received DatagramPacket from the Floor subsystem
        String floorPacketString = new String(floorData, 0, floorPacket.getLength());
        logger.info("Received floor event " + floorPacketString + " from Floor.");
        HardwareDevice floorEvent = HardwareDevice.stringToHardwareDevice(floorPacketString);

        // add the floor event to the appropriate list of floor events to handle
        scheduler.addFloorEvent(floorEvent);

        // construct acknowledgment data including the content of the received packet
        String acknowledgmentMsg = "ACK " + floorPacketString;
        byte[] acknowledgmentData = acknowledgmentMsg.getBytes();

        // create a DatagramPacket for the acknowledgment and send it
        sendPacketFloor = new DatagramPacket(acknowledgmentData, acknowledgmentData.length, floorPacket.getAddress(),
                floorPacket.getPort());
        try {
            DatagramSocket sendSocketFloor = new DatagramSocket();
            sendSocketFloor.send(sendPacketFloor);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        logger.info("Sending " + acknowledgmentMsg + " to Floor");
    }

    public DatagramPacket getSendPacketFloor() {
        return sendPacketFloor;
    }

}
