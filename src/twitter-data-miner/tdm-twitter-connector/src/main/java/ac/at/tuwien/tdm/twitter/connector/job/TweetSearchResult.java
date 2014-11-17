package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;

import java.util.List;

import twitter4j.Query;

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
