package ac.at.tuwien.tdm.processor;

import at.ac.tuwien.aic.Neo4JConnector;
import at.ac.tuwien.aic.Neo4JConnectorImpl;

import ac.at.tuwien.tdm.docstore.DocStoreConnector;
import ac.at.tuwien.tdm.docstore.DocStoreConnectorImpl;
import ac.at.tuwien.tdm.processor.reader.TwitterFileReader;

import com.google.gson.Gson;


public abstract class TwitterDataProcessor {
  protected final TwitterFileReader reader = TwitterFileReader.getInstance();
  protected final Neo4JConnector neo4j = Neo4JConnectorImpl.getInstance();
  protected final DocStoreConnector docStore = new DocStoreConnectorImpl();
  
  protected final Gson gson = new Gson();
  
  public void connectNeo4J() {
    this.neo4j.connect(false);
  }
  
  public void disconnectNeo4J() {
    this.neo4j.disconnect();
  }

  public abstract void process();
}
