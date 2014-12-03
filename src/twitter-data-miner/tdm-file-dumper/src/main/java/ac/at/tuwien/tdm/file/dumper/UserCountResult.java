package ac.at.tuwien.tdm.file.dumper;

public final class UserCountResult {

  private final long amountAuthorUsers;

  private final long amountFriends;

  private final long amountFollowers;

  public UserCountResult(final long amountAuthorUsers, final long amountFriends, final long amountFollowers) {
    this.amountAuthorUsers = amountAuthorUsers;
    this.amountFriends = amountFriends;
    this.amountFollowers = amountFollowers;
  }

  public long getAmountAuthorUsers() {
    return amountAuthorUsers;
  }

  public long getAmountFriends() {
    return amountFriends;
  }

  public long getAmountFollowers() {
    return amountFollowers;
  }

  public long getTotalAmountOfUsers() {
    return amountAuthorUsers + amountFriends + amountFollowers;
  }

  @Override
  public String toString() {
    return "UserCountResult [amountAuthorUsers=" + amountAuthorUsers + ", amountFriends=" + amountFriends
        + ", amountFollowers=" + amountFollowers + "]";
  }
}
