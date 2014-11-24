package ac.at.tuwien.tdm.twitter.connector.result;

import twitter4j.RateLimitStatus;

public abstract class TaskResult {

  private final RateLimitStatus rateLimitStatus;

  public TaskResult(final RateLimitStatus rateLimitStatus) {
    this.rateLimitStatus = rateLimitStatus;
  }

  public RateLimitStatus getRateLimitStatus() {
    return rateLimitStatus;
  }
}
