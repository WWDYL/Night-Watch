package cn.turingmoon.utilities;

import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MongoDbUtils {
    private static MongoDbUtils mongoDbUtils;

    private MongoCollection<Document> traffic;
    private MongoCollection<Document> flows;

    public static MongoDbUtils getInstance() {
        if (mongoDbUtils == null) {
            mongoDbUtils = new MongoDbUtils();
        }
        return mongoDbUtils;
    }

    private MongoDbUtils() {
        MongoClient mongoClient = new MongoClient("localhost");
        MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");

        String now_time = Long.toString(new Date().getTime());
        String traffic_db_name = "traffic" + now_time;
        String flows_db_name = "flows" + now_time;

        mongoDatabase.createCollection(traffic_db_name);
        mongoDatabase.createCollection(flows_db_name);

        traffic = mongoDatabase.getCollection(traffic_db_name);
        flows = mongoDatabase.getCollection(flows_db_name);
    }

    public void storeOneRecord(Document doc) {
        traffic.insertOne(doc);
    }

    public void storeSomeRecord(List<Document> docs) {
        flows.insertMany(docs);
        System.out.println(flows.count());
    }

    public List<Document> getFlowRecords(Bson query) {
        List<Document> doc = flows.find(query).into(new ArrayList<Document>());
        return doc;
    }

    public DistinctIterable<String> getDistinctValues(String col) {
        // TODO: 按照时间排序
        DistinctIterable<String> res = flows.distinct(col, String.class);
        return res;
    }

    long getCount() {
        return traffic.count();
    }
}
