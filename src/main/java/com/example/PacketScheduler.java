package com.example;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.DelayQueue;

public class PacketScheduler {
    DelayQueue<DummyPacket> delayQueue;
    private final DataOutputStream out;
    private final PacketPersistence packetPersistence;

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
                DummyPacket dummy = delayQueue.take();

                // ako paketu nije istekao delay, saljemo ga kao dummy, u suprotnom saljemo
                // cancel
                if (!isExpired(dummy)) {
                    synchronized (out) {
                        out.write(dummy.getOriginalBuffer());
                        out.flush();
                        System.out.println("Dummy Packet sent: " + dummy.toString());
                    }
                } else {
                    byte[] cancelData = dummy.getCancelBuffer();
                    synchronized (out) {
                        out.write(cancelData);
                        out.flush();
                        System.out.println("Cancel Packet sent: " + dummy.getId());
                    }

                }

            } catch (InterruptedException e) {

                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }

    // funkcija za proveru da l je paketu istekao delay
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
        
        for (DummyPacket packet : packets) {
            System.out.println(packet.toString());
            schedulePacket(packet);
        }
    }

}
