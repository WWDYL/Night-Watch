import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by BarryGates on 2016/4/25.
 *
 */

public class PacketCapturer implements PcapPacketHandler<String> {

    public void nextPacket(PcapPacket packet, String user) {
        PacketMatcher packetMatcher = PacketMatcher.getInstance();
        packetMatcher.handlePacket(packet);

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

        PcapIf dev = pcapIfs.get(1);

        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;
        Pcap pcap = Pcap.openLive(dev.getName(), snaplen, flags, timeout, errBuf);
        if (pcap == null) {
            System.err.printf("Error while opening device for capture: %s\n", errBuf.toString());
            return;
        }
        pcap.loop(5 * 1000, new PacketCapturer(), null);

        pcap.close();
    }
}
