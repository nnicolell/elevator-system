import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class FloorListener implements Runnable {
    private Scheduler scheduler;
    private DatagramSocket sendSocketFloor, receiveSocketFloor;
    private static int port;
    private DatagramPacket sendPacketFloor;
    private boolean running=true;
    public FloorListener(Scheduler scheduler, int port) {
        this.port = port;
        this.scheduler = scheduler;
        try {
            sendSocketFloor = new DatagramSocket();
            receiveSocketFloor = new DatagramSocket(port);
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    public void setRunningToFalse() {
        running = false;
    }
    public void setRunningToTrue() {
        running = true;
    }

    public void closeFloorSocket() {
        receiveSocketFloor.close();
    }

    @Override
    public void run() {
        while(running) {
            try {
                checkForFloorEvent();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        closeFloorSocket();
    }
    /**
     * Receives the next floor event to be processed. Runs as long as there are more requests pending, it will wait
     * until there is an event added to the floor queue and no floor event is current being processed.
     *
     * @throws InterruptedException When a thread is interrupted while it is in a blocked state.
     */
    public synchronized void checkForFloorEvent() throws InterruptedException {
        // construct a DatagramPacket for receiving packets up to 100 bytes long
        byte[] floorData = new byte[150];
        DatagramPacket floorPacket = new DatagramPacket(floorData, floorData.length);

        // block until a DatagramPacket is received from receiveSocket
        System.out.println("[Scheduler] Waiting for packet from floor...");
        try {
            receiveSocketFloor.receive(floorPacket);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        // process the received DatagramPacket from the Floor subsystem
        String floorPacketString = new String(floorData, 0, floorPacket.getLength());
        System.out.println("[Scheduler] Received packet from floor containing: " + floorPacketString);
        HardwareDevice floorEvent = HardwareDevice.stringToHardwareDevice(floorPacketString);

        // add the floor event to the appropriate list of floor events to handle
        scheduler.addFloorEvent(floorEvent);

        // construct acknowledgment data including the content of the received packet
        byte[] acknowledgmentData = ("ACK " + floorPacketString).getBytes();

        // create a DatagramPacket for the acknowledgment and send it
        sendPacketFloor = new DatagramPacket(acknowledgmentData, acknowledgmentData.length, floorPacket.getAddress(),
                floorPacket.getPort());
        try {
            sendSocketFloor.send(sendPacketFloor);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("[Scheduler] Acknowledgment sent to floor!");
    }

    public DatagramPacket getSendPacketFloor() {
        return sendPacketFloor;
    }
}
