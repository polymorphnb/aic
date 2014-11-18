package ac.at.tuwien.tdm.docstore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;


public class DocStoreConnectorImpl {
  
  private static final String DB_NAME = "AIC";
  private static final String TOPIC_FILE = "topics.json";
  private static final String ADS_FILE = "ads.json";
  private static final String TOPIC_COLLECTION = "topics";
  private static final String ADS_COLLECTION = "ads";
  private DB db;
  
  public void connect() {
    try {
      
      
      MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
      this.db = mongoClient.getDB(DB_NAME);
 
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch (MongoException e) {
      e.printStackTrace();
    }
  }
  
  public void createTopicCollection() {
    if(this.db.collectionExists(DocStoreConnectorImpl.TOPIC_COLLECTION) == false) {
      DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
      // List<DBObject> dbobjects = this.getContentFromFile(DocStoreConnectorImpl.TOPIC_FILE);
      String content = this.getContentFromFile(DocStoreConnectorImpl.TOPIC_FILE);
      
      DBObject obj = (DBObject)JSON.parse(content);
      for(String key : obj.keySet()) {
        collection.insert((DBObject)(obj.get(key)));
      }
    }
  }
  
  public void createAdsCollection() {
    if(this.db.collectionExists(DocStoreConnectorImpl.ADS_COLLECTION) == false) {
      DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.ADS_COLLECTION);
      String content = this.getContentFromFile(DocStoreConnectorImpl.ADS_FILE);
      
      DBObject obj = (DBObject)JSON.parse(content);
      for(String key : obj.keySet()) {
        collection.insert((DBObject)(obj.get(key)));
      }
    }
  }
  
  private String getContentFromFile(String file) {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + file)))) {
      //List<DBObject> dbObjects = new ArrayList<DBObject>();
      String content = "";
      String contentLine = null;
      while ((contentLine = br.readLine()) != null) {
        content += contentLine;
        
        //dbObjects.add((DBObject)JSON.parse(contentLine));
      }
      //return dbObjects;
      return content;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }
  
  public void retrieveTopics() {
    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
    DBCursor cursorDoc = collection.find();
    while (cursorDoc.hasNext()) {
      System.out.println(cursorDoc.next());
    }
  }
  
  public void retrieveAds() {
    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.ADS_COLLECTION);
    DBCursor cursorDoc = collection.find();
    while (cursorDoc.hasNext()) {
      System.out.println(cursorDoc.next());
    }
  }
  
  public void getTopicForID(int id) {
    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
    BasicDBObject whereQuery = new BasicDBObject();
    whereQuery.put("id", id);
    DBCursor cursor = collection.find(whereQuery);
    while(cursor.hasNext()) {
        System.out.println(cursor.next());
    }
  }
  
  public void dropCollection(String collectionName) {
    DBCollection collection = this.db.getCollection(collectionName);
    collection.drop();
  }
  
  public void dropDatabase() {
    this.db.dropDatabase();
  }
  
  public static void main(String[] args) {
    DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
    docstore.connect();
    //docstore.dropDatabase();
//    docstore.createTopicCollection();
//    docstore.createAdsCollection();
//    
//    docstore.retrieveAds();
//    docstore.retrieveTopics();
    
    docstore.getTopicForID(2);
  }

}
