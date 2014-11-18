package at.ac.tuwien.aic;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class Neo4JConnectorImpl implements Neo4JConnector {

  private GraphDatabaseService graphDb;
  private static final String STORE_DIR = "graphDB";
  private static final String NEO4J_PROPERTIES_PATH = "neo4j.properties";

  public void connect() {
    this.graphDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(Neo4JConnectorImpl.STORE_DIR)
        .loadPropertiesFromURL(this.getClass().getResource("/" + Neo4JConnectorImpl.NEO4J_PROPERTIES_PATH))
        .newGraphDatabase();
  }

  public void disconnect() {
    this.graphDb.shutdown();
  }

}
