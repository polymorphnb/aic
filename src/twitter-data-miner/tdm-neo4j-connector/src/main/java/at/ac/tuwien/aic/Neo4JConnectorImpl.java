package at.ac.tuwien.aic;

import ac.at.tuwien.tdm.commons.pojo.User;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.index.UniqueFactory.UniqueEntity;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

public class Neo4JConnectorImpl implements Neo4JConnector {

  private static final String STORE_DIR = "graphDB";
  private static final String NEO4J_PROPERTIES_PATH = "neo4j.properties";
  private static final String NEO4JBATCHINSERT_PROPERTIES_PATH = "neo4jBatchInsert.properties";

  private static final String USER_NODE_INDEX_NAME = "user";

  private static final Label USER_LABEL = DynamicLabel.label("user");

  private static final Neo4JConnector INSTANCE = new Neo4JConnectorImpl();

  private GraphDatabaseService graphDb;
  private ReadableIndex<Node> autoNodeIndex;

  private Transaction currentTransaction;

  private BatchInserter inserter;
  private BatchInserterIndex friendsIndex;
  private BatchInserterIndex followsIndex;

  private boolean batchInsert = false;

  private Neo4JConnectorImpl() {
  }

  public static Neo4JConnector getInstance() {
    return Neo4JConnectorImpl.INSTANCE;
  }

