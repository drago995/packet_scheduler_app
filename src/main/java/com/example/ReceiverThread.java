package com.example;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.BlockingQueue;

public class ReceiverThread extends Thread {
    private final DataInputStream in;
    private final BlockingQueue<Packet> delayQueue;
    private volatile boolean running = true;

    public ReceiverThread(DataInputStream in, BlockingQueue<Packet> queue) {
        this.in = in;
        this.delayQueue = queue;
    }

    @Override
    public void run() {

        while (running) {

            try {
                // determining packet type based on first 4 bytes
                byte[] typeBytes = new byte[4];
                in.readFully(typeBytes);

                ByteBuffer byteBuffer = ByteBuffer.wrap(typeBytes);
                byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
                int packetType = byteBuffer.getInt();

                // determining packet size based on type
                int packetSize = 0;
                int remainingBytes = 0;
                Packet packet;
                if (packetType == 1) {
                    packetSize = 16;
                    remainingBytes = packetSize - 4;
                    packet = new DummyPacket();
                } else {
                    throw new Exception("Unsupported packet type: " + packetType);

                }

                byte[] wholePacket = new byte[packetSize];

                System.arraycopy(typeBytes, 0, wholePacket, 0, 4);

                in.readFully(wholePacket, 4, remainingBytes);

                ByteBuffer bBuffer = ByteBuffer.wrap(wholePacket);

                bBuffer.order(ByteOrder.LITTLE_ENDIAN); // server sends data in little endian

                packet.readPacketDataFromBuffer(bBuffer, wholePacket);

                delayQueue.add(packet);
                System.out.println("Received packet: " + packet.toString());
            } catch (IOException e) {
                e.printStackTrace();

            } catch (Exception e) {
                System.out.println(e.getMessage());
                e.printStackTrace();
            }
        }

    }
    /*
     * // metoda za uspostavljanje konekcija i merenja downtime-a konekcije
     * private Socket connectWithRetry() {
     * long downtimeStart = 0;
     * 
     * while (true) {
     * try {
     * Socket s = new Socket();
     * // postavljamo koliko drugo treba da pokusava da se konektuje
     * s.connect(new InetSocketAddress(host, port), 2000);
     * // soket baca exception ako read ne uspe nakon 2 sekunde
     * s.setSoTimeout(2000);
     * // racunanje kolko je server bio down
     * if (downtimeStart > 0) {
     * serverOnAtTimeMilis = System.currentTimeMillis();
     * serverDownDurationMilis = serverOnAtTimeMilis - downtimeStart;
     * System.out.println("Server downtime: " + serverDownDurationMilis + " ms");
     * downtimeStart = 0;
     * }
     * 
     * System.out.println("Connection established!");
     * return s;
     * 
     * } catch (IOException e) {
     * // ako je tek oboren kreni odbrojavanje, ako ne nastavi da meris downtime
     * if (downtimeStart == 0) {
     * downtimeStart = System.currentTimeMillis();
     * System.out.
     * println("NE MOZE DA SE USPOSTAVI KONEKCIJA (starting downtime timer)");
     * } else {
     * System.out.println("NE MOZE DA SE USPOSTAVI KONEKCIJA (retrying...)");
     * }
     * 
     * // uspavljujemo nit kako ne bi opteretili server konstantim pokusavanjem
     * try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
     * }
     * }
     * }
     */

    public void shutdown() {
        running = false;
    }
}
