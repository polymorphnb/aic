package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;

/**
 * Looks up user data of up to 100 users
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public class LookUpUsersJob implements Job<List<User>> {

  private final List<Long> userIdsToLookUp;

  private LookUpUsersJob(final Builder b) {
    this.userIdsToLookUp = b.userIdsToLookUp;
  }

  @Override
  public List<User> call() throws TwitterConnectorException {
    final LookUpUsersTask task = LookUpUsersTask.newInstance(userIdsToLookUp);

    try {
      return task.execute();
    } catch (final LimitReachedException e) {
      //FIXME
      throw new RuntimeException(e);
    }

    // FIXME return Collections.emptyList();
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
