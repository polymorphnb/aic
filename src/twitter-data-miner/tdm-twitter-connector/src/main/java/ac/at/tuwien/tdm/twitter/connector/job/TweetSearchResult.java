package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;

import java.util.List;

import twitter4j.Query;

/**
 * Represents a tweet search result that may contain a following up query (next result page)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TweetSearchResult {

  private final List<Tweet> tweets;

  private final Query nextQuery;

  public TweetSearchResult(final List<Tweet> tweets, final Query nextQuery) {
    this.tweets = tweets;
    this.nextQuery = nextQuery;
  }

  public List<Tweet> getTweets() {
    return tweets;
  }

  public Query getNextQuery() {
    return nextQuery;
  }
}
