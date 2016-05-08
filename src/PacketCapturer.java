import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by BarryGates on 2016/4/25.
 *
 */

public class PacketCapturer implements PcapPacketHandler<String> {
    private Ethernet ethernet = new Ethernet();

    private Ip4 ip4 = new Ip4();

    public static class IpReassemblyBuffer extends JBuffer implements Comparable<IpReassemblyBuffer> {
        private Ip4 header = new Ip4();

        private int ipDatagramLength = -1;

        private int bytesCopiedIntoBuffer = 20;

        private final int START = 20;

        private final long TIMEOUT;
        private final int HASH;

        @Override
        public int hashCode() {
            return this.HASH;
        }

        private void transferFrom(Ip4 ip) {
            ip.transferTo(this, 0, 20, 0);
            header.peer(this, 0, 20);
            header.hlen(5); // Clear IP optional headers
            header.clearFlags(Ip4.FLAG_MORE_FRAGMENTS); // FRAG flag
            header.offset(0); // Offset is now 0
            header.checksum(0); // R
        }

        public void addLastSegment(JBuffer packet, int offset,
                                   int length, int packetOffset) {

            addSegment(packet, offset, length, packetOffset);

            this.ipDatagramLength = START + offset + length;

            super.setSize(this.ipDatagramLength);

            header.length(ipDatagramLength); // Set Ip4 total length field
        }

        public void addSegment(JBuffer packet, int offset, int length,
                               int packetOffset) {

            this.bytesCopiedIntoBuffer += length;
            packet.transferTo(this, packetOffset, length, offset + START);
        }

        public int compareTo(IpReassemblyBuffer o) {
            return (int) (o.TIMEOUT- this.TIMEOUT);
        }

        public boolean isComplete() {
            return this.ipDatagramLength == this.bytesCopiedIntoBuffer;
        }

        public boolean isTimedout() {
            return this.TIMEOUT < System.currentTimeMillis(); // Future or
            // past
        }

        public Ip4 getIpHeader() {
            return header;
        }

        public IpReassemblyBuffer(Ip4 ip, int size, long timeout, int hash) {
            super(size); // allocate memory

            this.TIMEOUT = timeout;
            this.HASH = hash;

            transferFrom(ip); // copy fragment's Ip header to our buffer
        }
    }

    public interface IpReassemblyBufferHandler {
        public void nextIpDatagram(IpReassemblyBuffer buffer);
    }

    public void nextPacket(PcapPacket packet, String user) {
        System.out.printf("Received packet at %s caplen=%-4d len=%-4d %s\n",
                new Date(packet.getCaptureHeader().timestampInMillis()),
                packet.getCaptureHeader().caplen(),
                packet.getCaptureHeader().wirelen(),
                packet.toHexdump());

        if (packet.hasHeader(ethernet)) {
            System.out.printf("ethernet.type = %X", ethernet.type());
        }

        if (packet.hasHeader(ip4)) {
            System.out.printf("ip.type = %d", ip4.type());
        }
    }

    public static void main(String[] args) {
        List<PcapIf> pcapIfs = new ArrayList<>();
        StringBuilder errBuf = new StringBuilder();
        Pcap.findAllDevs(pcapIfs, errBuf);
        if (pcapIfs.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s\n", errBuf.toString());
            return;
        }
        for (PcapIf pcapIf: pcapIfs) {
            System.out.println(pcapIf.toString());
        }

        PcapIf dev = pcapIfs.get(3);

        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;
        Pcap pcap = Pcap.openLive(dev.getName(), snaplen, flags, timeout, errBuf);
        if (pcap == null) {
            System.err.printf("Error while opening device for capture: %s\n", errBuf.toString());
            return;
        }
        pcap.loop(5 * 1000, new PacketCapturer(), "123");

        pcap.close();
    }
}
