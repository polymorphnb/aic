package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.commons.Maybe;
import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.DtoFactory;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.ListTaskResult;

import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Looks up user data of up to 100 users
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class LookUpUsersTask implements Task<ListTaskResult<User>> {

  private final List<Long> userIdsToLookUp;

  private LookUpUsersTask(final List<Long> userIdsToLookUp) {
    this.userIdsToLookUp = userIdsToLookUp;
  }

  public static LookUpUsersTask newInstance(final List<Long> userIdsToLookUp) {
    return new LookUpUsersTask(userIdsToLookUp);
  }

  @Override
  public ListTaskResult<User> execute() throws LimitReachedException, TwitterConnectorException, ConnectionException,
      HttpRetryProblemException {

    final List<User> users = new ArrayList<User>(128);
    ResponseList<twitter4j.User> twitterUsers;

    try {
      final Twitter twitter = TwitterAuthenticationService.getInstance().getTwitter();
      twitterUsers = twitter.lookupUsers(toPrimitiveArray(userIdsToLookUp));

      for (final twitter4j.User twitterUser : twitterUsers) {
        // dto factory performs sanity checks
        final Maybe<User> maybeUser = DtoFactory.createUserFromTwitterUser(twitterUser);

        if (maybeUser.isKnown()) {
          users.add(maybeUser.value());
        }
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

    return new ListTaskResult<User>(twitterUsers.getRateLimitStatus(), users);
  }

  private long[] toPrimitiveArray(final List<Long> list) {
    final long[] arr = new long[list.size()];

    for (int i = 0; i < list.size(); i++) {
      arr[i] = list.get(i);
    }

    return arr;
  }
}
