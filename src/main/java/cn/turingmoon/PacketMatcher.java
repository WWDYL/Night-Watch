package cn.turingmoon;

import cn.turingmoon.utilities.MongoDbUtils;
import org.bson.Document;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Arp;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * cn.turingmoon.PacketMatcher 类
 * Created by Deng Li on 2016/5/10.
 */

class PacketMatcher {

    private MongoDbUtils mongoDbUtils;
    private Document doc;
    private static PacketMatcher pm;
    private Ethernet ethernet = new Ethernet();
    private Ip4 ip = new Ip4();
    private Arp arp = new Arp();
    private Tcp tcp = new Tcp();
    private Udp udp = new Udp();
    private Icmp icmp = new Icmp();

    static PacketMatcher getInstance() {
        if (pm == null) {
            pm = new PacketMatcher();
        }
        return pm;
    }

    private PacketMatcher() {
        mongoDbUtils = MongoDbUtils.getInstance();
    }

    void handlePacket(JPacket packet) {

        System.out.printf("\nReceived packet at %s caplen=%-4d len=%-4d\n",
                new Date(packet.getCaptureHeader().timestampInMillis()),
                packet.getCaptureHeader().caplen(),
                packet.getCaptureHeader().wirelen());
        doc = new Document("timestamp", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
                .format(packet.getCaptureHeader().timestampInMillis()))
                .append("caplen", packet.getCaptureHeader().caplen())
                .append("wirelen", packet.getCaptureHeader().wirelen());
        if (packet.hasHeader(ethernet)) {
            ethernetHandler(ethernet);
        }
        if (packet.hasHeader(ip)) {
            ip4Handler(ip);
        }
        else if (packet.hasHeader(arp)) {
            arpHandler(arp);
        }
        if (packet.hasHeader(icmp)) {
            icmpHandler(icmp);
        }
        else if (packet.hasHeader(udp)) {
            udpHandler(udp);
        }
        else if (packet.hasHeader(tcp)) {
            tcpHandler(tcp);
        }
        mongoDbUtils.storeOneRecord(doc);
    }

    private void ethernetHandler(Ethernet ethernet) {
        String srcMac = FormatUtils.mac(ethernet.source());
        String dstMac = FormatUtils.mac(ethernet.destination());
        int length = ethernet.getLength();
        int offset = ethernet.getOffset();
        System.out.println("ETHERNET: " + srcMac + " -> " + dstMac + " " + length + " " + offset);
        doc.append("ethernet", new Document("src", srcMac)
                    .append("dst", dstMac)
                    .append("length", length)
                    .append("offset", offset));
    }

    private void ip4Handler(Ip4 ip) {
        String srcIp = FormatUtils.ip(ip.source());
        String dstIp = FormatUtils.ip(ip.destination());
        int type = ip.type();
        int length = ip.length();
        int ttl = ip.ttl();
        System.out.println("IP: " + srcIp + " " + dstIp + " " + type + " " + length + " " + ttl);
        doc.append("ip", new Document("src", srcIp)
                .append("dst", dstIp)
                .append("length", length)
                .append("offset", ttl));
    }

    private void arpHandler(Arp arp) {
        int operation = arp.operation();
        String srcMac = FormatUtils.mac(arp.sha());
        String srcIp = FormatUtils.ip(arp.spa());
        String dstMac = FormatUtils.mac(arp.tha());
        String dstIp = FormatUtils.ip(arp.tpa());
        System.out.printf("ARP: %d   %s %s %s %s\n", operation, srcMac, srcIp, dstMac, dstIp);
        doc.append("arp", new Document("srcMac", srcMac)
                .append("srcIp", srcIp)
                .append("dstMac", dstMac)
                .append("dstIp", dstIp));
    }

    private void icmpHandler(Icmp icmp) {
        int type = icmp.type();
        int code = icmp.code();
        System.out.printf("ICMP: type: %d code: %d\n", type, code);
        doc.append("icmp", new Document("type", type)
                .append("code", code));
    }

    private void tcpHandler(Tcp tcp) {
        String srcPort = String.valueOf(tcp.source());
        String dstPort = String.valueOf(tcp.destination());
        boolean ack = tcp.flags_ACK();
        boolean rst = tcp.flags_RST();
        boolean syn = tcp.flags_SYN();
        boolean fin = tcp.flags_FIN();
        System.out.printf("TCP: %s -> %s %b %b %b %b\n", srcPort, dstPort, ack, rst, syn, fin);
        doc.append("tcp", new Document("src", srcPort)
                .append("dst", dstPort)
                .append("ack", ack)
                .append("rst", rst)
                .append("syn", syn)
                .append("fin", fin));
    }

    private void udpHandler(Udp udp) {
        String srcPort = String.valueOf(udp.source());
        String dstPort = String.valueOf(udp.destination());
        int length = udp.length();
        System.out.printf("UDP: len: %d %s -> %s\n", length, srcPort, dstPort);
        doc.append("udp", new Document("src", srcPort)
                .append("dst", dstPort)
                .append("length", length));
    }
}
