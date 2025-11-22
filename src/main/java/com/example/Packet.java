package com.example;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.concurrent.Delayed;

public interface Packet extends Serializable, Delayed {

    long getId();

    byte[] getOriginalBuffer();

    void readPacketDataFromBuffer(ByteBuffer buffer, byte[] raw);

}