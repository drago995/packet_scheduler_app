package com.example;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class PacketPersistence {

    private static final String FILE_NAME = "unsent_packets.dat";

    public PacketPersistence() {
    }

    public void saveAll(List<Packet> packets) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(packets);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<Packet> getAllPackets() {
        List<Packet> loadedPackets = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            loadedPackets = (List<Packet>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error reading packets from disk");
            e.printStackTrace();
        }
        return loadedPackets;
    }
}
