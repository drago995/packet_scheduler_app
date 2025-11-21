package com.example;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ReceiverThread extends Thread {
    private final PacketScheduler packetScheduler;
    private final DataInputStream in;

    public ReceiverThread(DataInputStream in, PacketScheduler scheduler) {
        this.in = in;
        this.packetScheduler = scheduler;
    }

    // u prvoj petlji pravimo rekonekcije ka serveru u slucaju pucanja server ili
    // gasenja soketa od strane servera
    //
    @Override
    public void run() {

        while (true) {
            // dok konekcija traje (server ne pukne)
            try {
                // citamo tip paketa - 4 bajta
                byte[] typeBytes = new byte[4];
                in.readFully(typeBytes);
                // server salje podatke u little endian - koristimo byte buffer za konverziju
                ByteBuffer bb = ByteBuffer.wrap(typeBytes);
                bb.order(ByteOrder.LITTLE_ENDIAN);
                int packetType = bb.getInt();

                // ostatak paketa
                int packetSize = (packetType == 1) ? 16 : 12;
                int remainingBytes = packetSize - 4;

                byte[] wholePacket = new byte[packetSize];

                // cuvamo paket u celosti kako bih ga poslao lakse nazad !
                System.arraycopy(typeBytes, 0, wholePacket, 0, 4);

                in.readFully(wholePacket, 4, remainingBytes);

                ByteBuffer bb2 = ByteBuffer.wrap(wholePacket);
                bb2.order(ByteOrder.LITTLE_ENDIAN);

                int type = bb2.getInt();
                int len = bb2.getInt();
                // java ima signed int, server salje unsigned pa konvertujemo
                int idSigned = bb2.getInt();

                long id = Integer.toUnsignedLong(idSigned);

                if (packetType == 1) {
                    int delay = bb2.getInt();
                    DummyPacket dummy = new DummyPacket(delay, wholePacket, id);
                    System.out.println(dummy);
                    packetScheduler.schedulePacket(dummy);

                } else if (packetType == 2) {
                    // CancelPacket cancelP = new CancelPacket(id, wholePacket);
                    // packetScheduler.cancelPacket(id);
                }

            } catch (IOException e) {
                System.out.println("PUKLA JE VEZA");
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
}
