package ac.at.tuwien.tdm.twitter.connector.result;

import java.util.Map;

import twitter4j.RateLimitStatus;

public class MapTaskResult<T, V> extends TaskResult {

  private final Map<T, V> result;

  public MapTaskResult(final RateLimitStatus rateLimitStatus, final Map<T, V> result) {
    super(rateLimitStatus);
    this.result = result;
  }

  public Map<T, V> getResult() {
    return result;
  }
}
