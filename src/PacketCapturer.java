import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
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
        for (PcapIf pcapIf: pcapIfs) {
            System.out.println(pcapIf.toString());
        }

        PcapIf dev = pcapIfs.get(3);

        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;
        Pcap pcap = Pcap.openLive(dev.getName(), snaplen, flags, timeout, errBuf);

        pcap.loop(5 * 1000, new PacketCapturer(), "123");

        pcap.close();
    }
}
