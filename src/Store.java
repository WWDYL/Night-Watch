import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader;
import org.bson.Document;

import java.util.List;


/**
 * Store 类
 * Created by jiany on 2016/5/11.
 */
class Store {
    private static Store store;


    private MongoCollection<Document> traffic;

    static Store getInstance() {
        if (store == null) {
            store = new Store();
        }
        return store;
    }

    private Store() {
        MongoClient mongoClient = new MongoClient( "localhost" );
        MongoDatabase mongoDatabase = mongoClient.getDatabase("mydb");

        traffic = mongoDatabase.getCollection("traffic");
    }

    void storeOneRecord(Document doc) {
        traffic.insertOne(doc);
    }

    void storeSomeRecord(List<Document> docs) {
        traffic.insertMany(docs);
    }

    long getCount() {
        return traffic.count();
    }
}
