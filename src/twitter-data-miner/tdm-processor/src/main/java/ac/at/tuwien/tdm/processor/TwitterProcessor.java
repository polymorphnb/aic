package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.docstore.DocStoreConnector;
import ac.at.tuwien.tdm.docstore.DocStoreConnectorImpl;
import ac.at.tuwien.tdm.userdb.UserDBConnector;

public class TwitterProcessor {
  
  public TwitterProcessor() {
    this.addShutdownHook();
  }

  public static void main(String[] args) {
    
    if(args.length != 0) {
      if(args[0].equals("rebuild")) {
        DocStoreConnector docStore = new DocStoreConnectorImpl();
        docStore.connect();
        docStore.dropDatabase();
        docStore.createTopicCollection();
        docStore.createAdsCollection();
        
        UserDBConnector userDB = UserDBConnector.getInstance();
        userDB.connect();
        userDB.dropTableTwitterUsers();
        userDB.createUserTable();
        System.out.println("RESET ALL DATABASES");
      }
    }
    
    TwitterUserDataProcessor userDataProcessor = new TwitterUserDataProcessor();
    userDataProcessor.connectNeo4J();
    userDataProcessor.start();
    userDataProcessor.disconnectNeo4J();
    
    TwitterTweetDataProcessor twitterDataProcessor = new TwitterTweetDataProcessor();
    twitterDataProcessor.connectNeo4J();
    twitterDataProcessor.start();    
    twitterDataProcessor.disconnectNeo4J();
    
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
