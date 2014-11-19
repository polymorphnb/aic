package ac.at.tuwien.tdm.twitter.connector.api;

import ac.at.tuwien.tdm.twitter.connector.job.SearchTweetsJob;

import java.util.List;
import java.util.concurrent.Future;

/**
 * TODO use TwitterException to wrap runtime exceptions
 * 
 * Executes search requests (REST based) on twitter
 * 
 * <br />
 * <b>Use shutdownService() for cleanup</b>
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public interface TwitterConnector {

  /**
   * Finds tweets using the given search term, searching in 'free text' and hash tags. Returns an empty list if nothing
   * was found. Maximum amount of tweets returned is limited by
   * {@link SearchTweetsJob.DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH}.
   * 
   * @param searchTerm the search term
   *          <ul>
   *          <li>must not be null</li>
   *          <li>must not be blank</li>
   *          </ul>
   * @param searchOnlyInHashTags restricts search to hash tags only
   * @return a {@link Future} that returns a list of tweets, or an empty list if nothing was found
   */
  Future<List<Tweet>> findByKeyWord(String searchTerm, boolean searchOnlyInHashTags) throws TwitterConnectorException;

  /**
   * Finds tweets using the given search term, searching in 'free text' and hash tags. Returns an empty list if nothing
   * was found.
   * 
   * @param searchTerm the search term
   *          <ul>
   *          <li>must not be null</li>
   *          <li>must not be blank</li>
   *          </ul>
   * @param searchOnlyInHashTags restricts search to hash tags only
   * @param maxResults maximum number of tweets that should be returned
   * @return a {@link Future} that returns a list of tweets, or an empty list if nothing was found
   */
  Future<List<Tweet>> findByKeyWord(String searchTerm, boolean searchOnlyInHashTags, int maxResults)
      throws TwitterConnectorException;

  /**
   * Finds users using the given user ids. Limited to 100 users.
   * 
   * @param ids list of user ids that should be looked up. Must not be more than 100 ids
   * @return a {@link Future} that returns a list of users, or an empty list if nothing was found
   */
  Future<List<User>> lookUpUsersById(List<Long> ids) throws TwitterConnectorException;

  /**
   * Shuts the connector down (stopping 'running' threads)
   */
  void shutdownService();
}
