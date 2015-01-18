package ac.at.tuwien.tdm.processor;

public class TwitterProcessor {
  
  public TwitterProcessor() {
    this.addShutdownHook();
  }

  public static void main(String[] args) {
    
    
    
    TwitterUserDataProcessor userDataProcessor = new TwitterUserDataProcessor();
    userDataProcessor.connectNeo4J();
    userDataProcessor.process();
    userDataProcessor.disconnectNeo4J();
    
    TwitterTweetDataProcessor twitterDataProcessor = new TwitterTweetDataProcessor();
    twitterDataProcessor.connectNeo4J();
    twitterDataProcessor.process();
    
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
