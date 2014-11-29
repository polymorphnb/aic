package ac.at.tuwien.tdm.twitter.connector.result;

import ac.at.tuwien.tdm.commons.pojo.Tweet;

import java.util.List;

import twitter4j.Query;
import twitter4j.RateLimitStatus;

/**
 * Represents a tweet search result that may contain a following up query (next result page)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TweetSearchResult extends ListTaskResult<Tweet> {

  private final Query nextQuery;

  public TweetSearchResult(final RateLimitStatus rateLimitStatus, final List<Tweet> result, final Query nextQuery) {
    super(rateLimitStatus, result);
    this.nextQuery = nextQuery;
  }

  public Query getNextQuery() {
    return nextQuery;
  }
}
