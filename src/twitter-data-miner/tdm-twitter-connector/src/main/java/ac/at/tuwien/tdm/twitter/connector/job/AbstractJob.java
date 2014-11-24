package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.result.TaskResult;

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
      handleReachedLimit(((long)rateLimitStatus.getResetTimeInSeconds()) * 1000l);
    }
  }

  protected abstract String getRequestType();
}
