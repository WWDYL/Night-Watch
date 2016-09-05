package cn.turingmoon.generators;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PacketCapturer implements PcapPacketHandler<String> {

    public void nextPacket(PcapPacket packet, String user) {
        FlowGenerator generator = new FlowGenerator();
        generator.handlePacket(packet);
    }

    public void start() {
        List<PcapIf> pcapIfs = new ArrayList<PcapIf>();
        StringBuilder errBuf = new StringBuilder();
        Pcap.findAllDevs(pcapIfs, errBuf);
        if (pcapIfs.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s\n", errBuf.toString());
            // TODO: Modify this line to another safe method to exit.
            System.exit(1);
        }
        int i = 0;
        for (PcapIf pcapIf : pcapIfs) {
            System.out.printf("%d: %s %s\n", i++, pcapIf.getAddresses().get(0).getAddr(), pcapIf.getDescription());
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input the number of the NIC: ");
        int num = scanner.nextInt();
        PcapIf dev = pcapIfs.get(num);

        PcapBpfProgram program = new PcapBpfProgram();
        String bpf = "ip";
        int optimize = 0;
        int netmask = 0xFFFFFF00;

        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;
        Pcap pcap = Pcap.openLive(dev.getName(), snaplen, flags, timeout, errBuf);


        if (pcap == null) {
            System.err.printf("Error while opening device for capture: %s\n", errBuf.toString());
            return;
        }

        if (pcap.compile(program, bpf, optimize, netmask) != Pcap.OK) {
            System.err.println(pcap.getErr());
            System.exit(1);
        }
        pcap.setFilter(program);

        pcap.loop(-1, new PacketCapturer(), null);
        pcap.close();
    }
}
