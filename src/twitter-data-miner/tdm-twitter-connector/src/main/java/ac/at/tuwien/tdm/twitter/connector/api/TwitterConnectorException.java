package ac.at.tuwien.tdm.twitter.connector.api;

/**
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TwitterConnectorException extends Exception {

  public TwitterConnectorException(final String msg) {
    super(msg);
  }

  public TwitterConnectorException(final Throwable t) {
    super(t);
  }

  public TwitterConnectorException(final String msg, final Throwable t) {
    super(msg, t);
  }
}
