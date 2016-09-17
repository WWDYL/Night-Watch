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

    private int cycle = LocalStorage.CYCLE_TIME;

    public FlowStore() {
        this.scheduExec = Executors.newScheduledThreadPool(2);
    }

    public void run() {
        scheduExec.scheduleWithFixedDelay(new Runnable() {
            public void run() {
                System.err.println("Storing...");
                List<Document> docs = new ArrayList<Document>();
                for (Flow item : LocalStorage.tempFlows) {
                    Document temp = Flow.toDocument(item);
                    docs.add(temp);
                }
                System.err.println(docs.size());
                MongoDbUtils dbUtils = MongoDbUtils.getInstance();
                if (docs.isEmpty()){
                    System.out.println("EMPTY");
                    return;
                }
                dbUtils.storeSomeRecord(docs);
                LocalStorage.tempFlows.clear();
            }
        }, cycle, cycle, TimeUnit.SECONDS);
    }
}
