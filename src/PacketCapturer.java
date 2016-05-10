import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.nio.JBuffer;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.packet.format.XmlFormatter;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Ip4;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by BarryGates on 2016/4/25.
 *
 */

public class PacketCapturer implements PcapPacketHandler<String> {

    public void nextPacket(PcapPacket packet, String user) {
//        System.out.printf("\nReceived packet at %s caplen=%-4d len=%-4d\n",
//                new Date(packet.getCaptureHeader().timestampInMillis()),
//                packet.getCaptureHeader().caplen(),
//                packet.getCaptureHeader().wirelen());
        PacketMatcher packetMatcher = PacketMatcher.getInstance();
        packetMatcher.handlePacket(packet);

//        try {
//            if (packet.hasHeader(ethernet)) {
//                System.out.printf("ethernet.type = %X\n", ethernet.type());
//                System.out.printf("ethernet.source = %s\n", new String(ethernet.source(), "ASCII"));
//                System.out.printf("ethernet.destination = %s\n", new String(ethernet.destination(), "GBK"));
//            }
//
//            if (packet.hasHeader(ip4)) {
//                System.out.printf("ip.type = %d\n", ip4.type());
//                System.out.printf("ip.source = %s\n", new String(ip4.source(), "GBK"));
//                System.out.printf("ip.destination = %s\n", new String(ip4.destination(), "GBK"));
//            }
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }

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
        pcap.loop(5 * 1000, new PacketCapturer(), "123");

        pcap.close();
    }
}
