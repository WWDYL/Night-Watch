package cn.turingmoon.detectors;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.models.TrafficPattern;
import cn.turingmoon.utilities.RedisUtils;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficPatternDetector {

    private ScheduledExecutorService scheduExec = null;

    public TrafficPatternDetector() {
        scheduExec = Executors.newScheduledThreadPool(2);
    }

    private static class ScanningValue {
        private static double w_n_flow = 0.3;
        private static double w_l_flow = 0.1;
        private static double w_n_packet = 0.2;
        private static double w_ip = 0.1;
        private static double w_port = 0.3;

        private static int t_n_flow = 1024;
        private static int t_l_flow = 128;
        private static int t_n_packet = 2;
        private static int t_ip = 3;
        private static int t_port = 1024;
    }

    private static class SYNFloodingValue {
        private static double w_n_flow = 0.2;
        private static double w_l_flow = 0.1;
        private static double w_n_packet = 0.1;
        private static double w_port = 0.1;
        private static double w_syn_ack = 0.5;

        private static int t_n_flow = 3500;
        private static int t_l_flow = 64;
        private static int t_n_packet = 1;
        private static int t_port = 1;
    }

    private boolean isLarge(int num) {
        return true;
    }

    private boolean isLarge(float num) {
        return true;
    }

    private boolean isLarge(double num) { return true; }

    private boolean isSmall(int num) {
        return true;
    }

    private boolean isSmall(float num) {
        return true;
    }

    private boolean isScanning(TrafficPattern pattern) {
        int v_n_flow = pattern.getFlow_num() / ScanningValue.t_n_flow;
        float v_l_flow = ScanningValue.t_l_flow / pattern.getFlow_size_avr();
        float v_n_packet = ScanningValue.t_n_packet / pattern.getPacket_num_avr();
        int v_ip = ScanningValue.t_ip / pattern.getSrcIP_num();
        int v_port = pattern.getDstPort_num() / ScanningValue.t_port;

        double f_scan = v_n_flow * ScanningValue.w_n_flow +
                        v_l_flow * ScanningValue.w_l_flow +
                        v_n_packet * ScanningValue.w_n_packet +
                        v_ip * ScanningValue.w_ip +
                        v_port * ScanningValue.w_port;
        return isLarge(f_scan);
    }

    private boolean isSYNflooding(TrafficPattern pattern) {
        int v_n_flow = pattern.getFlow_num() / SYNFloodingValue.t_n_flow;
        float v_l_flow = SYNFloodingValue.t_l_flow / pattern.getFlow_size_avr();
        float v_n_packet = SYNFloodingValue.t_n_packet / pattern.getPacket_num_avr();
        int v_port = pattern.getDstPort_num() / SYNFloodingValue.t_port;
        float v_syn_ack = pattern.getSYN_num() / pattern.getACK_num();

        double f_syn = v_n_flow * SYNFloodingValue.w_n_flow +
                v_l_flow * SYNFloodingValue.w_l_flow +
                v_n_packet * SYNFloodingValue.w_n_packet +
                v_port * SYNFloodingValue.w_port +
                v_syn_ack * SYNFloodingValue.w_syn_ack;
        return isLarge(f_syn);
    }

    public void recordAttackType(String type) {
        RedisUtils utils = RedisUtils.getInstance();
        Jedis jedis = utils.getJedis();
        Long id = jedis.incr("attack:");
        jedis.hset("attack:"+id, "Type", type);
    }

    public void detect(TrafficPattern pattern, int type) {
        if (type == 1) {
            if (isLarge(pattern.getFlow_num()) && isSmall(pattern.getFlow_size_avr()) && isSmall(pattern.getPacket_num_avr())) {
                if (isLarge(pattern.getDstPort_num()) && isSmall(pattern.getSrcIP_num())) {
                    System.out.println("host scanning");
                    recordAttackType("host scanning");
                }
                if (isSmall(pattern.getDstPort_num()) && isSmall(pattern.getACK_num() / pattern.getSYN_num())) {
                    System.out.println("TCP SYN flood");
                    recordAttackType("TCP SYN flood");
                }
            }
            if (isLarge(pattern.getPacket_num_sum()) && isLarge(pattern.getFlow_size_sum())) {
                System.out.println("(ICMP, UDP, TCP) flooding");
                recordAttackType("(ICMP UDP TCP) flooding");
            }
        } else if (type == 2) {
            if (isLarge(pattern.getFlow_num()) && isSmall(pattern.getFlow_size_avr()) && isSmall(pattern.getPacket_num_avr())) {
                if (isLarge(pattern.getDstIP_num()) && isSmall(pattern.getDstPort_num())) {
                    System.out.println("network scanning");
                    recordAttackType("network scanning");
                }
            }
            if (isLarge(pattern.getPacket_num_sum()) && isLarge(pattern.getFlow_size_sum())) {
                System.out.println("(ICMP, UDP, TCP) flooding");
                recordAttackType("(ICMP UDP TCP) flooding");
            }
        }
    }

    public void run() {
        scheduExec.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                TrafficPatternDataGenerator generator = new TrafficPatternDataGenerator();
                generator.run();
                System.out.printf("Start Traffic Pattern Detection! %d %d \n", LocalStorage.source_based.size(), LocalStorage.destination_based.size());
                for (Map.Entry<String, TrafficPattern> item : LocalStorage.source_based.entrySet()) {
                    detect(item.getValue(), 1);
                }

                for (Map.Entry<String, TrafficPattern> item : LocalStorage.destination_based.entrySet()) {
                    detect(item.getValue(), 2);
                }

            }
        }, 60000, 60000, TimeUnit.MILLISECONDS);
    }
}
