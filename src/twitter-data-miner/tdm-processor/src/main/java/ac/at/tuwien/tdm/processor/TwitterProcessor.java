package ac.at.tuwien.tdm.processor;

public class TwitterProcessor {
  
  public TwitterProcessor() {
    this.addShutdownHook();
  }

  public static void main(String[] args) {
    
    TwitterUserDataProcessor userDataProcessor = new TwitterUserDataProcessor();

    userDataProcessor.process();
    //userDataProcessor.getUser(2298476980L);
    
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
