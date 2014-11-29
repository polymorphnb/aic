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

public final class FindFollowersJob extends AbstractJob<List<Long>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindFollowersJob.class);

  private final Long userIdToLookUp;

  private final Integer followersCount;

  private FindFollowersJobApproachEnum approach;

  private FindFollowersJobApproachEnum whichApproachWasChecked;

  private FindFollowersJob(final Builder b) {
    this.userIdToLookUp = b.userIdToLookUp;
    this.followersCount = b.followersCount;
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
              + ", followersCount: " + followersCount + ", foundIds: " + userIds.size() + ", remaining: "
              + e.getRemaining());
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (approach == null);

      FindFollowersTask task = FindFollowersTask.newInstanceForFirstRetrieval(approach, userIdToLookUp);
      CursorListTaskResult<Long> result = null;

      do {
        try {
          result = task.execute();
        } catch (final LimitReachedException e) {
          LOGGER.debug("Limit reached during first task execution, timestamp: " + e.getResetTimestamp()
              + ", followersCount: " + followersCount + ", foundIds: " + userIds.size() + ", remaining: "
              + e.getRemaining());
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (result == null);

      userIds.addAll(result.getResult());
      LOGGER.debug("Executed first task, followersCount: " + followersCount + ", userIdsSize: " + userIds.size());

      if (approach == FindFollowersJobApproachEnum.ID) {
        // max 5000 ids, do not continue
        return userIds;
      }

      checkRateLimit(result);

      if (result.hasNextResultPage()) {
        do {
          task = FindFollowersTask.newInstanceForContinuedSearch(approach, userIdToLookUp, result.getNextCursorId());

          try {
            result = task.execute();
            userIds.addAll(result.getResult());
            LOGGER.debug("Executed continuing task, followersCount: " + followersCount + ", userIdsSize: "
                + userIds.size());
            checkRateLimit(result);
          } catch (final LimitReachedException e) {
            LOGGER.debug("Limit reached during continuing task execution, timestamp: " + e.getResetTimestamp()
                + ", followersCount: " + followersCount + ", foundIds: " + userIds.size() + ", remaining: "
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
    return ("findFollowers, approach: " + (approach != null ? approach : whichApproachWasChecked));
  }

  private FindFollowersJobApproachEnum determineApproach() throws LimitReachedException, TwitterConnectorException,
      ConnectionException, HttpRetryProblemException {

    final MapTaskResult<String, RateLimitStatus> taskResult = GetRateLimitTask.newInstance().execute();
    final Map<String, RateLimitStatus> rateLimits = taskResult.getResult();

    if (followersCount <= TwitterConnectorConstants.ID_LIST_APPROACH_THRESHOLD) {
      final int remainingForListRequest = rateLimits.get(FindFollowersJobApproachEnum.LIST.getResourceName())
          .getRemaining();

      if (remainingForListRequest > 0) {
        return FindFollowersJobApproachEnum.LIST;
      }
    }

    final int remainingForIdRequest = rateLimits.get(FindFollowersJobApproachEnum.ID.getResourceName()).getRemaining();
    if (remainingForIdRequest > 0) {
      return FindFollowersJobApproachEnum.ID;
    }

    whichApproachWasChecked = FindFollowersJobApproachEnum.ID;
    throw new LimitReachedException(rateLimits.get(FindFollowersJob.FindFollowersJobApproachEnum.ID.getResourceName()));
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
