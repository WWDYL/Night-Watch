package cn.turingmoon.utilities;

import cn.turingmoon.LocalStorage;
import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.result.UpdateResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;

public class MongoDbUtils {
    private static MongoDbUtils mongoDbUtils;

    private static Logger logger = LogManager.getLogger(MongoDbUtils.class.getName());

    private MongoCollection<Document> traffic;
    private MongoCollection<Document> flows;
    private MongoCollection<Document> tps;

    public static MongoDbUtils getInstance() {
        if (mongoDbUtils == null) {
            mongoDbUtils = new MongoDbUtils();
        }
        return mongoDbUtils;
    }

    /** 产生一个随机的字符串*/
    private static String RandomString(int length) {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int num = random.nextInt(62);
            buf.append(str.charAt(num));
        }
        return buf.toString();
    }

    private MongoDbUtils() {
        MongoClient mongoClient = new MongoClient();
        MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");

//        String now_time = Long.toString(new Date().getTime());
        String now_time = RandomString(5);
        String traffic_db_name = "traffic" + now_time;
        String flows_db_name = "flows" + now_time;
        String tp_db_name = "TP_" + now_time;

        logger.info("Traffic Collection Name: {}", traffic_db_name);
        logger.info("Flows Collection Name: {}", flows_db_name);
        logger.info("TP Collection Name: {}", tp_db_name);

        LocalStorage.TRAFFIC_DB = traffic_db_name;
        LocalStorage.FLOWS_DB = flows_db_name;

        mongoDatabase.createCollection(traffic_db_name);
        mongoDatabase.createCollection(flows_db_name);
        mongoDatabase.createCollection(tp_db_name);

        traffic = mongoDatabase.getCollection(traffic_db_name);
        flows = mongoDatabase.getCollection(flows_db_name);
        tps = mongoDatabase.getCollection(tp_db_name);
    }

    public void storeOneRecord(Document doc) {
        traffic.insertOne(doc);
    }

    public void storeSomeRecord(List<Document> docs) {
        flows.insertMany(docs);
    }

    public List<Document> getFlowRecords(Bson query) {
        List<Document> doc = flows.find(query).into(new ArrayList<Document>());
        return doc;
    }

    public DistinctIterable<String> getDistinctValues(String col, Bson filter) {
        // TODO: 按照时间排序
        DistinctIterable<String> res = flows.distinct(col, filter, String.class);
        return res;
    }

    public void storeTP(Document doc) {
        tps.insertOne(doc);
    }

    public void storeHasDetect(ObjectId id, int type) {
        String key;
        if (type == 1) {
            key = "FHDetect";
        } else {
            key = "TPDetect";
        }
        UpdateResult result = flows.updateOne(eq("_id", id), set(key, true));
    }

    long getCount() {
        return traffic.count();
    }
}
