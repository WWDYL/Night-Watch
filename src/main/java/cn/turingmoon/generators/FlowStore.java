package cn.turingmoon.generators;

import cn.turingmoon.LocalStorage;
import cn.turingmoon.models.Flow;
import cn.turingmoon.utilities.MongoDbUtils;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FlowStore {

    private Logger logger = LoggerFactory.getLogger(FlowStore.class);

    private ScheduledExecutorService scheduExec;

    private int cycle = LocalStorage.CYCLE_TIME;

    public FlowStore() {
        this.scheduExec = Executors.newScheduledThreadPool(1);
    }

    public void run() {
        scheduExec.scheduleWithFixedDelay(() -> {
                List<Document> docs = new ArrayList<>();
                for (Flow item : LocalStorage.tempFlows) {
                    Document temp = Flow.toDocument(item);
                    docs.add(temp);
                }
                logger.info("Flows Store .. {} records", docs.size());
                MongoDbUtils dbUtils = MongoDbUtils.getInstance();
                if (docs.isEmpty()) {
                    return;
                }
                dbUtils.storeSomeRecord(docs);
                LocalStorage.tempFlows.clear();
        }, cycle, cycle, TimeUnit.SECONDS);
    }
}
