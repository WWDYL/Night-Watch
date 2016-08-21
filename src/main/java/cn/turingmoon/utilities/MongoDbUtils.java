package cn.turingmoon.utilities;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;


/**
 * cn.turingmoon.utilities.MongoDbUtils 类
 * Created by jiany on 2016/5/11.
 */
public class MongoDbUtils {
    private static MongoDbUtils mongoDbUtils;

    private MongoCollection<Document> traffic;

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
    }

    public void storeOneRecord(Document doc) {
        traffic.insertOne(doc);
    }

    public void storeSomeRecord(List<Document> docs) {
        traffic.insertMany(docs);
    }

    public List<Document> getTrafficRecords() {
        List<Document> doc = traffic.find().into(new ArrayList<Document>());
        return doc;
    }

    long getCount() {
        return traffic.count();
    }
}
