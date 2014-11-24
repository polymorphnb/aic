package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.MapTaskResult;
import twitter4j.RateLimitStatus;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public final class GetRateLimitTask implements Task<MapTaskResult<String, RateLimitStatus>> {

  private GetRateLimitTask() {
  }

  public static GetRateLimitTask newInstance() {
    return new GetRateLimitTask();
  }

  @Override
  public MapTaskResult<String, RateLimitStatus> execute() throws LimitReachedException, TwitterConnectorException {

    final Twitter twitter = TwitterAuthenticationService.getInstance().getTwitter();

    try {
      return new MapTaskResult<>(null, twitter.getRateLimitStatus());
    } catch (final TwitterException e) {
      if (e.exceededRateLimitation()) {
        throw new LimitReachedException(e.getRateLimitStatus());
      } else {
        throw new TwitterConnectorException(e);
      }
    }
  }
}
