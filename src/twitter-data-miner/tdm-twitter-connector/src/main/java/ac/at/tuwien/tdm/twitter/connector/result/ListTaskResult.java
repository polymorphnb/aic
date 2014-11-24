package ac.at.tuwien.tdm.twitter.connector.result;

import java.util.List;

import twitter4j.RateLimitStatus;

public class ListTaskResult<T> extends TaskResult {

  private final List<T> result;

  public ListTaskResult(final RateLimitStatus rateLimitStatus, final List<T> result) {
    super(rateLimitStatus);
    this.result = result;
  }

  public List<T> getResult() {
    return result;
  }
}
