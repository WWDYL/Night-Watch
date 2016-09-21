package cn.turingmoon.generators;

import cn.turingmoon.LocalStorage;
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class PacketCapturer {

    private String filename = null;

    private class PacketHandler implements PcapPacketHandler<String> {
        public void nextPacket(PcapPacket packet, String user) {
            FlowGenerator generator = new FlowGenerator();
            generator.handlePacket(packet);
        }
    }

    public PacketCapturer(String filename) {
        this.filename = filename;
    }

    public void start() {
        StringBuilder errBuf = new StringBuilder();

        PcapBpfProgram program = new PcapBpfProgram();
        String bpf = "ip";
        int optimize = 0;
        int netmask = 0xFFFFFF00;

        int snaplen = 64 * 1024;
        int flags = Pcap.MODE_PROMISCUOUS;
        int timeout = 10 * 1000;

        Pcap pcap;
        if (filename == null) {
            List<PcapIf> pcapIfs = new ArrayList<PcapIf>();
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

//            LocalStorage.BroadcastAddr = dev.getAddresses().get(0).getBroadaddr().toString();
            System.out.println(LocalStorage.BroadcastAddr);
            pcap = Pcap.openLive(dev.getName(), snaplen, flags, timeout, errBuf);
        } else {
            pcap = Pcap.openOffline(filename, errBuf);
        }

        if (pcap == null) {
            System.err.printf("Error: %s\n", errBuf.toString());
            System.exit(1);
        }

        if (pcap.compile(program, bpf, optimize, netmask) != Pcap.OK) {
            System.err.println(pcap.getErr());
            System.exit(1);
        }
        pcap.setFilter(program);

        pcap.loop(-1, new PacketHandler(), null);
        pcap.close();
    }
}
