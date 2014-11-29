package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.ListTaskResult;

import java.util.List;

import twitter4j.TwitterException;

/**
 * Looks up user data of up to 100 users
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public class LookUpUsersJob extends AbstractJob<List<User>> {

  private final List<Long> userIdsToLookUp;

  private LookUpUsersJob(final Builder b) {
    this.userIdsToLookUp = b.userIdsToLookUp;
  }

  @Override
  public List<User> call() throws TwitterConnectorException {
    final LookUpUsersTask task = LookUpUsersTask.newInstance(userIdsToLookUp);

    ListTaskResult<User> result = null;

    try {
      do {
        try {
          result = task.execute();
        } catch (final LimitReachedException e) {
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (result == null);

      checkRateLimit(result);
    } catch (final TwitterException e) {
      throw new TwitterConnectorException(e);
    }

    return result.getResult();
  }

  @Override
  protected String getRequestType() {
    return "lookUpUsers";
  }

  public static class Builder {

    private final List<Long> userIdsToLookUp;

    public Builder(final List<Long> userIdsToLookUp) {
      if (userIdsToLookUp.size() > 100) {
        throw new IllegalArgumentException(String.format(
            "List with userIdsToLookUp must not contain more than 100 values. Actual size: ", userIdsToLookUp.size()));
      }

      this.userIdsToLookUp = userIdsToLookUp;
    }

    public LookUpUsersJob build() {
      return new LookUpUsersJob(this);
    }
  }
}
