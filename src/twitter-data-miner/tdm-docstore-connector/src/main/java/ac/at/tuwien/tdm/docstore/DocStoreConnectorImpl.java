package ac.at.tuwien.tdm.docstore;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import com.mongodb.AggregationOutput;
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
  private static final String SERVER = "localhost";
  private static final int PORT = 27017;
  private static final String TOPIC_FILE = "topics.json";
  private static final String ADS_FILE = "ads.json";
  private static final String TOPIC_COLLECTION = "topics";
  private static final String ADS_COLLECTION = "ads";
  private static final String USER_TWEET_COLLECTION = "user_tweets";
  private DB db;
  
  public void connect() {
    try {
      
      MongoClient mongoClient = new MongoClient(DocStoreConnectorImpl.SERVER, DocStoreConnectorImpl.PORT);
      this.db = mongoClient.getDB(DocStoreConnectorImpl.DB_NAME);
 
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
    try {
      BufferedReader br = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream("/" + file)));
      //List<DBObject> dbObjects = new ArrayList<DBObject>();
      String content = "";
      String contentLine = null;
      while ((contentLine = br.readLine()) != null) {
        content += contentLine;
        
        //dbObjects.add((DBObject)JSON.parse(contentLine));
      }
      //return dbObjects;
      br.close();
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
  
  public String getKeywordsForTopic(int id) {
    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
    BasicDBObject whereQuery = new BasicDBObject();
    whereQuery.put("id", id);
    DBCursor cursor = collection.find(whereQuery);
    while(cursor.hasNext()) {
        DBObject obj = cursor.next();
        System.out.println(obj.get("keywords"));
    }
    return null;
  }
  
  public void dropCollection(String collectionName) {
    DBCollection collection = this.db.getCollection(collectionName);
    collection.drop();
  }
  
  public void dropDatabase() {
    this.db.dropDatabase();
  }
  
  public void getInterestsForUsers(int interestThreshold) {
	  DBCollection coll = db.getCollection(USER_TWEET_COLLECTION);
	  DBObject groupFields = new BasicDBObject( "user", "$user");
	  groupFields.put("cnt", new BasicDBObject( "$sum", "$topic"));
	  DBObject group = new BasicDBObject("$group", groupFields);
	  DBObject match = new BasicDBObject("$match", new BasicDBObject("cnt", new BasicDBObject("$gt",interestThreshold)));
	  
	  List<DBObject> pipeline = Arrays.asList(group, match);
	  AggregationOutput output = coll.aggregate(pipeline);
  }
  
  public static void main(String[] args) {
    DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
    docstore.connect();
    //docstore.dropDatabase();
    docstore.createTopicCollection();
    docstore.createAdsCollection();
    
    docstore.retrieveAds();
    docstore.retrieveTopics();
    
    docstore.getKeywordsForTopic(2);
    
    // docstore.getTopicForID(2);
  }

}
