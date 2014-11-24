package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.CursorListTaskResult;
import ac.at.tuwien.tdm.twitter.connector.result.MapTaskResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;

public final class FindFollowersJob extends AbstractJob<List<Long>> {

  private final Long userIdToLookUp;

  private final Integer followersCount;

  private FindFollowersJobApproachEnum approach;

  private FindFollowersJob(final Builder b) {
    this.userIdToLookUp = b.userIdToLookUp;
    this.followersCount = b.followersCount;
  }

  @Override
  public List<Long> call() throws TwitterConnectorException {

    final List<Long> userIds = new ArrayList<>(32);

    try {

      do {
        try {
          approach = determineApproach();
        } catch (final LimitReachedException e) {
          approach = FindFollowersJobApproachEnum.LIST; // only for log message
          handleReachedLimit(e.getResetTimestamp());
          approach = null;
        }
      } while (approach == null);

      FindFollowersTask task = FindFollowersTask.newInstanceForFirstRetrieval(approach, userIdToLookUp);
      CursorListTaskResult<Long> result = null;

      do {
        try {
          result = task.execute();
        } catch (final LimitReachedException e) {
          handleReachedLimit(e.getResetTimestamp());
        }
      } while (result == null);

      userIds.addAll(result.getResult());
      checkRateLimit(result);

      if (result.hasNextResultPage()) {
        do {
          task = FindFollowersTask.newInstanceForContinuedSearch(approach, userIdToLookUp, result.getNextCursorId());

          try {
            result = task.execute();
            userIds.addAll(result.getResult());
            checkRateLimit(result);
          } catch (final LimitReachedException e) {
            handleReachedLimit(e.getResetTimestamp());
          }
        } while (result.hasNextResultPage());
      }
    } catch (final TwitterException e) {
      throw new TwitterConnectorException(e);
    }

    return userIds;
  }

  @Override
  protected String getRequestType() {
    return ("findFollowers, approach: " + approach);
  }

  private FindFollowersJobApproachEnum determineApproach() throws LimitReachedException, TwitterConnectorException {

    final MapTaskResult<String, RateLimitStatus> taskResult = GetRateLimitTask.newInstance().execute();
    final Map<String, RateLimitStatus> rateLimits = taskResult.getResult();
    final int remainingForIdRequest = rateLimits.get(FindFollowersJobApproachEnum.ID.getResourceName()).getRemaining();

    if (followersCount > TwitterConnectorConstants.ID_LIST_APPROACH_THRESHOLD) {
      if (remainingForIdRequest > 0) {
        return FindFollowersJobApproachEnum.ID;
      }

      throw new LimitReachedException(
          rateLimits.get(FindFollowersJob.FindFollowersJobApproachEnum.ID.getResourceName()));
    } else {
      final int remainingForListRequest = rateLimits.get(FindFollowersJobApproachEnum.LIST.getResourceName())
          .getRemaining();

      if (remainingForListRequest > 0) {
        return FindFollowersJobApproachEnum.LIST;
      }

      throw new LimitReachedException(rateLimits.get(FindFollowersJob.FindFollowersJobApproachEnum.LIST
          .getResourceName()));
    }
  }

  public static class Builder {

    private final Long userIdToLookUp;

    // optional
    private Integer followersCount = 20;

    public Builder(final Long userIdToLookUp) {
      this.userIdToLookUp = userIdToLookUp;
    }

    public Builder withFollowersCount(final int followersCount) {
      this.followersCount = followersCount;
      return this;
    }

    public FindFollowersJob build() {
      return new FindFollowersJob(this);
    }
  }

  public static enum FindFollowersJobApproachEnum {
    ID("/followers/ids"), LIST("/followers/list");

    private final String resourceName;

    private FindFollowersJobApproachEnum(final String resourceName) {
      this.resourceName = resourceName;
    }

    public String getResourceName() {
      return resourceName;
    }
  }
}
