package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.DtoFactory;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

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

  public static SearchTweetsTask newInstanceForContinuingSearch(final Query query, final int tweetsPerPage) {
    return new SearchTweetsTask(null, false, query, tweetsPerPage);
  }

  public TweetSearchResult execute() throws LimitReachedException, TwitterException {

    final List<Tweet> tweets = new ArrayList<Tweet>(Integer.highestOneBit(tweetsPerPage) * 2);
    QueryResult result = null;

    try {
      final Twitter twitter = TwitterFactory.getSingleton();

      Query query = buildStatusSearchQuery();
      result = twitter.search(query);
      processResults(result, tweets);

    } catch (final TwitterException e) {
      if (e.exceededRateLimitation()) {
        throw new LimitReachedException();
      } else {
        throw e;
      }
    }

    return new TweetSearchResult(tweets, result.hasNext() ? result.nextQuery() : null);
  }

  private Query buildStatusSearchQuery() {
    if (this.query != null) {
      return this.query;
    }

    final String hashTagSearchTerm = (searchOnlyInHashTags ? "#" : "");

    final Query query = new Query(hashTagSearchTerm + searchTerm);
    query.setCount(tweetsPerPage);
    query.setLang("en");

    return query;
  }

  private void processResults(final QueryResult result, final List<Tweet> tweets) {
    for (final Status status : result.getTweets()) {

      final Status retweetedStatus = status.getRetweetedStatus();

      if (retweetedStatus != null) {
        tweets.add(buildTweet(retweetedStatus, false));
      }

      tweets.add(buildTweet(status, true));
    }
  }

  private Tweet buildTweet(final Status status, final boolean includeRetweetedStatus) {
    return DtoFactory.createTweetFromStatus(status, includeRetweetedStatus);
  }
}
