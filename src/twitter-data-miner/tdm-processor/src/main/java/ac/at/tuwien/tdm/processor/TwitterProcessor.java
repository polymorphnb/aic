package ac.at.tuwien.tdm.processor;

import at.ac.tuwien.aic.Neo4JConnector;

import ac.at.tuwien.tdm.docstore.DocStoreConnector;
import ac.at.tuwien.tdm.docstore.DocStoreConnectorImpl;
import ac.at.tuwien.tdm.userdb.UserDBConnector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitterProcessor {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterProcessor.class);
    
  public TwitterProcessor() {
    this.addShutdownHook();
  }

  public static void main(String[] args) throws FileNotFoundException {
    
    Properties config = TwitterProcessor.loadConfig();
    String neoPath = Neo4JConnector.STORE_DIR_DEFAULT;
    String neoProp = Neo4JConnector.NEO4J_PROPERTIES_PATH_DEFAULT;
    String userDBPath = UserDBConnector.PATH_TO_DB_DEFAULT;
    String userDBTablePath = UserDBConnector.PATH_TO_TABLE_DEFAULT;
    if(config != null) {
      if(config.containsKey(Neo4JConnector.NEO4J_PATH_KEY)) {
        neoPath = config.getProperty(Neo4JConnector.NEO4J_PATH_KEY);
      }
      if(config.containsKey(Neo4JConnector.NEO4J_PATH_PROPERTIES_KEY)) {
        neoProp = config.getProperty(Neo4JConnector.NEO4J_PATH_PROPERTIES_KEY);
      }
      if(config.containsKey(UserDBConnector.PATH_USERDB_KEY)) {
        userDBPath = config.getProperty(UserDBConnector.PATH_USERDB_KEY);
      }
      if(config.containsKey(UserDBConnector.PATH_USERTABLE_KEY)) {
        userDBTablePath = config.getProperty(UserDBConnector.PATH_USERTABLE_KEY);
      }
    }
    
    if(args.length != 0) {
      if(args[0].equals("rebuild")) {
        DocStoreConnector docStore = new DocStoreConnectorImpl();
        docStore.connect();
        docStore.dropDatabase();
        docStore.createTopicCollection();
        docStore.createAdsCollection();
        
        //UserDBConnector userDB = UserDBConnector.getInstance();
        UserDBConnector userDB = new UserDBConnector(userDBPath, userDBTablePath, true);
        userDB.connect();
        userDB.dropTableTwitterUsers();
        userDB.createUserTable();
        LOGGER.info("Reset all Databases done!");
      }
    }
    
    TwitterUserDataProcessor userDataProcessor = new TwitterUserDataProcessor(neoPath, neoProp, userDBPath, userDBTablePath);
    userDataProcessor.connectNeo4J();
    userDataProcessor.start();
    userDataProcessor.disconnectNeo4J();
    
    TwitterTweetDataProcessor twitterDataProcessor = new TwitterTweetDataProcessor(neoPath, neoProp, userDBPath, userDBTablePath);
    twitterDataProcessor.connectNeo4J();
    twitterDataProcessor.start();    
    twitterDataProcessor.disconnectNeo4J();
    
  }
  
  private static Properties loadConfig() {
    try {
    InputStream resource = new FileInputStream("./" + ac.at.tuwien.tdm.commons.Constants.CONFIG_FILE_NAME);
    
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
