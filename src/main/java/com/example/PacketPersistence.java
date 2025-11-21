package com.example;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class PacketPersistence {

    public PacketPersistence() {

    }

    public void saveAll(List<DummyPacket> packets) {

        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("unsent_packets.dat"))) {
            oos.writeObject(packets);
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    public List<DummyPacket> getAllPackages() {
        List<DummyPacket> loadedPackets = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("unsent_packets.dat"))) {
            loadedPackets = (List<DummyPacket>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading packets from disc");
            e.printStackTrace();
        }
        
        return loadedPackets;

    }
}
