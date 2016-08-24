package cn.turingmoon.utilities;

import com.mongodb.MongoClient;
import com.mongodb.client.DistinctIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
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
        MongoClient mongoClient = new MongoClient( "localhost" );
        MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");

        traffic = mongoDatabase.getCollection("traffic");
        flows = mongoDatabase.getCollection("flows");
    }

    public void storeOneRecord(Document doc) {
        traffic.insertOne(doc);
    }

    public void storeSomeRecord(List<Document> docs) {
        flows.insertMany(docs);
    }

    public List<Document> getFlowRecords(Bson query) {
        List<Document> doc = traffic.find(query).into(new ArrayList<Document>());
        return doc;
    }

    public DistinctIterable<String> getDistinctValues(String col) {
        DistinctIterable<String> res = flows.distinct(col, String.class);
        return res;
    }

    long getCount() {
        return traffic.count();
    }
}
