package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.CursorListTaskResult;
import ac.at.tuwien.tdm.twitter.connector.result.MapTaskResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;

public final class FindFriendsJob extends AbstractJob<List<Long>> {

  private final Long userIdToLookUp;

  private final Integer friendsCount;

  private FindFriendsJobApproachEnum approach;

  private FindFriendsJob(final Builder b) {
    this.userIdToLookUp = b.userIdToLookUp;
    this.friendsCount = b.friendsCount;
  }

  @Override
  public List<Long> call() throws TwitterConnectorException {

    final List<Long> userIds = new ArrayList<>(32);

    try {
      do {
        try {
          approach = determineApproach();
        } catch (final LimitReachedException e) {
          approach = FindFriendsJobApproachEnum.LIST; // only for log message
          handleReachedLimit(e.getResetTimestamp());
          approach = null;
        } catch (final ConnectionException e) {
          handleConnectionError();
        }
      } while (approach == null);

      FindFriendsTask task = FindFriendsTask.newInstanceForFirstRetrieval(approach, userIdToLookUp);
      CursorListTaskResult<Long> result = null;

      do {
        try {
          result = task.execute();
        } catch (final LimitReachedException e) {
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        }
      } while (result == null);

      userIds.addAll(result.getResult());
      checkRateLimit(result);

      if (result.hasNextResultPage()) {
        do {
          task = FindFriendsTask.newInstanceForContinuedSearch(approach, userIdToLookUp, result.getNextCursorId());

          try {
            result = task.execute();
            userIds.addAll(result.getResult());
            checkRateLimit(result);
          } catch (final LimitReachedException e) {
            handleReachedLimit(e.getResetTimestamp());
          } catch (final ConnectionException e) {
            handleConnectionError();
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
    return ("findFriends, approach: " + approach);
  }

  private FindFriendsJobApproachEnum determineApproach() throws LimitReachedException, TwitterConnectorException,
      ConnectionException {

    final MapTaskResult<String, RateLimitStatus> taskResult = GetRateLimitTask.newInstance().execute();
    final Map<String, RateLimitStatus> rateLimits = taskResult.getResult();
    final int remainingForIdRequest = rateLimits.get(FindFriendsJobApproachEnum.ID.getResourceName()).getRemaining();

    if (friendsCount > TwitterConnectorConstants.ID_LIST_APPROACH_THRESHOLD) {
      if (remainingForIdRequest > 0) {
        return FindFriendsJobApproachEnum.ID;
      }

      throw new LimitReachedException(rateLimits.get(FindFriendsJobApproachEnum.ID.getResourceName()));
    } else {
      final int remainingForListRequest = rateLimits.get(FindFriendsJobApproachEnum.LIST.getResourceName())
          .getRemaining();

      if (remainingForListRequest > 0) {
        return FindFriendsJobApproachEnum.LIST;
      }

      throw new LimitReachedException(rateLimits.get(FindFriendsJobApproachEnum.LIST.getResourceName()));
    }
  }

  public static class Builder {

    private final Long userIdToLookUp;

    // optional
    private Integer friendsCount = 20;

    public Builder(final Long userIdToLookUp) {
      this.userIdToLookUp = userIdToLookUp;
    }

    public Builder withFriendsCount(final int friendsCount) {
      this.friendsCount = friendsCount;
      return this;
    }

    public FindFriendsJob build() {
      return new FindFriendsJob(this);
    }
  }

  public static enum FindFriendsJobApproachEnum {
    ID("/friends/ids"), LIST("/friends/list");

    private final String resourceName;

    private FindFriendsJobApproachEnum(final String resourceName) {
      this.resourceName = resourceName;
    }

    public String getResourceName() {
      return resourceName;
    }
  }
}
