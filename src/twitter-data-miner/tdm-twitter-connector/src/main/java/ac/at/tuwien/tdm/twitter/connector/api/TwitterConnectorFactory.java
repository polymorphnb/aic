package ac.at.tuwien.tdm.twitter.connector.api;


public final class TwitterConnectorFactory {

  private TwitterConnectorFactory() {
    // hide constructor
  }

  public static TwitterConnector createTwitterConnector() {
    return new TwitterConnectorImpl();
  }
}
