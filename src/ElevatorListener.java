import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ElevatorListener implements Runnable {
    private Scheduler scheduler;
    private DatagramSocket sendReceiveSocket;
    private Elevator elevator;
    private int port;

    public ElevatorListener(Scheduler scheduler) {
        this.scheduler = scheduler;
        this.elevator = elevator;
        try {
            sendReceiveSocket = new DatagramSocket();
        } catch (SocketException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        while(true) {

        }
    }

    public void send(DatagramPacket packet) {
        try {
            sendReceiveSocket.send(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public DatagramPacket receive(DatagramPacket packet) {
        try {
            sendReceiveSocket.receive(packet);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return packet;
    }
}
