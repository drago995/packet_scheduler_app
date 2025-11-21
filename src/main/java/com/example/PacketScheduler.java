package com.example;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.DelayQueue;

public class PacketScheduler {
    DelayQueue<DummyPacket> delayQueue;
    private final DataOutputStream out;
    private final PacketPersistence packetPersistence;
    private double dummiesSent = 0;
    private double cancelsSent = 0;

    public PacketScheduler(DataOutputStream outputStream) {
        packetPersistence = new PacketPersistence();
        this.delayQueue = new DelayQueue<>();
        this.out = outputStream;
        // pokrecemo nit koja ce da salje pakete
        Thread worker = new Thread(this::startWorker);
        worker.setDaemon(true);
        worker.start();
    }

    public void schedulePacket(DummyPacket dummyPacket) {
        delayQueue.add(dummyPacket);
    }

    private void startWorker() {
        while (true) {
            try {
                // DelayQueue will block until a packet's delay has expired
                DummyPacket dummy = delayQueue.take();

                // samo saljemo dummy pakete, DelayQueue garantuje da je vreme stiglo
                synchronized (out) {
                    out.write(dummy.getOriginalBuffer());
                    out.flush();
                    dummiesSent++;
                    System.out.println("Dummy Packet sent: " + dummy.toString());
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // funkcija za proveru da li je paketu istekao delay 
    private boolean isExpired(DummyPacket dummy) {
        return dummy.getExpirationTime() < System.currentTimeMillis();
    }

    public void savePendingPackets() {
        List<DummyPacket> list = delayQueue.stream().toList();
        packetPersistence.saveAll(list);
    }

    // ucitavamo sacuvane pakete i ubacujemo u delay queue
    public void loadPendingPackages() {
        List<DummyPacket> packets = packetPersistence.getAllPackages();
        System.out.println("PAKETI SA DISKA:");
        for (DummyPacket packet : packets) {
            System.out.println(packet.toString());
            // proveravamo  pakete sa diska da li su vec istekli
            if (!isExpired(packet)) {
                schedulePacket(packet);
            } else {
                sendCancel(packet); // saljemo cancel za vec istekle pakete
            }
        }
        System.out.println("====================");
    }

    private void sendCancel(DummyPacket packet) {
        try {
            synchronized (out) {
                out.write(packet.getCancelBuffer());
                out.flush();
                cancelsSent++;
                System.out.println("Cancel Packet sent: " + packet.getId());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public double getEfficiencyRatio() {
        return dummiesSent / (dummiesSent + cancelsSent);
    }
}
