import org.jnetpcap.packet.JPacket;
import org.jnetpcap.packet.format.FormatUtils;
import org.jnetpcap.protocol.lan.Ethernet;
import org.jnetpcap.protocol.network.Icmp;
import org.jnetpcap.protocol.network.Ip4;
import org.jnetpcap.protocol.tcpip.Http;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

/**
 * PacketMatcher 类
 * Created by BarryGates on 2016/5/10.
 */
public class PacketMatcher {

    private static PacketMatcher pm;
    private Ethernet ethernet = new Ethernet();
    private Ip4 ip = new Ip4();
    private Tcp tcp = new Tcp();
    private Udp udp = new Udp();
    private Icmp icmp = new Icmp();
    private Http http = new Http();


    public static PacketMatcher getInstance() {
        if (pm == null) {
            pm = new PacketMatcher();
        }
        return pm;
    }

    public void handlePacket(JPacket packet) {
        if (packet.hasHeader(ethernet)) {
            ethernetHandler(ethernet);
        }
        if (packet.hasHeader(ip)) {
            ip4Handler(ip);
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
        if (packet.hasHeader(http)) {
            httpHandler(http);
        }
    }

    private void ethernetHandler(Ethernet ethernet) {
        String srcMac = FormatUtils.mac(ethernet.source());
        String dstMac = FormatUtils.mac(ethernet.destination());
        System.out.println(srcMac + " " + dstMac);
    }

    private void ip4Handler(Ip4 ip) {
        String srcIp = FormatUtils.ip(ip.source());
        String dstIp = FormatUtils.ip(ip.destination());
        System.out.println(srcIp + " " + dstIp);
    }

    private void icmpHandler(Icmp icmp) {

    }

    private void tcpHandler(Tcp tcp) {
        String srcPort = String.valueOf(tcp.source());
    }

    private void udpHandler(Udp udp) {

    }

    private void httpHandler(Http http) {

    }
}
