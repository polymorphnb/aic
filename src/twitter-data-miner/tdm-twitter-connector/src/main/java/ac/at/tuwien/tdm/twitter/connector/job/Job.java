package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;

import java.util.concurrent.Callable;

/**
 * Represents a request against the twitter rest api (which actually can be more than one request internally)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public interface Job<T> extends Callable<T> {

  @Override
  T call() throws TwitterConnectorException;

}
