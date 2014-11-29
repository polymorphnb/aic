package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.commons.Clock;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;

import java.util.Calendar;

import twitter4j.RateLimitStatus;

/**
 * Thrown if the twitter api request limit is reached. Contains the timestamp when the rate limit is reset.
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class LimitReachedException extends Exception {

  private final long resetTimestamp;

  private final long remaining;

  public LimitReachedException(final long resetTimestamp) {
    this.resetTimestamp = resetTimestamp;
    remaining = -1l;
  }

  public LimitReachedException(final RateLimitStatus rateLimitStatus) {
    resetTimestamp = convertToResetTimestamp(rateLimitStatus);
    remaining = (rateLimitStatus == null ? -1l : rateLimitStatus.getRemaining());
  }

  public LimitReachedException(final Throwable t, final RateLimitStatus rateLimitStatus) {
    super(t);
    resetTimestamp = convertToResetTimestamp(rateLimitStatus);
    remaining = (rateLimitStatus == null ? -1l : rateLimitStatus.getRemaining());
  }

  public long getResetTimestamp() {
    return resetTimestamp;
  }

  public long getRemaining() {
    return remaining;
  }

  private long convertToResetTimestamp(final RateLimitStatus rateLimitStatus) {
    final Calendar time = Clock.currentTime();

    if (rateLimitStatus != null) {
      time.setTimeInMillis(((long) rateLimitStatus.getResetTimeInSeconds()) * 1000l);
    } else {
      time.add(Calendar.SECOND, TwitterConnectorConstants.TIME_WINDOW_IN_SECONDS);
    }

    time.add(Calendar.MINUTE, 1);

    return time.getTimeInMillis();
  }

  @Override
  public String toString() {
    return "LimitReachedException [resetTimeStamp=" + resetTimestamp + "]";
  }
}
