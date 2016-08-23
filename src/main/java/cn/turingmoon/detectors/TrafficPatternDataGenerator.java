package cn.turingmoon.detectors;

import cn.turingmoon.models.Flow;
import cn.turingmoon.models.TrafficPattern;
import cn.turingmoon.utilities.MongoDbUtils;
import com.mongodb.client.DistinctIterable;

import java.util.List;

public class TrafficPatternDataGenerator {
    private MongoDbUtils utils = MongoDbUtils.getInstance();

    private List<Flow> readFlowInfos(String ip) {
        return null;
    }

    private TrafficPattern generatePattern(List<Flow> flows) {
        return null;
    }

    private void storeIntoDb(TrafficPattern pattern) {

    }

    public void run() {
        DistinctIterable<String> srcIPs = utils.getDistinctValues("SrcIP");
        DistinctIterable<String> dstIPs = utils.getDistinctValues("DstIP");
        for (String ip : srcIPs) {
            List<Flow> flows = readFlowInfos(ip);
            TrafficPattern pattern = generatePattern(flows);
            storeIntoDb(pattern);
        }
        for (String ip : dstIPs) {
            List<Flow> flows = readFlowInfos(ip);
            TrafficPattern pattern = generatePattern(flows);
            storeIntoDb(pattern);
        }
    }

    public static void main(String[] args) {
        TrafficPatternDataGenerator generator = new TrafficPatternDataGenerator();
        generator.run();
    }
}
