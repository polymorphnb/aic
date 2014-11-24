package ac.at.tuwien.tdm.twitter.connector.api;

import ac.at.tuwien.tdm.twitter.connector.GsonInstance;

import java.util.ArrayList;
import java.util.List;

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

  private final int favoritesCount;

  private final int followersCount;

  private final int friendsCount;

  private final List<Long> followerUserIds;

  private final List<Long> friendsUserIds;

  public User(final long id, final String screenName, final String name, final String location, final String language,
      final int statusesCount, final int favoritesCount, final int followersCount, final int friendsCount) {
    this.id = id;
    this.screenName = screenName;
    this.name = name;
    this.location = location;
    this.language = language;
    this.statusesCount = statusesCount;
    this.favoritesCount = favoritesCount;
    this.followersCount = followersCount;
    this.friendsCount = friendsCount;
    followerUserIds = new ArrayList<>();
    friendsUserIds = new ArrayList<>();
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

  public int getFavoritesCount() {
    return favoritesCount;
  }

  public int getFollowersCount() {
    return followersCount;
  }

  public int getFriendsCount() {
    return friendsCount;
  }

  public List<Long> getFollowerUserIds() {
    return followerUserIds;
  }

  public List<Long> getFriendsUserIds() {
    return friendsUserIds;
  }

  public void addFollowerUserIds(final List<Long> userIds) {
    followerUserIds.addAll(userIds);
  }

  public void addFriendsUserIds(final List<Long> userIds) {
    friendsUserIds.addAll(userIds);
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
    return GsonInstance.get().toJson(this);
  }
}
