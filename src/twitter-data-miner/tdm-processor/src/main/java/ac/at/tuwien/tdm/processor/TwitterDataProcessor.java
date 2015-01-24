package ac.at.tuwien.tdm.processor;

import at.ac.tuwien.aic.Neo4JConnector;
import at.ac.tuwien.aic.Neo4JConnectorImpl;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ac.at.tuwien.tdm.docstore.DocStoreConnector;
import ac.at.tuwien.tdm.docstore.DocStoreConnectorImpl;
import ac.at.tuwien.tdm.processor.reader.TwitterFileReader;

import com.google.gson.Gson;


public abstract class TwitterDataProcessor {
  protected final TwitterFileReader reader = TwitterFileReader.getInstance();
  //protected final Neo4JConnector neo4j = Neo4JConnectorImpl.getInstance();
  protected Neo4JConnector neo4j = null;
  protected final DocStoreConnector docStore = new DocStoreConnectorImpl();
  protected String fileFolder = null;
  protected String folderProcessed = null;
  
  protected final Gson gson = new Gson();
  
  public TwitterDataProcessor(String fileFolder, String folderProcessed, String neo4jDBPath, String neo4jPropertiesPath) {
    this.fileFolder = fileFolder;
    this.folderProcessed = folderProcessed;
    this.neo4j = new Neo4JConnectorImpl(neo4jDBPath, neo4jPropertiesPath);
  }
  
  public void connectNeo4J() {
    this.neo4j.connect(false);
  }
  
  public void disconnectNeo4J() {
    this.neo4j.disconnect();
  }
  
  public void createProcessedFolder() {
    Path path = Paths.get(this.folderProcessed);

    if (Files.notExists(path)) {
        path.toFile().mkdir();
    }
  }
  
  public boolean checkIFFolderExists() {
    Path path = Paths.get(this.fileFolder);
    return Files.exists(path);
  }

  public void start() {
    if(this.checkIFFolderExists() == false) {
      return;
    }
    else {
      this.createProcessedFolder();
    }
    
    this.process();
  }
  
  public abstract void process();
}
