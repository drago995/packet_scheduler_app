package com.example;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Hello world!
 * //
 */
public class App {
    private final Socket socket;
    private final PacketScheduler packetScheduler;
    private final ReceiverThread receiverThread;

    public App() throws UnknownHostException, IOException {
        socket = new Socket("hermes.plusplus.rs", 4000);
        packetScheduler = new PacketScheduler(new DataOutputStream(socket.getOutputStream()));
        // prvo ucitavamo pakete sa diska
        packetScheduler.loadPendingPackages();
        receiverThread = new ReceiverThread(new DataInputStream(socket.getInputStream()), packetScheduler);
        receiverThread.start();
        // nit koja se izvrsava pri gasenju JVM-a
        Runtime.getRuntime().addShutdownHook(new Thread(this::onShutdown));
    }

    public static void main(String[] args) {

        try {
            new App();
        } catch (IOException e) {
            System.out.println("Nije moguce povezati se na server !");
            e.printStackTrace();
        }

    }

    private void onShutdown() {

        packetScheduler.savePendingPackets();

    }

}
