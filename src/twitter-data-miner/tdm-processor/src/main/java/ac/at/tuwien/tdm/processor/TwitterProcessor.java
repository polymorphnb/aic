package ac.at.tuwien.tdm.processor;

import at.ac.tuwien.aic.Neo4JConnector;
import ac.at.tuwien.tdm.docstore.DocStoreConnector;
import ac.at.tuwien.tdm.docstore.DocStoreConnectorImpl;
import ac.at.tuwien.tdm.userdb.UserDBConnector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

public class TwitterProcessor {
  
  private static String NEO4J_CONFIG_FILE = "neo4j.config";
  
  private static String USERDB_CONFIG_FILE = "userdb.config";
  
  public TwitterProcessor() {
    this.addShutdownHook();
  }

  public static void main(String[] args) throws FileNotFoundException {
    
    Properties propNeo4j = TwitterProcessor.loadNeo4JConfig();
    String neoPath = Neo4JConnector.STORE_DIR_DEFAULT;
    String neoProp = Neo4JConnector.NEO4J_PROPERTIES_PATH_DEFAULT;
    if(propNeo4j != null) {
      neoPath = propNeo4j.getProperty(Neo4JConnector.NEO4J_PATH_KEY);
      neoProp = propNeo4j.getProperty(Neo4JConnector.NEO4J_PATH_PROPERTIES_KEY);
    }
    
    Properties propUserDB = TwitterProcessor.loadUserDBConfig();
    String userDBPath = UserDBConnector.PATH_TO_DB_DEFAULT;
    if(propUserDB != null) {
      userDBPath = propUserDB.getProperty(UserDBConnector.PATH_USERDB_KEY);
    }
    
    if(args.length != 0) {
      if(args[0].equals("rebuild")) {
        DocStoreConnector docStore = new DocStoreConnectorImpl();
        docStore.connect();
        docStore.dropDatabase();
        docStore.createTopicCollection();
        docStore.createAdsCollection();
        
        //UserDBConnector userDB = UserDBConnector.getInstance();
        UserDBConnector userDB = new UserDBConnector(userDBPath);
        userDB.connect();
        userDB.dropTableTwitterUsers();
        userDB.createUserTable();
        System.out.println("RESET ALL DATABASES");
      }
    }
    
    TwitterUserDataProcessor userDataProcessor = new TwitterUserDataProcessor(neoPath, neoProp, userDBPath);
    userDataProcessor.connectNeo4J();
    userDataProcessor.start();
    userDataProcessor.disconnectNeo4J();
    
    TwitterTweetDataProcessor twitterDataProcessor = new TwitterTweetDataProcessor(neoPath, neoProp);
    twitterDataProcessor.connectNeo4J();
    twitterDataProcessor.start();    
    twitterDataProcessor.disconnectNeo4J();
    
  }
  
  private static Properties loadNeo4JConfig() {
    try {
    InputStream resource = new FileInputStream("./" + USERDB_CONFIG_FILE);
    
    Properties prop = new Properties();
    prop.load(resource);
    
    return prop;
    } catch (Exception ex) {
      return null;
    }
  }
  
  private static Properties loadUserDBConfig() {
    try {
    InputStream resource = new FileInputStream("./" + NEO4J_CONFIG_FILE);
    
    Properties prop = new Properties();
    prop.load(resource);
    
    return prop;
    } catch (Exception ex) {
      return null;
    }
  }

  private void addShutdownHook() {
    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        System.out.println("Shutdown");
      }
    });
  }
}
