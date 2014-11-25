package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.job.FindFollowersJob.FindFollowersJobApproachEnum;
import ac.at.tuwien.tdm.twitter.connector.result.CursorListTaskResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public final class FindFollowersTask extends AbstractFriendsFollowersTask {

  private final FindFollowersJobApproachEnum approach;

  private final Long userId;

  private final Long cursorId;

  private FindFollowersTask(final FindFollowersJobApproachEnum approach, final Long userId, final Long cursorId) {
    this.approach = approach;
    this.userId = userId;
    this.cursorId = cursorId;
  }

  public static FindFollowersTask newInstanceForFirstRetrieval(final FindFollowersJobApproachEnum approach,
      final Long userId) {
    return new FindFollowersTask(approach, userId, -1l);
  }

  public static FindFollowersTask newInstanceForContinuedSearch(final FindFollowersJobApproachEnum approach,
      final Long userId, final Long cursorId) {
    return new FindFollowersTask(approach, userId, cursorId);
  }

  @Override
  public CursorListTaskResult<Long> execute() throws LimitReachedException, TwitterConnectorException,
      ConnectionException {

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
        throw new LimitReachedException(e.getRateLimitStatus());
      } else if (e.isCausedByNetworkIssue()) {
        throw new ConnectionException(e);
      } else {
        throw new TwitterConnectorException(e);
      }
    }
  }
}