  public void connectBatchInsert() {
    InputStream in = this.getClass().getResourceAsStream("/" + Neo4JConnectorImpl.NEO4JBATCHINSERT_PROPERTIES_PATH);
    Map<String, String> config = null;
    try {
      config = MapUtil.load(in);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    inserter = BatchInserters.inserter(Neo4JConnectorImpl.STORE_DIR, config);
    ConstraintDefinition cdf = inserter.createDeferredConstraint(USER_LABEL).assertPropertyIsUnique(USER_NODE_INDEX_NAME).create();
    IndexDefinition idx = inserter.createDeferredSchemaIndex(USER_LABEL).on(USER_NODE_INDEX_NAME).create();
    System.out.println(idx.isConstraintIndex());
    
    BatchInserterIndexProvider indexProvider = new LuceneBatchInserterIndexProvider(inserter);
    followsIndex = indexProvider.relationshipIndex(RelationshipTypeConstants.FOLLOWS, MapUtil.stringMap("type", "exact"));
    friendsIndex = indexProvider.relationshipIndex(RelationshipTypeConstants.FRIEND, MapUtil.stringMap("type", "exact"));
    
    batchInsert = true;
  }

  public void disconnectBatchInsert() {
    inserter.shutdown();
  }

  public void connect(boolean batchInsert) {
    if (batchInsert == true) {
      InputStream in = this.getClass().getResourceAsStream("/" + Neo4JConnectorImpl.NEO4JBATCHINSERT_PROPERTIES_PATH);
      Map<String, String> config = null;
      try {
        config = MapUtil.load(in);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      this.batchInsert = true;
      this.graphDb = BatchInserters.batchDatabase(Neo4JConnectorImpl.STORE_DIR, config);
    } else {
      this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(Neo4JConnectorImpl.STORE_DIR)
          .loadPropertiesFromURL(this.getClass().getResource("/" + Neo4JConnectorImpl.NEO4J_PROPERTIES_PATH))
          .newGraphDatabase();
      autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
    }

  }

  public void disconnect() {
    if (batchInsert == true) {
      if(this.graphDb != null) {
        this.graphDb.shutdown();
      }
      else {
        this.inserter.shutdown();
      }
    } else {
      this.graphDb.shutdown();
    }
  }

  public void startTransaction() {
    if(this.batchInsert == false) {
      currentTransaction = this.graphDb.beginTx();
    }
  }

  public void closeTransaction() {
    if(this.batchInsert == false) {
      currentTransaction.success();
      currentTransaction.close();
    }
  }

  public String getUserViaCypher(Long userID) {
    ExecutionEngine engine = new ExecutionEngine(graphDb);
    for(String indexname : graphDb.index().nodeIndexNames()) {
      System.out.println(indexname);
    }
    System.out.println(graphDb.index().getNodeAutoIndexer().getAutoIndex().getName());

    ExecutionResult result = engine.execute( "match (n {user: " + userID + "}) return n, n.user" );
    
    return result.dumpToString();
    
  }
  
  public String getUserAsString(Long userID) {
    return this.autoNodeIndex.get(USER_NODE_INDEX_NAME, userID).getSingle().toString();
  }

  public void addUserNode(Long id) {
    Node user = this.getUser(id);
    if (user == null) {
      user = this.graphDb.createNode(USER_LABEL);
      user.setProperty(USER_NODE_INDEX_NAME, id);
    }
  }

  public void addUser(User user, boolean fullUser) {
    if (this.batchInsert == true) {
      Long node1 = this.insertUserBatch(user.getId());

      for (Long userID : user.getFollowerUserIds()) {
        Long node2 = this.insertUserBatch(userID);
        this.insertRelationshipBatch(node1, node2, user.getId(), userID, TwitterRelationshipType.FOLLOWS);
        //this.addFollowsRelationship(user.getId(), userID);
      }
      for (Long userID : user.getFriendsUserIds()) {
        Long node2 = this.insertUserBatch(userID);
        this.insertRelationshipBatch(node1, node2, user.getId(), userID, TwitterRelationshipType.FRIEND);
        // this.addFriendsRelationship(user.getId(), userID);
      }
    } else {
      this.getOrCreateUserWithUniqueFactory(user.getId(), fullUser);
      for (Long userID : user.getFollowerUserIds()) {
        this.addFollowsRelationship(user.getId(), userID);
      }
      for (Long userID : user.getFriendsUserIds()) {
        this.addFriendsRelationship(user.getId(), userID);
      }
    }
  }

  private Long insertUserBatch(Long userID) {
    try {
      Map<String, Object> properties = new HashMap<>();
      properties.put(USER_NODE_INDEX_NAME, userID);
      return this.inserter.createNode(properties, USER_LABEL);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
    return (long) -1;
  }

  private void insertRelationshipBatch(Long node1, Long node2, Long userID1, Long userID2, TwitterRelationshipType type) {
    try {
      Map<String, Object> properties = new HashMap<>();
      properties.put(type.getValue(), userID1 + " " + userID2);
      Long id = this.inserter.createRelationship(node1, node2, type, properties);
      if(type.equals(TwitterRelationshipType.FOLLOWS)) {
        this.followsIndex.add(id, properties);
      }
      else if(type.equals(TwitterRelationshipType.FRIEND)) {
        this.friendsIndex.add(id, properties);
      }
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  public UniqueEntity<Node> getOrCreateUserWithUniqueFactory(Long id, final boolean fullUser) {
    UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "user") {

      @Override
      protected void initialize(Node created, Map<String, Object> properties) {
        created.setProperty(USER_NODE_INDEX_NAME, properties.get(USER_NODE_INDEX_NAME));
      }
    };

    return factory.getOrCreateWithOutcome(USER_NODE_INDEX_NAME, id);
  }

  public UniqueEntity<Relationship> getOrCreateRelationshipWithUniqueFactory(final Node user1, final Node user2,
      final TwitterRelationshipType type) {
    UniqueFactory<Relationship> factory = new UniqueFactory.UniqueRelationshipFactory(graphDb, type.getValue()) {

      @Override
      protected Relationship create(Map<String, Object> properties) {
        Relationship rel = user1.createRelationshipTo(user2, type);
        rel.setProperty(type.getValue(), properties.get(type.getValue()));
        rel.setProperty(RelationshipTypeConstants.WEIGHT, 0);
        return rel;
      }
    };

    return factory.getOrCreateWithOutcome(type.getValue(),
        user1.getProperty(USER_NODE_INDEX_NAME) + "_" + user2.getProperty(USER_NODE_INDEX_NAME));
  }

  private Node getUser(Long id) {
    Node user = autoNodeIndex.get(USER_NODE_INDEX_NAME, id).getSingle();
    return user;
  }

  private void addRelationship(Long userID1, Long userID2, TwitterRelationshipType type) {
    if (this.batchInsert == true) {
      //inserter.insertRelationshipBatch(userID1, userID2, type);
    } else {
      UniqueEntity<Node> user1 = this.getOrCreateUserWithUniqueFactory(userID1, true);
      UniqueEntity<Node> user2 = this.getOrCreateUserWithUniqueFactory(userID2, false);

      //      Relationship rel = user1.createRelationshipTo(user2, type);
      //      rel.setProperty(RelationshipTypeConstants.WEIGHT, 0);

      UniqueEntity<Relationship> rel = this.getOrCreateRelationshipWithUniqueFactory(user1.entity(), user2.entity(),
          type);
      if (rel.wasCreated() == false) {
        int weight = (int) rel.entity().getProperty(RelationshipTypeConstants.WEIGHT);
        weight++;
        rel.entity().setProperty(RelationshipTypeConstants.WEIGHT, weight);
      }
      //      Relationship rel = this.getRelationship(user1, user2, type);
      //      if (rel != null) {
      //        Integer weight = (Integer) rel.getProperty(RelationshipTypeConstants.WEIGHT);
      //        weight++;
      //        rel.setProperty(RelationshipTypeConstants.WEIGHT, weight);
      //      } else {
      //        rel = user1.createRelationshipTo(user2, type);
      //        rel.setProperty(RelationshipTypeConstants.WEIGHT, 0);
      //      }
    }
  }

  private void addRelationshipInterested(Long userID1, Long topicID, int weight, TwitterRelationshipType type) {
    Node user1 = this.getUser(userID1);
    //TODO: get Topic Node user2 = this.getUser(userID2);
    Node topic = null;
    Relationship rel = this.getRelationship(user1, topic, type);
    if (rel != null) {
      Integer weightInt = (Integer) rel.getProperty(RelationshipTypeConstants.WEIGHT);
      weightInt += Integer.valueOf(weight);
      rel.setProperty(RelationshipTypeConstants.WEIGHT, weightInt);
    } else {
      rel = user1.createRelationshipTo(topic, type);
      rel.setProperty(RelationshipTypeConstants.WEIGHT, Integer.valueOf(weight));
    }

  }

  public void addFollowsRelationship(Long userID1, Long userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.FOLLOWS);
  }

  public void addFriendsRelationship(Long userID1, Long userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.FRIEND);
  }

  public void addRepliesRelationship(Long userID1, Long userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.REPLIES);
  }

  public void addRetweetsRelationship(Long userID1, Long userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.RETWEETS);
  }

  public void addInterestedInRelationship(Long userID1, Long topicID, int weight) {
    this.addRelationshipInterested(userID1, topicID, weight, TwitterRelationshipType.INTERESTEDIN);
  }

  private Relationship getRelationship(Node user1, Node user2, TwitterRelationshipType type) {
    //    System.out.println("Neo4JConnectorImpl.getRelationship() " + user1 + " " + user2 + " " + type);
    Iterable<Relationship> itRel = user1.getRelationships(type);
    if (itRel != null) {
      if (itRel.iterator().hasNext()) {
        return itRel.iterator().next();
      }
    }
    return null;

  }
}
