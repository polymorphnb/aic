package at.ac.tuwien.aic;

import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.ReadableIndex;

public class Neo4JConnectorImpl implements Neo4JConnector {

  
  private static final String STORE_DIR = "graphDB";
  private static final String NEO4J_PROPERTIES_PATH = "neo4j.properties";
  
  private static final String USER_NODE_INDEX_NAME = "user";
  
  private static final Neo4JConnector INSTANCE = new Neo4JConnectorImpl();
  
  private GraphDatabaseService graphDb;
  
  private Neo4JConnectorImpl() {
    this.connect();
  }
  
  public static Neo4JConnector getInstance() {
    return Neo4JConnectorImpl.INSTANCE;
  }

  public void connect() {
    this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(Neo4JConnectorImpl.STORE_DIR)
        .loadPropertiesFromURL(this.getClass().getResource("/" + Neo4JConnectorImpl.NEO4J_PROPERTIES_PATH))
        .newGraphDatabase();
  }

  public void disconnect() {
    this.graphDb.shutdown();
  }
  
  private Transaction startTransaction() {
    return this.graphDb.beginTx();
  }
  
  private void closeTransaction(Transaction tx) {
    tx.success();
    tx.close();
  }
  
  public void addUserNode(String id) {
    Transaction tx = this.startTransaction();
    Node user = this.getUser(id);
    if(user == null) {
      user = this.graphDb.createNode(DynamicLabel.label(id));
      user.setProperty(USER_NODE_INDEX_NAME, id);
    }
    this.closeTransaction(tx);
  }
  
  private Node getUser(String id) {
    ReadableIndex<Node> autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
    Node user = autoNodeIndex.get(USER_NODE_INDEX_NAME, id).getSingle();
    return user;
  }
  
  private void addRelationship(String userID1, String userID2, TwitterRelationshipType type) {
    Transaction tx = this.startTransaction();
    Node user1 = this.getUser(userID1);
    Node user2 = this.getUser(userID2);
    if(user2 == null) {
      user2 = this.graphDb.createNode(DynamicLabel.label(userID2));
      user2.setProperty(USER_NODE_INDEX_NAME, userID2);
    }
    
    Relationship rel = this.getRelationship(user1, user2, type);
    if(rel != null) {
      Integer weight = (Integer)rel.getProperty(RelationshipTypeConstants.WEIGHT);
      weight++;
      rel.setProperty(RelationshipTypeConstants.WEIGHT, weight);
    }
    else {
      rel = user1.createRelationshipTo(user2, type);
      rel.setProperty(RelationshipTypeConstants.WEIGHT, 0);
    }
    this.closeTransaction(tx);
  }
  
  private void addRelationshipInterested(String userID1, String topicID, int weight, TwitterRelationshipType type) {
    Node user1 = this.getUser(userID1);
    //TODO: get Topic Node user2 = this.getUser(userID2);
    Node topic = null;
    Relationship rel = this.getRelationship(user1, topic, type);
    if(rel != null) {
      Integer weightInt = (Integer)rel.getProperty(RelationshipTypeConstants.WEIGHT);
      weightInt += Integer.valueOf(weight);
      rel.setProperty(RelationshipTypeConstants.WEIGHT, weightInt);
    }
    else {
      rel = user1.createRelationshipTo(topic, type);
      rel.setProperty(RelationshipTypeConstants.WEIGHT, Integer.valueOf(weight));
    }
    
  }
  
  public void addFollowsRelationship(String userID1, String userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.FOLLOWS);
  }
  
  public void addFriendsRelationship(String userID1, String userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.FRIEND);
  }
  
  public void addRepliesRelationship(String userID1, String userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.REPLIES);
  }
  
  public void addRetweetsRelationship(String userID1, String userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.RETWEETS);
  }
  
  public void addInterestedInRelationship(String userID1, String topicID, int weight) {
    this.addRelationshipInterested(userID1, topicID, weight, TwitterRelationshipType.INTERESTEDIN);
  }
  
  private Relationship getRelationship(Node user1, Node user2, TwitterRelationshipType type) {
    Iterable<Relationship> itRel = user1.getRelationships(type);
    if(itRel != null) {
      if(itRel.iterator().hasNext()) {
        return itRel.iterator().next();
      }
    }
    return null;

  }

}
