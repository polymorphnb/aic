package at.ac.tuwien.aic;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.docstore.DocStoreConnector;
import ac.at.tuwien.tdm.queries.QueryHelper;
import ac.at.tuwien.tdm.results.DirectInterestResult;
import ac.at.tuwien.tdm.results.IndirectInterestResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Path;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.ReadableIndex;
import org.neo4j.graphdb.index.UniqueFactory;
import org.neo4j.graphdb.index.UniqueFactory.UniqueEntity;
import org.neo4j.graphdb.schema.ConstraintDefinition;
import org.neo4j.graphdb.schema.IndexDefinition;
import org.neo4j.graphdb.traversal.Evaluators;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neo4JConnectorImpl implements Neo4JConnector {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4JConnectorImpl.class);

  private String graphDBLocation = Neo4JConnector.STORE_DIR_DEFAULT;
  private String neo4JPropertiesLocation = Neo4JConnector.NEO4J_PROPERTIES_PATH_DEFAULT;
  private static final String NEO4JBATCHINSERT_PROPERTIES_PATH = "neo4jBatchInsert.properties";

  private static final String USER_NODE_INDEX_NAME = "user";
  private static final String TOPIC_NODE_INDEX_NAME = "topic";

  private static final Label USER_LABEL = DynamicLabel.label("user");
  private static final Label TOPIC_LABEL = DynamicLabel.label("topic");

  //private static final Neo4JConnector INSTANCE = new Neo4JConnectorImpl();

  private GraphDatabaseService graphDb;
  private ReadableIndex<Node> autoNodeIndex;
  private ReadableIndex<Relationship> autoRelationshipIndex;

  private Transaction currentTransaction;

  private BatchInserter inserter;
  private BatchInserterIndex friendsIndex;
  private BatchInserterIndex followsIndex;
  private BatchInserterIndex interactsWithIndex;

  private boolean batchInsert = false;
  
  public Neo4JConnectorImpl(String store_dir, String prop_path) {
	  this.graphDBLocation = store_dir;
	  this.neo4JPropertiesLocation = prop_path;
  }

  public void connectBatchInsert() {
    InputStream in = this.getClass().getResourceAsStream("/" + Neo4JConnectorImpl.NEO4JBATCHINSERT_PROPERTIES_PATH);
    Map<String, String> config = null;
    try {
      config = MapUtil.load(in);
    } catch (IOException e) {
      LOGGER.error("Could not load config for BatchInsert from '%s'", Neo4JConnectorImpl.NEO4JBATCHINSERT_PROPERTIES_PATH);
    }

    inserter = BatchInserters.inserter(this.graphDBLocation, config);
    @SuppressWarnings("unused")
    ConstraintDefinition cdf = inserter.createDeferredConstraint(USER_LABEL).assertPropertyIsUnique(USER_NODE_INDEX_NAME).create();
    IndexDefinition idx = inserter.createDeferredSchemaIndex(USER_LABEL).on(USER_NODE_INDEX_NAME).create();
    System.out.println(idx.isConstraintIndex());
    
    BatchInserterIndexProvider indexProvider = new LuceneBatchInserterIndexProvider(inserter);
    followsIndex = indexProvider.relationshipIndex(RelationshipTypeConstants.FOLLOWS, MapUtil.stringMap("type", "exact"));
    friendsIndex = indexProvider.relationshipIndex(RelationshipTypeConstants.FRIEND, MapUtil.stringMap("type", "exact"));
    interactsWithIndex = indexProvider.relationshipIndex(RelationshipTypeConstants.INTERACTS_WITH, MapUtil.stringMap("type", "exact"));
    
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
        LOGGER.error(String.format("Could not load config for BatchInsert from '%s'", Neo4JConnectorImpl.NEO4JBATCHINSERT_PROPERTIES_PATH));
      }
      this.batchInsert = true;
      this.graphDb = BatchInserters.batchDatabase(this.graphDBLocation, config);
    } 
    else {
      try {
        this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(this.graphDBLocation)
            .loadPropertiesFromURL(this.getClass().getResource("/" + this.neo4JPropertiesLocation))
              .newGraphDatabase();
        autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
        autoRelationshipIndex = graphDb.index().getRelationshipAutoIndexer().getAutoIndex();
        return;
      } catch (Exception ex) {
        LOGGER.info(String.format("Could not load properties file '%s' as resource, trying file system.", "/" + this.neo4JPropertiesLocation));
      }
      this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(this.graphDBLocation)
          .loadPropertiesFromFile(this.neo4JPropertiesLocation)
            .newGraphDatabase();
      
      autoNodeIndex = graphDb.index().getNodeAutoIndexer().getAutoIndex();
      autoRelationshipIndex = graphDb.index().getRelationshipAutoIndexer().getAutoIndex();
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
  
//  public void addTopic(Long topicID) {
//    Node topic = this.getUser(topicID);
//    if(topic == null) {
//      topic = this.graphDb.createNode(TOPIC_LABEL);
//      topic.setProperty(TOPIC_NODE_INDEX_NAME, topicID);
//    }
//  }
  
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
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(USER_NODE_INDEX_NAME, userID);
      return this.inserter.createNode(properties, USER_LABEL);
    } catch (Exception ex) {
      LOGGER.error(String.format("Could not insert User '%s' via batchInsert: " + ex.getMessage(), userID));
    }
    return (long) -1;
  }

  private void insertRelationshipBatch(Long node1, Long node2, Long userID1, Long userID2, TwitterRelationshipType type) {
    try {
      Map<String, Object> properties = new HashMap<String, Object>();
      properties.put(type.getValue(), userID1 + " " + userID2);
      Long id = this.inserter.createRelationship(node1, node2, type, properties);
      if(type.equals(TwitterRelationshipType.FOLLOWS)) {
        this.followsIndex.add(id, properties);
      }
      else if(type.equals(TwitterRelationshipType.FRIEND)) {
        this.friendsIndex.add(id, properties);
      }
      else if(type.equals(TwitterRelationshipType.INTERACTS_WITH)) {
        this.interactsWithIndex.add(id, properties);
      }
    } catch (Exception ex) {
      LOGGER.error("Could not insert Relationship via batchInsert: " + ex.getMessage());
    }
  }

  public UniqueEntity<Node> getOrCreateUserWithUniqueFactory(Long id, final boolean fullUser) {
    UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "user") {

      @Override
      protected void initialize(Node created, Map<String, Object> properties) {
        created.setProperty(USER_NODE_INDEX_NAME, properties.get(USER_NODE_INDEX_NAME));
        created.addLabel(USER_LABEL);
      }
    };

    return factory.getOrCreateWithOutcome(USER_NODE_INDEX_NAME, id);
  }
  
  public UniqueEntity<Node> getOrCreateTopicWithUniqueFactory(Long id) {
    UniqueFactory<Node> factory = new UniqueFactory.UniqueNodeFactory(graphDb, "topic") {

      @Override
      protected void initialize(Node created, Map<String, Object> properties) {
        created.setProperty(TOPIC_NODE_INDEX_NAME, properties.get(TOPIC_NODE_INDEX_NAME));
        created.addLabel(TOPIC_LABEL);
      }
    };

    return factory.getOrCreateWithOutcome(TOPIC_NODE_INDEX_NAME, id);
  }

  public UniqueEntity<Relationship> getOrCreateRelationshipWithUniqueFactory(final Node user1, final Node node2,
      final TwitterRelationshipType type, boolean isTopic) {
    UniqueFactory<Relationship> factory = new UniqueFactory.UniqueRelationshipFactory(graphDb, type.getValue()) {

      @Override
      protected Relationship create(Map<String, Object> properties) {
        Relationship rel = user1.createRelationshipTo(node2, type);
        rel.setProperty(type.getValue(), properties.get(type.getValue()));
        rel.setProperty(RelationshipTypeConstants.WEIGHT, 1);
        return rel;
      }
    };

    if(isTopic == false) {    
      return factory.getOrCreateWithOutcome(type.getValue(), user1.getProperty(USER_NODE_INDEX_NAME) + "_" + node2.getProperty(USER_NODE_INDEX_NAME));
    }
    else {
      return factory.getOrCreateWithOutcome(type.getValue(), user1.getProperty(USER_NODE_INDEX_NAME) + "_" + node2.getProperty(TOPIC_NODE_INDEX_NAME));
    }
  }
  
  @SuppressWarnings("unused")
  private Node getTopic(Long id) {
    Node topic = autoNodeIndex.get(TOPIC_NODE_INDEX_NAME, id).getSingle();
    return topic;
  }

  private Node getUser(Long id) {
    Node user = autoNodeIndex.get(USER_NODE_INDEX_NAME, id).getSingle();
    return user;
  }
  
  public Relationship getRelationshipInterested(Long userID, Long topicID) {
    Relationship rel = autoRelationshipIndex.get(TwitterRelationshipType.INTERESTEDIN.getValue(), userID + "_" + topicID).getSingle();

    return rel;
  }
  
  public Integer getTopicWeightForUser(Long userID, Long topicID) {
    Relationship rel = this.getRelationshipInterested(userID, topicID);
    if(rel == null) {
      return 0;
    }
    return Integer.valueOf(rel.getProperty(RelationshipTypeConstants.WEIGHT).toString());
  }

  private void addRelationship(Long userID1, Long userID2, TwitterRelationshipType type) {
    if (this.batchInsert == true) {
      //inserter.insertRelationshipBatch(userID1, userID2, type);
    } else {
      
      Node user1 = this.getUser(userID1);
      Node user2 = this.getUser(userID2);
      
      if(user2 == null) {
        return;
      }
      
      UniqueEntity<Relationship> rel = this.getOrCreateRelationshipWithUniqueFactory(user1, user2, type, false);
      if (rel.wasCreated() == false) {
        int weight = ((Integer) rel.entity().getProperty(RelationshipTypeConstants.WEIGHT)).intValue();
        weight++;
        rel.entity().setProperty(RelationshipTypeConstants.WEIGHT, weight);
      }
    }
  }

  private void addRelationshipInterested(Long userID1, Long topicID, int weight, TwitterRelationshipType type) {
    Node user1 = this.getUser(userID1);
    UniqueEntity<Node> topic = this.getOrCreateTopicWithUniqueFactory(topicID);
    UniqueEntity<Relationship> rel = this.getOrCreateRelationshipWithUniqueFactory(user1, topic.entity(), type, true);
    if (rel.wasCreated() == false) {
      Integer weightInt = (Integer) rel.entity().getProperty(RelationshipTypeConstants.WEIGHT);
      if(weight == 0) {
        // just in case the value has not been set properly
        weight = 1;
      }
      weightInt += Integer.valueOf(weight);
      rel.entity().setProperty(RelationshipTypeConstants.WEIGHT, weightInt);
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
  
  public void addInteractsWithRelationship(Long userID1, Long userID2) {
    this.addRelationship(userID1, userID2, TwitterRelationshipType.INTERACTS_WITH);
  }
  
  public void addInterestedInRelationship(Long userID1, Long topicID) {
    this.addRelationshipInterested(userID1, topicID, 0, TwitterRelationshipType.INTERESTEDIN);
  }

  public void addInterestedInRelationship(Long userID1, Long topicID, int weight) {
    this.addRelationshipInterested(userID1, topicID, weight, TwitterRelationshipType.INTERESTEDIN);
  }

//  private Relationship getRelationship(Node user1, Node user2, TwitterRelationshipType type) {
//    Iterable<Relationship> itRel = user1.getRelationships(type);
//    if (itRel != null) {
//      if (itRel.iterator().hasNext()) {
//        return itRel.iterator().next();
//      }
//    }
//    return null;
//
//  }
  
  public List<DirectInterestResult> getDirectInterestsForUser(Long userId, int interestThreshold, DocStoreConnector docstore) {
	  LinkedList<DirectInterestResult> ret = new LinkedList<DirectInterestResult>();
	  Node u = this.getUser(userId);
	  Iterable<Relationship> itRel = u.getRelationships(TwitterRelationshipType.INTERESTEDIN);
	  while(itRel.iterator().hasNext()) {
		  Relationship tmp = itRel.iterator().next();
		  if(((Integer)tmp.getProperty(RelationshipTypeConstants.WEIGHT)).intValue() >= interestThreshold) {
			  ret.addLast(new DirectInterestResult(
					     ((Integer)tmp.getProperty(RelationshipTypeConstants.WEIGHT)).intValue(),
					     docstore.getTopicForID((Long)tmp.getEndNode().getProperty(TOPIC_NODE_INDEX_NAME)),
					     (Long)tmp.getEndNode().getProperty(TOPIC_NODE_INDEX_NAME)
					  ));
		  }
	  }

	  return QueryHelper.sortDirectInterestResults(ret);
  }
  
  public List<IndirectInterestResult> getIndirectInterestsForUser(Long userId, int maxDepth, int interestThreshold, DocStoreConnector docstore ) {
	  //TODO return type and sort by weight, remove duplicate interests
	  LinkedList<IndirectInterestResult> ret = new LinkedList<IndirectInterestResult>();
	  Map<Long, IndirectInterestResult> interestTopicToUser = new HashMap<Long, IndirectInterestResult>();
	  for( Path position : this.graphDb.traversalDescription()
			               .breadthFirst()
			               .relationships(TwitterRelationshipType.INTERACTS_WITH)
			               .evaluator(Evaluators.toDepth(maxDepth))
			               .traverse(this.getUser(userId))
			  
	  ) {
	   
	    for(DirectInterestResult x : 
			   this.getDirectInterestsForUser(((Long)position.endNode().getProperty(USER_NODE_INDEX_NAME)).longValue(), interestThreshold, docstore))
	    {
	      if(interestTopicToUser.containsKey(x.getTopicID())) {
	        continue;
	      }
	      IndirectInterestResult tempResult = new IndirectInterestResult(x.getInterest(), x.getTopic(), x.getTopicID(), position.length());   
	      ret.addLast(tempResult);
	      interestTopicToUser.put(x.getTopicID(), tempResult);
		 }
	  }
	  
	  return ret;
  }
  
  public static void main (String[] args) {
	  Neo4JConnectorImpl db = new Neo4JConnectorImpl("/tmp/graphdb", "/tmp/neo4j.properties");
	  db.connect(false);
	  db.startTransaction();
	  db.addUser(new User(1, "test", "test", "test", "test",1, 1, 1, 1), true);
	  db.addUser(new User(2, "", "", "", "",1, 1, 1, 1), true);
	  db.addUser(new User(3, "", "", "", "",1, 1, 1, 1), true);
	  db.addInteractsWithRelationship(new Long(1), new Long(2));
	  db.addInteractsWithRelationship(new Long(2), new Long(3));
	  db.addInteractsWithRelationship(new Long(1), new Long(3));
	  db.addInterestedInRelationship(new Long(1), new Long(1111), 1);
	  db.addInterestedInRelationship(new Long(2), new Long(2222), 1);
	  db.addInterestedInRelationship(new Long(3), new Long(3333), 5);
	  //db.getDirectInterestsForUser(new Long(1), 2);
	  //System.out.println("indirect");
	  //db.getIndirectInterestsForUser(new Long(1), 3,2);
	  db.closeTransaction();
	  db.disconnect();
  }
}
