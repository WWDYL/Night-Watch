package cn.turingmoon.generators;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.models.Flow;
import cn.turingmoon.utilities.MongoDbUtils;
import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.network.Ip6;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

import java.util.*;

public class FlowGenerator {

    private static FlowGenerator generator = null;

    private Flow temp = null;

    private Ip4 ip4 = new Ip4();
    private Ip6 ip6 = new Ip6();
    private Tcp tcp = new Tcp();
    private Udp udp = new Udp();
    private Icmp icmp = new Icmp();

    public static FlowGenerator getInstance() {
        if (generator == null) {
            generator = new FlowGenerator();
        }
        return generator;
    }

    public void handlePacket(JPacket packet) {
        temp = new Flow();
        if (packet.hasHeader(ip4)) {
            ip4Handler(ip4);
        } else if (packet.hasHeader(ip6)) {
            ip6Handler(ip6);
        } else {
            return;
        }
        if (packet.hasHeader(tcp)) {
            tcpHandler(tcp);
        } else if (packet.hasHeader(udp)) {
            udpHandler(udp);
        } else if (packet.hasHeader(icmp)) {
            icmpHandler(icmp);
        } else {
            return;
        }

        Flow match = matchRecentFlow();
        if (match != null) {
            match.seteTime(new Date(packet.getCaptureHeader().timestampInMillis()));
            match.setpNum(match.getpNum() + 1);
            match.setpSize(match.getpSize() + packet.getTotalSize());
        } else {
            temp.setbTime(new Date(packet.getCaptureHeader().timestampInMillis()));
            temp.seteTime(new Date(packet.getCaptureHeader().timestampInMillis()));
            temp.setpNum(1);
            temp.setpSize(packet.getTotalSize());
            LocalStorage.tempFlows.add(temp);
        }
    }

    private Flow matchRecentFlow() {
        for (Flow item : LocalStorage.tempFlows) {
            if (item.equals(temp)) {
                return item;
            }
        }
        return null;
    }

    private void ip4Handler(Ip4 ip) {
        String srcIp = FormatUtils.ip(ip.source());
        String dstIp = FormatUtils.ip(ip.destination());
//       System.out.println("IP: " + srcIp + " " + dstIp + " ");
        temp.setsIP(srcIp);
        temp.setdIP(dstIp);
    }

    private void ip6Handler(Ip6 ip) {
        String srcIp = FormatUtils.ip(ip.source());
        String dstIp = FormatUtils.ip(ip.destination());
//        System.out.println("IPv6: " + srcIp + " " + dstIp);
        temp.setsIP(srcIp);
        temp.setdIP(dstIp);
    }


    private void icmpHandler(Icmp icmp) {
        int type = icmp.type();
        int code = icmp.code();
//        System.out.printf("ICMP: type: %d code: %d\n", type, code);
        temp.setType(Integer.toString(type));
    }

    private void tcpHandler(Tcp tcp) {
        String srcPort = String.valueOf(tcp.source());
        String dstPort = String.valueOf(tcp.destination());
        boolean ack = tcp.flags_ACK();
        boolean rst = tcp.flags_RST();
        boolean syn = tcp.flags_SYN();
        boolean fin = tcp.flags_FIN();
//        System.out.printf("TCP: %s -> %s %b %b %b %b\n", srcPort, dstPort, ack, rst, syn, fin);
        temp.setsPort(srcPort);
        temp.setdPort(dstPort);
        temp.setType("TCP");
    }

    private void udpHandler(Udp udp) {
        String srcPort = String.valueOf(udp.source());
        String dstPort = String.valueOf(udp.destination());
        int length = udp.length();
//        System.out.printf("UDP: len: %d %s -> %s\n", length, srcPort, dstPort);
        temp.setsPort(srcPort);
        temp.setdPort(dstPort);
        temp.setType("UDP");
    }

}
