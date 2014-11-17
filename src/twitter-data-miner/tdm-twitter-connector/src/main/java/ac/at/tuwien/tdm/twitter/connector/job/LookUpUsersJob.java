package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;

public class LookUpUsersJob implements Job<List<User>> {

  private final List<Long> userIdsToLookUp;

  private LookUpUsersJob(final Builder b) {
    this.userIdsToLookUp = b.userIdsToLookUp;
  }

  @Override
  public List<User> call() throws Exception {
    final LookUpUsersTask task = LookUpUsersTask.newInstance(userIdsToLookUp);
    return task.execute().getUsers();
  }

  public static class Builder {

    private final List<Long> userIdsToLookUp;

    public Builder(final List<Long> userIdsToLookUp) {
      if (userIdsToLookUp.size() > 100) {
        throw new IllegalArgumentException("Max. 100 user ids allowed for lookup");
      }

      this.userIdsToLookUp = userIdsToLookUp;
    }

    public LookUpUsersJob build() {
      return new LookUpUsersJob(this);
    }
  }
}
