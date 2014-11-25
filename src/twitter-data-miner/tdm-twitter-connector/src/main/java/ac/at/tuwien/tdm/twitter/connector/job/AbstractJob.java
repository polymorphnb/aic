package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.Clock;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.result.TaskResult;

import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;

public abstract class AbstractJob<T> implements Job<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJob.class);

  protected void handleReachedLimit(final long resetTimestamp) throws TwitterException {
    LOGGER.info(String.format("Limit reached for '%s'", getRequestType()));

    final TwitterAuthenticationService authService = TwitterAuthenticationService.getInstance();
    authService.invalidateCredentials(resetTimestamp);
  }

  protected void checkRateLimit(final TaskResult taskResult) throws TwitterException {
    final RateLimitStatus rateLimitStatus = taskResult.getRateLimitStatus();

    if (rateLimitStatus == null) {
      return;
    }

    if (rateLimitStatus.getRemaining() == 0) {
      LOGGER.debug("Limit reached during checkRateLimit(), timestamp: "
          + ((taskResult.getRateLimitStatus().getResetTimeInSeconds() * 1000l) + 60l * 1000l) + ", remaining: "
          + taskResult.getRateLimitStatus().getRemaining());

      final Calendar c = Clock.currentTime();
      c.setTimeInMillis(((long) rateLimitStatus.getResetTimeInSeconds()) * 1000l);
      c.add(Calendar.MINUTE, 1);
      handleReachedLimit(c.getTimeInMillis());
    }
  }

  protected void handleConnectionError() {
    LOGGER.warn("Detected connection problem");

    final TwitterAuthenticationService authService = TwitterAuthenticationService.getInstance();
    authService.handleConnectionError();
  }

  protected abstract String getRequestType();
}
