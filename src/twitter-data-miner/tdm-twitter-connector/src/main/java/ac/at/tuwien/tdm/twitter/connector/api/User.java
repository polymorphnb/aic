package ac.at.tuwien.tdm.twitter.connector.api;

public final class User {

  private final long id;

  private final String screenName;

  private final String name;

  private final String location;

  private final String language;

  private final int statusesCount;

  private final int followersCount;

  public User(final long id, final String screenName, final String name, final String location, final String language,
      final int statusesCount, final int followersCount) {
    this.id = id;
    this.screenName = screenName;
    this.name = name;
    this.location = location;
    this.language = language;
    this.statusesCount = statusesCount;
    this.followersCount = followersCount;
  }

  public long getId() {
    return id;
  }

  public String getScreenName() {
    return screenName;
  }

  public String getName() {
    return name;
  }

  public String getLocation() {
    return location;
  }

  public String getLanguage() {
    return language;
  }

  public int getStatusesCount() {
    return statusesCount;
  }

  public int getFollowersCount() {
    return followersCount;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", screenName=" + screenName + ", name=" + name + ", location=" + location
        + ", language=" + language + ", statusesCount=" + statusesCount + ", followersCount=" + followersCount + "]";
  }
}
