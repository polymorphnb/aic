package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.DtoFactory;
import ac.at.tuwien.tdm.twitter.connector.Maybe;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.ArrayList;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;

/**
 * Looks up user data of up to 100 users
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class LookUpUsersTask implements Task<List<User>> {

  private final List<Long> userIdsToLookUp;

  private LookUpUsersTask(final List<Long> userIdsToLookUp) {
    this.userIdsToLookUp = userIdsToLookUp;
  }

  public static LookUpUsersTask newInstance(final List<Long> userIdsToLookUp) {
    return new LookUpUsersTask(userIdsToLookUp);
  }

  @Override
  public List<User> execute() throws LimitReachedException, TwitterConnectorException {

    final List<User> users = new ArrayList<>(128);

    try {
      final Twitter twitter = TwitterFactory.getSingleton();
      final ResponseList<twitter4j.User> twitterUsers = twitter.lookupUsers(toPrimitiveArray(userIdsToLookUp));

      for (final twitter4j.User twitterUser : twitterUsers) {
        // dto factory performs sanity checks
        final Maybe<User> maybeUser = DtoFactory.createUserFromTwitterUser(twitterUser);

        if (maybeUser.isKnown()) {
          users.add(maybeUser.value());
        }
      }
    } catch (final TwitterException e) {
      if (e.exceededRateLimitation()) {
        throw new LimitReachedException(e.getRateLimitStatus());
      } else {
        throw new TwitterConnectorException(e);
      }
    }

    return users;
  }

  private long[] toPrimitiveArray(final List<Long> list) {
    final long[] arr = new long[list.size()];

    for (int i = 0; i < list.size(); i++) {
      arr[i] = list.get(i);
    }

    return arr;
  }
}
