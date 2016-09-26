package cn.turingmoon.detectors;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.constants.FlowType;
import cn.turingmoon.models.Flow;
import cn.turingmoon.models.TrafficPattern;
import cn.turingmoon.utilities.MongoDbUtils;
import com.mongodb.client.DistinctIterable;
import org.bson.Document;
import org.bson.types.ObjectId;

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
        String f_type = null;
        Map<String, Integer> ftype_stats = new HashMap<String, Integer>();
        int fs_sum = 0;
        float fs_avr;
        // float fs_dev = 0;
        int np_sum = 0;
        float np_avr;
        // float np_dev = 0;
        int syn_n = 0;
        int ack_n = 0;
        Date begin_time = null, end_time = null;
        long during_time;

        List<Document> docs = null;
        if (type == 1) {
            docs = utils.getFlowRecords(new Document("SrcIP", ip));
        } else if (type == 2) {
            docs = utils.getFlowRecords(new Document("DstIP", ip));
        }

        assert docs != null;
        for (Document doc : docs) {
            if (begin_time == null) {
                begin_time = doc.getDate("BeginTime");
            } else {
                begin_time = new Date(Math.min(begin_time.getTime(), doc.getDate("BeginTime").getTime()));
            }

            if (end_time == null) {
                end_time = doc.getDate("EndTime");
            } else {
                end_time = new Date(Math.max(end_time.getTime(), doc.getDate("EndTime").getTime()));
            }

            flow_num++;
            if (type == 1) {
                ips_stats.add(doc.getString("DstIP"));
            } else {
                ips_stats.add(doc.getString("SrcIP"));
            }
            dport_stats.add(doc.getString("DstPort"));
            sport_stats.add(doc.getString("SrcPort"));
            if (ftype_stats.containsKey(doc.getString("Type"))) {
                ftype_stats.put(doc.getString("Type"), ftype_stats.get(doc.getString("Type")));
            } else {
                ftype_stats.put(doc.getString("Type"), 1);
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


        during_time = (end_time.getTime() - begin_time.getTime()) / 1000;

        fs_avr = fs_sum / flow_num;

        np_avr = np_sum / flow_num;

        /* TODO: add deviation. */

        TrafficPattern pattern = new TrafficPattern();

        pattern.setBeginTime(begin_time);
        pattern.setDuration(during_time);

        pattern.setFlow_num(flow_num);

        ip_num = ips_stats.size();
        if (type == 1) {
            pattern.setDstIP_num(ip_num);
        } else {
            pattern.setSrcIP_num(ip_num);
        }

        dport_num = dport_stats.size();
        sport_num = sport_stats.size();
        pattern.setDstPort_num(dport_num);
        pattern.setSrcPort_num(sport_num);

        int val = -1;
        for (Map.Entry<String, Integer> entry : ftype_stats.entrySet()) {
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

    private void HasDetect(ObjectId id) {

        // LocalStorage.source_based.put(ip, pattern);
    }

    public void run() {

        // DistinctIterable<String> srcIPs = utils.getDistinctValues("SrcIP", new Document("TPDetect", false));
        // DistinctIterable<String> dstIPs = utils.getDistinctValues("DstIP", new Document("TPDetect", false));
        HashSet<String> srcIPs = new HashSet<>(),
                dstIPs = new HashSet<>();

        List<Document> documents = utils.getFlowRecords(new Document("TPDetect", false));
        for (Document doc : documents) {
            Flow flow = Flow.parseDocument(doc);
            HasDetect(doc.getObjectId("_id"));
            srcIPs.add(flow.getsIP());
            dstIPs.add(flow.getdIP());
        }

        for (String ip : srcIPs) {
            TrafficPattern pattern = generatePattern(ip, 1);
            LocalStorage.source_based.put(ip, pattern);
        }

        for (String ip : dstIPs) {
            TrafficPattern pattern = generatePattern(ip, 2);
            LocalStorage.destination_based.put(ip, pattern);
        }
    }

}
