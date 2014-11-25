package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.Defense;
import ac.at.tuwien.tdm.twitter.connector.DtoFactory;
import ac.at.tuwien.tdm.twitter.connector.Maybe;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.TweetSearchResult;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Looks for up to 'tweetsPerPage' tweets (tweets per result page) containing a given search term. <br />
 * default value: {@link TwitterConnectorConstants.DEFAULT_AMOUNT_OF_TWEETS_PER_RESULT_PAGE}
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class SearchTweetsTask implements Task<TweetSearchResult> {

  private final String searchTerm;

  private final boolean searchOnlyInHashTags;

  private final int tweetsPerPage;

  private final Query query;

  private SearchTweetsTask(final String searchTerm, final boolean searchOnlyInHashTags, final Query query,
      final int tweetsPerPage) {
    this.searchTerm = searchTerm;
    this.searchOnlyInHashTags = searchOnlyInHashTags;
    this.query = query;
    this.tweetsPerPage = tweetsPerPage;
  }

  public static SearchTweetsTask newInstanceForFirstSearch(final SearchTweetsJob jobReference, final int tweetsPerPage) {
    return new SearchTweetsTask(jobReference.getSearchTerm(), jobReference.isSearchOnlyInHashTags(), null,
        tweetsPerPage);
  }

  public static SearchTweetsTask newInstanceForContinuingSearch(final String searchTerm, final Query query,
      final int tweetsPerPage) {
    Defense.notNull("Query", query);
    return new SearchTweetsTask(searchTerm, false, query, tweetsPerPage);
  }

  public TweetSearchResult execute() throws LimitReachedException, TwitterConnectorException, ConnectionException,
      HttpRetryProblemException {

    final List<Tweet> tweets = new ArrayList<Tweet>(Integer.highestOneBit(tweetsPerPage) * 2);
    QueryResult result = null;

    try {
      final Twitter twitter = TwitterAuthenticationService.getInstance().getTwitter();

      Query query = (this.query != null ? this.query : buildStatusSearchQuery());
      result = twitter.search(query);
      processResults(result, tweets);

    } catch (final TwitterException e) {
      if (e.exceededRateLimitation()) {
        throw new LimitReachedException(e, e.getRateLimitStatus());
      } else if (e.isCausedByNetworkIssue()) {
        throw new ConnectionException(e);
      } else if (TwitterConnectorConstants.HTTP_RETRY_PROBLEMS.contains(e.getStatusCode())) {
        throw new HttpRetryProblemException(e);
      } else {
        throw new TwitterConnectorException(e);
      }
    }

    final boolean isNextQueryAvailable = (result != null && result.hasNext());
    return new TweetSearchResult(result.getRateLimitStatus(), tweets, isNextQueryAvailable ? result.nextQuery() : null);
  }

  private Query buildStatusSearchQuery() {
    final String hashTagSearchTerm = (searchOnlyInHashTags ? "#" : "");

    final Query query = new Query(hashTagSearchTerm + searchTerm);
    query.setCount(tweetsPerPage);
    query.setLang(TwitterConnectorConstants.TWEET_LANGUAGE);

    return query;
  }

  private void processResults(final QueryResult result, final List<Tweet> tweets) {
    for (final Status status : result.getTweets()) {

      final Status retweetedStatus = status.getRetweetedStatus();

      if (retweetedStatus != null) {
        addStatus(tweets, retweetedStatus, false);
      }

      addStatus(tweets, status, true);
    }
  }

  private void addStatus(final List<Tweet> tweets, final Status status, final boolean includeRetweetedStatus) {
    // dto factory performs sanity checks
    final Maybe<Tweet> maybeTweet = DtoFactory.createTweetFromStatus(searchTerm, status, includeRetweetedStatus);

    if (maybeTweet.isKnown()) {
      tweets.add(maybeTweet.value());
    }
  }
}
