package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.job.FindFriendsJob.FindFriendsJobApproachEnum;
import ac.at.tuwien.tdm.twitter.connector.result.CursorListTaskResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public final class FindFriendsTask extends AbstractFriendsFollowersTask {

  private final FindFriendsJobApproachEnum approach;

  private final Long userId;

  private final Long cursorId;

  private FindFriendsTask(final FindFriendsJobApproachEnum approach, final Long userId, final Long cursorId) {
    this.approach = approach;
    this.userId = userId;
    this.cursorId = cursorId;
  }

  public static FindFriendsTask newInstanceForFirstRetrieval(final FindFriendsJobApproachEnum approach,
      final Long userId) {
    return new FindFriendsTask(approach, userId, -1l);
  }

  public static FindFriendsTask newInstanceForContinuedSearch(final FindFriendsJobApproachEnum approach,
      final Long userId, final Long cursorId) {
    return new FindFriendsTask(approach, userId, cursorId);
  }

  @Override
  public CursorListTaskResult<Long> execute() throws LimitReachedException, TwitterConnectorException,
      ConnectionException, HttpRetryProblemException {

    final Twitter twitter = TwitterAuthenticationService.getInstance().getTwitter();

    try {
      switch (approach) {
        case ID:
          return buildResult(twitter.friendsFollowers().getFriendsIDs(userId, cursorId));

        case LIST:
          return buildResult(twitter.friendsFollowers().getFriendsList(userId, cursorId));

        default:
          throw new IllegalArgumentException("Unsupported approach: " + approach);
      }
    } catch (final TwitterException e) {
      if (e.exceededRateLimitation()) {
        throw new LimitReachedException(e, e.getRateLimitStatus());
      } else if (e.isCausedByNetworkIssue()) {
        throw new ConnectionException(e);
      } else if (TwitterConnectorConstants.HTTP_RETRY_PROBLEMS.contains(e.getStatusCode())) {
        throw new HttpRetryProblemException(e);
      } else {
        throw new TwitterConnectorException(e);
      }
    }
  }
}
