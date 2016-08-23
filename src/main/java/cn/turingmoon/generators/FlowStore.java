package cn.turingmoon.generators;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.models.Flow;
import cn.turingmoon.utilities.MongoDbUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlowStore {

    private ScheduledExecutorService scheduExec;

    FlowStore() {
        this.scheduExec = Executors.newScheduledThreadPool(2);
    }

    public void run() {
        scheduExec.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                System.err.println("Storing...");
                List<Document> docs = new ArrayList<Document>();
                for (Flow item : LocalStorage.tempFlows) {
                    Document temp = new Document()
                            .append("BeginTime", item.getbTime())
                            .append("EndTime", item.geteTime())
                            .append("SrcIP", item.getsIP())
                            .append("DstIP", item.getdIP())
                            .append("SrcPort", item.getsPort())
                            .append("DstPort", item.getdPort())
                            .append("Type", item.getType())
                            .append("PacketNum", item.getpNum())
                            .append("PacketSize", item.getpSize());
                    docs.add(temp);
                }
                MongoDbUtils dbUtils = MongoDbUtils.getInstance();
                if (docs.isEmpty()) return;
                dbUtils.storeSomeRecord(docs);
                LocalStorage.tempFlows.clear();
            }
        }, 60000, 60000, TimeUnit.MILLISECONDS);
    }
}
