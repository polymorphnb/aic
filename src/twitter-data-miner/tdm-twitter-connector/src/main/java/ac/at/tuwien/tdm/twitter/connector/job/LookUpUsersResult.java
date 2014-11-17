package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;

public final class LookUpUsersResult {

  private final List<User> users;

  public LookUpUsersResult(final List<User> users) {
    this.users = users;
  }

  public List<User> getUsers() {
    return users;
  }
}
