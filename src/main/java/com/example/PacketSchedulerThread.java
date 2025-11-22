package com.example;

import java.io.DataOutputStream;
import java.util.List;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.BlockingQueue;

public class PacketSchedulerThread extends Thread {

    private final BlockingQueue<Packet> delayQueue;
    private final DataOutputStream out;
    private final PacketPersistence packetPersistence;
    private volatile boolean running = true;
    private double dummiesSent = 0;
    private double cancelsSent = 0;

    public PacketSchedulerThread(DataOutputStream outputStream, BlockingQueue<Packet> queue) {
        this.delayQueue = queue;
        this.out = outputStream;
        this.packetPersistence = new PacketPersistence();
        loadPendingPackets();
    }

    public void run() {
        while (running) {
            try {
                Packet packet = delayQueue.take();

                synchronized (out) {
                    out.write(packet.getOriginalBuffer());
                    out.flush();
                    dummiesSent++;
                    System.out.println("Packet sent: " + packet.getId());
                }

            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isExpired(Packet packet) {
        return packet.getDelay(TimeUnit.MILLISECONDS) <= 0;
    }

    public void savePendingPackets() {
        List<Packet> list = delayQueue.stream().toList();
        packetPersistence.saveAll(list);
    }

    public void loadPendingPackets() {
        List<Packet> packets = packetPersistence.getAllPackets();
        for (Packet packet : packets) {
            if (!isExpired(packet)) {
                delayQueue.add(packet);
            } else {
                sendCancel(packet);
            }
        }
    }

    private void sendCancel(Packet packet) {
        try {
            synchronized (out) {
                if (packet instanceof DummyPacket) {
                    DummyPacket dummy = (DummyPacket) packet;
                    out.write(dummy.getCancelBuffer());
                }
                out.flush();
                cancelsSent++;
                System.out.println("Cancel sent for packet: " + packet.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getEfficiencyRatio() {
        return dummiesSent / (dummiesSent + cancelsSent);
    }

    public void shutdown() {
        running = false;
    }
}
