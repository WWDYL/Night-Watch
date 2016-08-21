package cn.turingmoon;

import cn.turingmoon.models.Flow;
import cn.turingmoon.utilities.MongoDbUtils;
import org.bson.Document;

import java.util.List;

public class FlowGenerator {
    private MongoDbUtils dbUtils = null;

    FlowGenerator() {
        dbUtils = MongoDbUtils.getInstance();
    }

    private void generate() {
        List<Document> traffics = dbUtils.getTrafficRecords();
        for (Document doc : traffics) {
            Document ip = (Document)doc.get("ip");
            if (ip == null) continue;
            Flow temp = new Flow();
            temp.setsIP(ip.getString("src"));
            temp.setdIP(ip.getString("dst"));
            Document ports = null;
            String type = null;
            if (doc.containsKey("tcp")) {
                ports = (Document) doc.get("tcp");
                type = "tcp";
            } else if (doc.containsKey("udp")) {
                ports = (Document) doc.get("udp");
                type = "udp";
            } else {
                type = Integer.toString(((Document)doc.get("icmp")).getInteger("type"));
            }
            if (ports != null) {
                temp.setsPort(ports.getString("src"));
                temp.setdPort(ports.getString("dst"));
            }
            temp.setType(type);
            if (matchRecentFlow(temp)) {
                /* TODO: 将已经存储的流信息更新，包括包的数量和流的总大小 */
            } else {
                System.out.println(temp);
                /* TODO: 存储为新的流 */
            }
        }
    }

    private boolean matchRecentFlow(Flow flow) {
        return false;
    }

    /* TODO: 添加定时存储功能。 */
    public static void main(String[] args) {
        FlowGenerator generator = new FlowGenerator();
        generator.generate();
    }
}
