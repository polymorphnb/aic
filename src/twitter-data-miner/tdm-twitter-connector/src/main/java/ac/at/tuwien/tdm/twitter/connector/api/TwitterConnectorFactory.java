package ac.at.tuwien.tdm.twitter.connector.api;

/**
 * Simple factory for {@link TwitterConnector}
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TwitterConnectorFactory {

  private TwitterConnectorFactory() {
    // hide constructor
  }

  public static TwitterConnector createTwitterConnector() {
    return new TwitterConnectorImpl();
  }
}
