package cn.turingmoon.detectors;

import cn.turingmoon.constants.FlowType;
import cn.turingmoon.models.TrafficPattern;
import cn.turingmoon.utilities.MongoDbUtils;
import com.mongodb.client.DistinctIterable;
import org.bson.Document;

import java.util.*;

public class TrafficPatternDataGenerator {
    private MongoDbUtils utils = MongoDbUtils.getInstance();

    private TrafficPattern generatePattern(String ip, int type) {
        int flow_num = 0;
        int ip_num = 0;
        Set<String> ips_stats = new HashSet<String>();
        int dport_num = 0;
        Set<String> dport_stats = new HashSet<String>();
        int sport_num = 0;
        Set<String> sport_stats = new HashSet<String>();
        FlowType f_type = null;
        Map<FlowType, Integer> ftype_stats = new HashMap<FlowType, Integer>();
        int fs_sum = 0;
        float fs_avr;
        // float fs_dev = 0;
        int np_sum = 0;
        float np_avr;
        // float np_dev = 0;
        int syn_n = 0;
        int ack_n = 0;

        List<Document> docs = null;
        if (type == 1) {
            docs = utils.getFlowRecords(new Document("SrcIP", ip));
        } else if (type == 2) {
            docs = utils.getFlowRecords(new Document("DstIP", ip));
        }

        for (Document doc : docs) {
            flow_num++;
            if (type == 1) {
                ips_stats.add(doc.getString("DstIP"));
            } else if (type == 2) {
                ips_stats.add(doc.getString("SrcIP"));
            }
            dport_stats.add(doc.getString("DstPort"));
            sport_stats.add(doc.getString("SrcPort"));
            if (ftype_stats.containsKey((FlowType)doc.get("Type"))) {
                ftype_stats.put((FlowType) doc.get("Type"), ftype_stats.get(doc.get("Type")));
            } else {
                ftype_stats.put((FlowType) doc.get("Type"), 1);
            }
            fs_sum += doc.getInteger("PacketSize");
            np_sum += doc.getInteger("PacketNum");

            if (doc.getString("Type").equals(FlowType.SYN)) {
                syn_n++;
            }

            if (doc.getString("Type").equals(FlowType.ACK)) {
                ack_n++;
            }
        }
        fs_avr = fs_sum / flow_num;

        np_avr = np_sum / flow_num;

        /* TODO: add deviation. */

        TrafficPattern pattern = new TrafficPattern();
        pattern.setFlow_num(flow_num);

        ip_num = ips_stats.size();
        if (type == 1) {
            pattern.setDstIP_num(ip_num);
        } else if (type == 2) {
            pattern.setSrcIP_num(ip_num);
        }

        dport_num = dport_stats.size();
        sport_num = sport_stats.size();
        pattern.setDstPort_num(dport_num);
        pattern.setSrcPort_num(sport_num);

        int val = -1;
        for (Map.Entry<FlowType, Integer> entry : ftype_stats.entrySet()) {
            if (entry.getValue() > val) {
                f_type = entry.getKey();
                val = entry.getValue();
            }
        }
        pattern.setProto(f_type);

        pattern.setFlow_size_sum(fs_sum);
        pattern.setFlow_size_avr(fs_avr);

        pattern.setPacket_num_sum(np_sum);
        pattern.setPacket_num_avr(np_avr);

        pattern.setACK_num(ack_n);
        pattern.setSYN_num(syn_n);
        return pattern;
    }

    private void storeIntoDb(TrafficPattern pattern) {
        MongoDbUtils utils = MongoDbUtils.getInstance();

    }

    public void run() {
        DistinctIterable<String> srcIPs = utils.getDistinctValues("SrcIP");
        DistinctIterable<String> dstIPs = utils.getDistinctValues("DstIP");
        for (String ip : srcIPs) {
            TrafficPattern pattern = generatePattern(ip, 1);
            storeIntoDb(pattern);
        }
        for (String ip : dstIPs) {
            TrafficPattern pattern = generatePattern(ip, 2);
            storeIntoDb(pattern);
        }
    }

    public static void main(String[] args) {
        TrafficPatternDataGenerator generator = new TrafficPatternDataGenerator();
        generator.run();
    }
}
