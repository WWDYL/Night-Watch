package cn.turingmoon.detectors;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.models.AttackRecord;
import cn.turingmoon.models.TrafficPattern;
import cn.turingmoon.utilities.RedisUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficPatternDetector {

    private Logger logger = LogManager.getLogger(TrafficPatternDetector.class);
    private ScheduledExecutorService scheduExec = null;
    private Jedis jedis = null;

    private int cycle = LocalStorage.CYCLE_TIME;

    public TrafficPatternDetector() {
        scheduExec = Executors.newScheduledThreadPool(1);
        jedis = RedisUtils.getInstance().getJedis();
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

    private boolean isScanning(TrafficPattern pattern, int type) {
        int v_n_flow = pattern.getFlow_num() / ScanningValue.t_n_flow;
        float v_l_flow = ScanningValue.t_l_flow / pattern.getFlow_size_avr();
        float v_n_packet = ScanningValue.t_n_packet / pattern.getPacket_num_avr();
        int v_ip = ScanningValue.t_ip / (type == 2 ? pattern.getSrcIP_num() : pattern.getDstIP_num());
        int v_port = pattern.getDstPort_num() / ScanningValue.t_port;

        double f_scan = v_n_flow * ScanningValue.w_n_flow +
                v_l_flow * ScanningValue.w_l_flow +
                v_n_packet * ScanningValue.w_n_packet +
                v_ip * ScanningValue.w_ip +
                v_port * ScanningValue.w_port;
        logger.info("Scan Function: {}", f_scan);
        return scanFuncIsLarge(f_scan);
    }

    private boolean scanFuncIsLarge(double f_scan) {
        return true;
    }

    private boolean isSYNflooding(TrafficPattern pattern) {
        int v_n_flow = pattern.getFlow_num() / SYNFloodingValue.t_n_flow;
        float v_l_flow = SYNFloodingValue.t_l_flow / pattern.getFlow_size_avr();
        float v_n_packet = SYNFloodingValue.t_n_packet / pattern.getPacket_num_avr();
        int v_port = pattern.getDstPort_num() / SYNFloodingValue.t_port;
        float v_syn_ack;
        if (pattern.getACK_num() != 0) {
            v_syn_ack = pattern.getSYN_num() / pattern.getACK_num();
        } else {
            v_syn_ack = 99999999;
        }

        double f_syn = v_n_flow * SYNFloodingValue.w_n_flow +
                v_l_flow * SYNFloodingValue.w_l_flow +
                v_n_packet * SYNFloodingValue.w_n_packet +
                v_port * SYNFloodingValue.w_port +
                v_syn_ack * SYNFloodingValue.w_syn_ack;
        logger.info("SYN Function: {}", f_syn);
        return SYNFuncisLarge(f_syn);
    }

    private boolean SYNFuncisLarge(double f_syn) {
        return true;
    }

    private void recordSrcAttack(String key, TrafficPattern tp, String type) {

        Long id = jedis.incr("tp_attack_src:");
        String attack_id = "tp_attack_src:" + id;
        jedis.hset(attack_id, "Description", type);

        jedis.hset(attack_id, "Attacker", key);

        jedis.hset(attack_id, "Protocol", tp.getProto());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jedis.hset(attack_id, "BeginTime", sdf.format(tp.getBeginTime()));
        jedis.hset(attack_id, "Duration", Long.toString(tp.getDuration()));

        jedis.hset(attack_id, "Flows/s", Float.toString(tp.getFlow_num() / (float) tp.getDuration()));
        jedis.hset(attack_id, "Packets/s", Float.toString(tp.getPacket_num_sum() / (float) tp.getDuration()));
        jedis.hset(attack_id, "Bytes/s", Float.toString(tp.getFlow_size_sum() / (float) tp.getDuration()));
    }

    private void recordDstAttack(String key, TrafficPattern tp, String type) {

        Long id = jedis.incr("tp_attack_dst:");
        String attack_id = "tp_attack_dst:" + id;
        jedis.hset(attack_id, "Description", type);

        jedis.hset(attack_id, "Victim", key);
        jedis.hset(attack_id, "Protocol", tp.getProto());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        jedis.hset(attack_id, "BeginTime", sdf.format(tp.getBeginTime()));
        jedis.hset(attack_id, "Duration", Long.toString(tp.getDuration()));

        jedis.hset(attack_id, "Flows/s", Float.toString(tp.getFlow_num() / (float) tp.getDuration()));
        jedis.hset(attack_id, "Packets/s", Float.toString(tp.getPacket_num_sum() / (float) tp.getDuration()));
        jedis.hset(attack_id, "Bytes/s", Float.toString(tp.getFlow_size_sum() / (float) tp.getDuration()));
    }

    public void detect(String key, TrafficPattern pattern, int type) {
        if (type == 2) {
            if (flowNumIsLarge(pattern.getFlow_num()) && flowSizeAvrIsSmall(pattern.getFlow_size_avr()) && packetNumAvgIsSmall(pattern.getPacket_num_avr())) {
                if (dstPortNumIsLarge(pattern.getDstPort_num()) && srcIpNumIsSmall(pattern.getSrcIP_num())) {
                    recordDstAttack(key, pattern, "host scanning");
                    if (isScanning(pattern, 2)) {
                        System.out.println("host scanning");
                        AttackRecorder.record(new AttackRecord(2, key, pattern, "host scanning"));
                    }
                }
                if (dstPortNumIsSmall(pattern.getDstPort_num()) && ACKDivSYNIsSmall(pattern.getACK_num(), pattern.getSYN_num())) {
                    System.out.println("TCP SYN flood");
                    recordDstAttack(key, pattern, "TCP SYN flood");
                    AttackRecorder.record(new AttackRecord(2, key, pattern, "TCP SYN flood"));
                }
            }
            if (packetNumSumIsLarge(pattern.getPacket_num_sum()) && flowSizeSumIsLarge(pattern.getFlow_size_sum())) {
                System.out.println("(ICMP, UDP, TCP) flooding");
                if (isSYNflooding(pattern)) {
                    System.out.println("SYN flooding");
                    AttackRecorder.record(new AttackRecord(2, key, pattern, "SYN flooding"));
                }
                recordDstAttack(key, pattern, "(ICMP UDP TCP) flooding");
            }
        } else {

            if (flowNumIsLarge(pattern.getFlow_num()) && flowSizeAvgIsSmall(pattern.getFlow_size_avr()) && packetNumAvgIsSmall(pattern.getPacket_num_avr())) {
                if (dstIpNumIsLarge(pattern.getDstIP_num()) && dstPortNumIsSmall(pattern.getDstPort_num())) {
                    recordSrcAttack(key, pattern, "network scanning");
                    if (isScanning(pattern, 1)) {
                        System.out.println("network scanning");
                        AttackRecorder.record(new AttackRecord(1, key, pattern, "network scanning"));
                    }
                }
            }

            if (packetNumSumIsLarge(pattern.getPacket_num_sum()) && flowSizeSumIsLarge(pattern.getFlow_size_sum())) {
                System.out.println("(ICMP, UDP, TCP) flooding");
                if (isSYNflooding(pattern)) {
                    System.out.println("SYN flooding");
                    AttackRecorder.record(new AttackRecord(1, key, pattern, "SYN flooding"));
                }
                recordSrcAttack(key, pattern, "(ICMP UDP TCP) flooding");
            }
        }
    }

    private boolean flowSizeSumIsLarge(int flow_size_sum) {
        logger.info("Flow Size Sum: {}", flow_size_sum);
        return true;
    }

    private boolean packetNumSumIsLarge(int packet_num_sum) {
        logger.info("Packet Num Sum: {}", packet_num_sum);
        return true;
    }

    private boolean dstIpNumIsLarge(int dstIP_num) {
        logger.info("Dst Ip Num: {}", dstIP_num);
        return true;
    }

    private boolean flowSizeAvgIsSmall(float flow_size_avr) {
        logger.info("Flow Size Avg: {}", flow_size_avr);
        return true;
    }

    private boolean ACKDivSYNIsSmall(int ack, int syn) {
        if (syn == 0) {
            logger.info("ACK / SYN: NO SYN");
            return false;
        }
        logger.info("ACK / SYN: {}", (float)ack / syn);
        return true;
    }

    private boolean dstPortNumIsSmall(int dstPort_num) {
        logger.info("Dst Port Num(S): {}", dstPort_num);
        return true;
    }

    private boolean srcIpNumIsSmall(int srcIP_num) {
        logger.info("Src Ip Num: {}", srcIP_num);
        return true;
    }

    private boolean dstPortNumIsLarge(int dstPort_num) {
        logger.info("Dst Port Num(L): {}", dstPort_num);
        return true;
    }

    private boolean packetNumAvgIsSmall(float packet_num_avr) {
        logger.info("Packet Num Avg: {}", packet_num_avr);
        return true;
    }

    private boolean flowNumIsLarge(int flow_num) {
        logger.info("Flow Num: {}", flow_num);
        return true;
    }

    private boolean flowSizeAvrIsSmall(float flow_size_avr) {
        logger.info("Flow Size Avg: {}", flow_size_avr);
        return true;
    }


    public void run() {
        scheduExec.scheduleWithFixedDelay(() -> {
            TrafficPatternDataGenerator generator = new TrafficPatternDataGenerator();
            generator.run();
            logger.info("Start Traffic Pattern Detection! {} {}", LocalStorage.source_based.size(), LocalStorage.destination_based.size());
            for (Map.Entry<String, TrafficPattern> item : LocalStorage.source_based.entrySet()) {
                detect(item.getKey(), item.getValue(), 1);
            }

            for (Map.Entry<String, TrafficPattern> item : LocalStorage.destination_based.entrySet()) {
                detect(item.getKey(), item.getValue(), 2);
            }

        }, 2 * cycle, cycle, TimeUnit.SECONDS);
    }
}
