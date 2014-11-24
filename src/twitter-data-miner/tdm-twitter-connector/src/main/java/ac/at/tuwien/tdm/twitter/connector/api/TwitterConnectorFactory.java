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
    try {
      return new TwitterConnectorImpl();
    } catch (final Exception e) {
      throw new RuntimeException("Couldn't build twitter connector");
    }
  }
}
