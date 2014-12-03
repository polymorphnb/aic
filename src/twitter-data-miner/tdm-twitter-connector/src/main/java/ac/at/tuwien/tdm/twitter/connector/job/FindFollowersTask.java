package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.job.FindFollowersJob.FindFollowersJobApproachEnum;
import ac.at.tuwien.tdm.twitter.connector.result.CursorListTaskResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public final class FindFollowersTask extends AbstractFriendsFollowersTask {

  private final FindFollowersJobApproachEnum approach;

  private final long userId;

  private final long cursorId;

  private FindFollowersTask(final FindFollowersJobApproachEnum approach, final long userId, final long cursorId) {
    this.approach = approach;
    this.userId = userId;
    this.cursorId = cursorId;
  }

  public static FindFollowersTask newInstanceForFirstRetrieval(final FindFollowersJobApproachEnum approach,
      final long userId) {
    return new FindFollowersTask(approach, userId, -1l);
  }

  public static FindFollowersTask newInstanceForContinuedSearch(final FindFollowersJobApproachEnum approach,
      final long userId, final long cursorId) {
    return new FindFollowersTask(approach, userId, cursorId);
  }

  @Override
  public CursorListTaskResult<Long> execute() throws LimitReachedException, TwitterConnectorException,
      ConnectionException, HttpRetryProblemException {

    final Twitter twitter = TwitterAuthenticationService.getInstance().getTwitter();

    try {
      switch (approach) {
        case ID:
          return buildResult(twitter.friendsFollowers().getFollowersIDs(userId, cursorId));

        case LIST:
          return buildResult(twitter.friendsFollowers().getFollowersList(userId, cursorId));

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
