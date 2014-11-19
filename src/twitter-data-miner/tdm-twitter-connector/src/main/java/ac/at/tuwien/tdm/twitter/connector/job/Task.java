package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;

/**
 * Represents a (sub)request against twitter and is a part of {@link Job}
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public interface Task<T> {

  /**
   * Executes a request against twitter and returns the result
   * 
   * @return request result
   * @throws LimitReachedException thrown if the twitter api limit (requests per 15 min window) is reached
   * @throws TwitterConnectorException some kind of request executing error
   */
  T execute() throws LimitReachedException, TwitterConnectorException;

}
