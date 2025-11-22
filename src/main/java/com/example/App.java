package com.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

/**
 * Hello world!
 * //
 */
public class App {
    private final Socket socket;
    private final PacketSchedulerThread packetScheduler;
    private final ReceiverThread receiverThread;

    public App() throws UnknownHostException, IOException {
        socket = new Socket("hermes.plusplus.rs", 4000);
        BlockingQueue<Packet> queue = new DelayQueue<>();
        packetScheduler = new PacketSchedulerThread(new DataOutputStream(socket.getOutputStream()), queue);
        receiverThread = new ReceiverThread(new DataInputStream(socket.getInputStream()), queue);
        packetScheduler.start();
        receiverThread.start();
        // thread that exectutes before JVM shutdown to save pending packets
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }

    public static void main(String[] args) {

        try {
            new App();
        } catch (IOException e) {
            System.out.println("Was not able to connect to the server !");
            e.printStackTrace();
        }

    }

    private void onShutdown() {

        packetScheduler.savePendingPackets();
        // freeing resources
        receiverThread.shutdown();
        packetScheduler.shutdown();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
