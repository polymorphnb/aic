package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.CursorListTaskResult;
import ac.at.tuwien.tdm.twitter.connector.result.MapTaskResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;

public final class FindFriendsJob extends AbstractJob<List<Long>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindFriendsJob.class);

  private final Long userIdToLookUp;

  private final Integer friendsCount;

  private FindFriendsJobApproachEnum approach;

  private FindFriendsJobApproachEnum whichApproachWasChecked;

  private FindFriendsJob(final Builder b) {
    this.userIdToLookUp = b.userIdToLookUp;
    this.friendsCount = b.friendsCount;
  }

  @Override
  public List<Long> call() throws TwitterConnectorException {

    final List<Long> userIds = new ArrayList<Long>(512);

    try {
      do {
        try {
          approach = determineApproach();
        } catch (final LimitReachedException e) {
          LOGGER.debug("Limit reached during determineApproach(), timestamp: " + e.getResetTimestamp()
              + ", friendsCount: " + friendsCount + ", foundIds: " + userIds.size() + ", remaining: "
              + e.getRemaining());
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (approach == null);

      FindFriendsTask task = FindFriendsTask.newInstanceForFirstRetrieval(approach, userIdToLookUp);
      CursorListTaskResult<Long> result = null;

      do {
        try {
          result = task.execute();
        } catch (final LimitReachedException e) {
          LOGGER.debug("Limit reached during first task execution, timestamp: " + e.getResetTimestamp()
              + ", friendsCount: " + friendsCount + ", foundIds: " + userIds.size() + ", remaining: "
              + e.getRemaining());
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (result == null);

      userIds.addAll(result.getResult());
      LOGGER.debug("Executed first task, friendsCount: " + friendsCount + ", userIdsSize: " + userIds.size());

      if (approach == FindFriendsJobApproachEnum.ID) {
        // max 5000 ids, do not continue
        return userIds;
      }

      checkRateLimit(result);

      if (result.hasNextResultPage()) {
        do {
          task = FindFriendsTask.newInstanceForContinuedSearch(approach, userIdToLookUp, result.getNextCursorId());

          try {
            result = task.execute();
            userIds.addAll(result.getResult());
            LOGGER
                .debug("Executed continuing task, friendsCount: " + friendsCount + ", userIdsSize: " + userIds.size());
            checkRateLimit(result);
          } catch (final LimitReachedException e) {
            LOGGER.debug("Limit reached during continuing task execution, timestamp: " + e.getResetTimestamp()
                + ", friendsCount: " + friendsCount + ", foundIds: " + userIds.size() + ", remaining: "
                + e.getRemaining());
            handleReachedLimit(e.getResetTimestamp());
          } catch (final ConnectionException e) {
            handleConnectionError();
          } catch (final HttpRetryProblemException e) {
            handleHttpProblem(e.getResetTimestamp());
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
    return ("findFriends, approach: " + (approach != null ? approach : whichApproachWasChecked));
  }

  private FindFriendsJobApproachEnum determineApproach() throws LimitReachedException, TwitterConnectorException,
      ConnectionException, HttpRetryProblemException {

    final MapTaskResult<String, RateLimitStatus> taskResult = GetRateLimitTask.newInstance().execute();
    final Map<String, RateLimitStatus> rateLimits = taskResult.getResult();

    if (friendsCount <= TwitterConnectorConstants.ID_LIST_APPROACH_THRESHOLD) {
      final int remainingForListRequest = rateLimits.get(FindFriendsJobApproachEnum.LIST.getResourceName())
          .getRemaining();

      if (remainingForListRequest > 0) {
        return FindFriendsJobApproachEnum.LIST;
      }
    }

    final int remainingForIdRequest = rateLimits.get(FindFriendsJobApproachEnum.ID.getResourceName()).getRemaining();
    if (remainingForIdRequest > 0) {
      return FindFriendsJobApproachEnum.ID;
    }

    whichApproachWasChecked = FindFriendsJobApproachEnum.ID;
    throw new LimitReachedException(rateLimits.get(FindFriendsJobApproachEnum.ID.getResourceName()));
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
