package ac.at.tuwien.tdm.twitter.connector.api;

/**
 * DTO for user data.
 * 
 * <ul>
 * <li>Encapsulated objects may be null</li>
 * <li>Encapsulated objects are immutable</li>
 * </ul>
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class User {

  // unique and used for equals & hashCode
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
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (id ^ (id >>> 32));
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }

    if (obj == null) {
      return false;
    }

    if (!(obj instanceof User)) {
      return false;
    }

    final User other = (User) obj;

    if (id != other.id) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return "User [id=" + id + ", screenName=" + screenName + ", name=" + name + ", location=" + location
        + ", language=" + language + ", statusesCount=" + statusesCount + ", followersCount=" + followersCount + "]";
  }
}
