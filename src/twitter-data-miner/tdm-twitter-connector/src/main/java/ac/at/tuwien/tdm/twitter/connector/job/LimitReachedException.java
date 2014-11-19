package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import twitter4j.RateLimitStatus;

/**
 * Thrown if the twitter api request limit is reached. Contains the amount of seconds till the rate limit is reset.
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class LimitReachedException extends Exception {

  private final int resetTimeInSeconds;

  public LimitReachedException(final RateLimitStatus rateLimitStatus) {
    resetTimeInSeconds = (rateLimitStatus == null ? TwitterConnectorConstants.TIME_WINDOW_IN_SECONDS : rateLimitStatus
        .getSecondsUntilReset());
  }

  public int getResetTimeInSeconds() {
    return resetTimeInSeconds;
  }

  @Override
  public String toString() {
    return "LimitReachedException [resetTimeInSeconds=" + resetTimeInSeconds + "]";
  }
}
