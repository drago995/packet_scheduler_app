package com.example;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

public class DummyPacket implements Packet {
    private long expirationTimeMilis;
    private long delay;
    private byte[] originalBuffer;
    private long id;
    private int length;
    private int type;

    public DummyPacket() {

    }

    // method returning remaining delay in given time unit
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

    @Override
    public void readPacketDataFromBuffer(ByteBuffer buffer, byte[] byteArray) {

        this.type = buffer.getInt();
        this.length = buffer.getInt();
        this.id = Integer.toUnsignedLong(buffer.getInt());
        this.delay = buffer.getInt();

        this.originalBuffer = byteArray.clone();

        this.expirationTimeMilis = System.currentTimeMillis() + delay * 1000L;

    }

}