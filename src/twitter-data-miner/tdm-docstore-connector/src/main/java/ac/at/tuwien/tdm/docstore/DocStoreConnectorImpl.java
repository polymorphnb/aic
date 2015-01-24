package ac.at.tuwien.tdm.docstore;

import ac.at.tuwien.tdm.commons.pojo.Ad;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.util.JSON;


public class DocStoreConnectorImpl implements DocStoreConnector {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(DocStoreConnectorImpl.class);
  
  private static final String DB_NAME = "AIC";
  private static final String SERVER = "localhost";
  private static final int PORT = 27017;
  private static final String TOPIC_FILE = "topics.json";
  private static final String ADS_FILE = "ads.json";
  private static final String TOPIC_COLLECTION = "topics";
  private static final String ADS_COLLECTION = "ads";
  private static final String USER_TWEET_COLLECTION = "user_tweets";
  private DB db;
  
  
  public DocStoreConnectorImpl() {
    this.connect();
    this.createTopicCollection();
    this.createAdsCollection();
  }
  
  public void connect() {
    try {
      
      MongoClient mongoClient = new MongoClient(DocStoreConnectorImpl.SERVER, DocStoreConnectorImpl.PORT);
      this.db = mongoClient.getDB(DocStoreConnectorImpl.DB_NAME);
 
    } catch (UnknownHostException e) {
      LOGGER.error("Could not connect to mongoDB server: " + e.getMessage());
    } catch (MongoException e) {
      LOGGER.error("Could not connect to mongoDB server: " + e.getMessage());
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
  
  public void createUserTweetCollection() {
	    if(this.db.collectionExists(DocStoreConnectorImpl.USER_TWEET_COLLECTION) == false) {
	      this.db.getCollection(DocStoreConnectorImpl.USER_TWEET_COLLECTION);
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
      LOGGER.error("Could not load content from File '%s': " + e.getMessage(), file);
    }
    return null;
  }
  
  public List<Long> retrieveTopics() {
    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
    DBCursor cursorDoc = collection.find();
    List<Long> topicIDs = new ArrayList<Long>();
    while (cursorDoc.hasNext()) {
      topicIDs.add(Long.valueOf(cursorDoc.next().get("id").toString()));
    }
    return topicIDs;
  }
  
  public List<Ad> retrieveAds() {
	LinkedList<Ad> ret = new LinkedList<Ad>();
    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.ADS_COLLECTION);
    DBCursor cursorDoc = collection.find();
    while (cursorDoc.hasNext()) {
      DBObject o = cursorDoc.next();
      ret.addFirst(new Ad(((Integer)o.get("id")).intValue(),
    		  			  ((Integer)o.get("topic_id")).intValue(), 
    		  			  o.get("name").toString(), 
    		  			  o.get("content").toString()
      ));
      //System.out.println(cursorDoc.next());
    }
    
    return ret;
  }
  
  public Long getTopicIDForKeyword(String keyword) {
    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
    BasicDBObject whereQuery = new BasicDBObject();
    whereQuery.put("keywords.keyword", keyword);
    DBCursor cursor = collection.find(whereQuery);
    Long resultID = -1L;
    while(cursor.hasNext()) {
      resultID = Long.valueOf(cursor.next().get("id").toString());
    }
    return resultID;
  }
  
  public String getTopicForID(Long id) {
	    DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
	    BasicDBObject whereQuery = new BasicDBObject();
	    whereQuery.put("id", id);
	    DBCursor cursor = collection.find(whereQuery);
	    return (String)cursor.next().get("Topic");
  }
  
  public void addTopicToUser(String user, String topic) {
	  DBCollection coll = db.getCollection(USER_TWEET_COLLECTION);
	  coll.insert(new BasicDBObjectBuilder().add("user",user).add("topic",topic).get());
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
  /*
  public void getInterestsForUsers(int interestThreshold, Neo4JConnector neo4jdb) {
	  DBCollection coll = db.getCollection(USER_TWEET_COLLECTION);
	  
	  Map<String, Object> dbObjIdMap = new HashMap <String, Object>();
	  dbObjIdMap.put("user", "$user");
	  dbObjIdMap.put("topic", "$topic");
	  
	  DBObject groupFields = new BasicDBObject( "_id", new BasicDBObject(dbObjIdMap));
	  groupFields.put("cnt", new BasicDBObject( "$sum", 1));
	  DBObject group = new BasicDBObject("$group", groupFields);
	  DBObject match = new BasicDBObject("$match", new BasicDBObject("cnt", new BasicDBObject("$gte",interestThreshold)));
	  
	  List<DBObject> pipeline = Arrays.asList(group,match);
	  AggregationOutput output = coll.aggregate(pipeline);
	  
	  for(DBObject result : output.results()) {
		  neo4jdb.addInterestedInRelationship(((Long) ((DBObject)result.get("_id")).get("user")), 
				                              ((Long) ((DBObject)result.get("_id")).get("topic")), 
				                               new Integer((String)result.get("cnt")).intValue()
				                              );
	  }
  }*/
  /*calculate term frequency inverse document frequence*/
  /*using boolean frequencies for terms in documents*/
  /*denominator is adjusted by 1 to avoid division by zero*/
  public double calc_tf_idf_UserTopic(Long userID, String topic) {
	  return Math.log(this.countAllTweetsForUser(userID)/(1+this.countTopicTweetsForUser(userID, topic)));
  }
  
  public int countTopicTweetsForUser(Long userID, String topic) {
	  DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
	  BasicDBObject query = new BasicDBObject("user", userID);
	  query.append("topic", topic);
	  return collection.find(query).count();
  }
  
  public int countAllTweetsForUser(Long userID) {	  
	  DBCollection collection = this.db.getCollection(DocStoreConnectorImpl.TOPIC_COLLECTION);
	  BasicDBObject query = new BasicDBObject("user", userID);
	  return collection.find(query).count();
  }
  
  public static void main(String[] args) {
    DocStoreConnectorImpl docstore = new DocStoreConnectorImpl();
    docstore.connect();
    docstore.dropDatabase();
    docstore.createTopicCollection();
    docstore.createAdsCollection();
    
    for(Ad x : docstore.retrieveAds()) {
    	System.out.println(x.getID());
    	System.out.println(x.getName());
    }
    System.out.println(docstore.retrieveTopics());
    System.out.println(docstore.getTopicIDForKeyword("ubuntu"));
    
    //docstore.getKeywordsForTopic(2);
    
    // docstore.getTopicForID(2);
    
    
    //docstore.getInterestsForUsers(20, null);
    //System.out.println("end");
  }
}
