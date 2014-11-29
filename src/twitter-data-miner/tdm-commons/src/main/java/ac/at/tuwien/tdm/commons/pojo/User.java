package ac.at.tuwien.tdm.commons.pojo;

import ac.at.tuwien.tdm.commons.GsonInstance;

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
// approx. 30008 bytes
public final class User {

  // unique and used for equals & hashCode
  private long id; // 8 bytes

  private String screenName; // approx. 12 * 2 bytes

  private String name; // approx. 12 * 2 bytes

  private String location; // approx. 12 * 2 bytes

  private String language; // approx. 3 * 2 bytes

  private int statusesCount; // 4 bytes

  private int favoritesCount; // 4 bytes

  private int followersCount; // 4 bytes

  private int friendsCount; // 4 bytes

  private List<Long> followerUserIds; // approx. 2500 * 8 bytes

  private List<Long> friendsUserIds; // approx. 1250 * 8 bytes

  public User() {
    id = -1l;
    statusesCount = 0;
    favoritesCount = 0;
    followersCount = 0;
    friendsCount = 0;
    followerUserIds = new ArrayList<Long>();
    friendsUserIds = new ArrayList<Long>();
  }

  public User(final long id, final String screenName, final String name, final String location, final String language,
      final int statusesCount, final int favoritesCount, final int followersCount, final int friendsCount) {
    this();
    this.id = id;
    this.screenName = screenName;
    this.name = name;
    this.location = location;
    this.language = language;
    this.statusesCount = statusesCount;
    this.favoritesCount = favoritesCount;
    this.followersCount = followersCount;
    this.friendsCount = friendsCount;

  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getScreenName() {
    return screenName;
  }

  public void setScreenName(String screenName) {
    this.screenName = screenName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public int getStatusesCount() {
    return statusesCount;
  }

  public void setStatusesCount(int statusesCount) {
    this.statusesCount = statusesCount;
  }

  public int getFavoritesCount() {
    return favoritesCount;
  }

  public void setFavoritesCount(int favoritesCount) {
    this.favoritesCount = favoritesCount;
  }

  public int getFollowersCount() {
    return followersCount;
  }

  public void setFollowersCount(int followersCount) {
    this.followersCount = followersCount;
  }

  public int getFriendsCount() {
    return friendsCount;
  }

  public void setFriendsCount(int friendsCount) {
    this.friendsCount = friendsCount;
  }

  public List<Long> getFollowerUserIds() {
    return followerUserIds;
  }

  public void setFollowerUserIds(List<Long> followerUserIds) {
    this.followerUserIds = followerUserIds;
  }

  public List<Long> getFriendsUserIds() {
    return friendsUserIds;
  }

  public void setFriendsUserIds(List<Long> friendsUserIds) {
    this.friendsUserIds = friendsUserIds;
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
