package ac.at.tuwien.tdm.twitter.connector.result;

import java.util.List;

import twitter4j.RateLimitStatus;

public class CursorListTaskResult<T> extends ListTaskResult<T> {

  private final Long nextCursorId;

  public CursorListTaskResult(final RateLimitStatus rateLimitStatus, final List<T> result, final Long nextCursorId) {
    super(rateLimitStatus, result);
    this.nextCursorId = nextCursorId;
  }

  public Long getNextCursorId() {
    return nextCursorId;
  }

  public boolean hasNextResultPage() {
    return nextCursorId != null;
  }
}
