package com.example;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DummyPacket implements Delayed, Serializable {
    // kad budem slao nazad moracu da konvertujem sve opet ?
    private final long expirationTimeMilis;
    private final long delay;
    private final long arrivalTimeMilis;
    private long savedAtTime;
    // da vratim serveru paket u originalnom formatu
    private final byte[] originalBuffer;
    private final long id;

    public DummyPacket(long delaySeconds, byte[] buffer, long id) {
        this.expirationTimeMilis = System.currentTimeMillis() + delaySeconds * 1000L;
        this.delay = delaySeconds;
        this.arrivalTimeMilis = System.currentTimeMillis();
        this.originalBuffer = buffer.clone();
        this.id = id;
    }

    // metoda vraca preostali delay u jedinicama callera
    @Override
    public long getDelay(TimeUnit unit) {
        long remainingDelayMilis = expirationTimeMilis - System.currentTimeMillis();
        return unit.convert(remainingDelayMilis, TimeUnit.MILLISECONDS);
    }

    @Override
    public int compareTo(Delayed other) {
        return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), other.getDelay(TimeUnit.MILLISECONDS));

    }

    public long getId() {
        return id;

    }

    @Override
    public String toString() {
        return "Dummy Packet" + " DELAY: " + delay + " ID: " + id;

    }

    public byte[] getOriginalBuffer() {
        return originalBuffer;
    }

    public void setSavedTime(long savedTime) {
        this.savedAtTime = savedTime;
    }

    public long getExpirationTime() {
        return expirationTimeMilis;
    }

    public byte[] getCancelBuffer() {
        ByteBuffer buffer = ByteBuffer.allocate(12);
        buffer.order(ByteOrder.LITTLE_ENDIAN);

        buffer.putInt(2);
        buffer.putInt(12); 
        buffer.putInt((int) id); 

        return buffer.array();

    }

}