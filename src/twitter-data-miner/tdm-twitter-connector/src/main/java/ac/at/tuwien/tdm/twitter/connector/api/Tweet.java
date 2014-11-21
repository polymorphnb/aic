package ac.at.tuwien.tdm.twitter.connector.api;

import ac.at.tuwien.tdm.twitter.connector.GsonInstance;

/**
 * DTO for tweet data.
 * 
 * <ul>
 * <li>Encapsulated objects may be null</li>
 * <li>Encapsulated objects are immutable</li>
 * <li>non-valid primitive number references are -1</li>
 * </ul>
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class Tweet {

  // unique and used for equals & hashCode
  private final long id;

  private final String content;

  // not to persist, only used by other processing logic
  private transient final User authorUser;

  private final long authorUserId;

  private final long repliedToUserId;

  private final long retweetedFromUserId;

  private final int favoritedCount;

  private final int retweetedCount;

  public Tweet(final long id, final String content, final User authorUser, final long repliedToUserId,
      final long retweetedFromUserId, final int favoritedCount, final int retweetedCount) {
    this.id = id;
    this.content = content;
    this.authorUser = authorUser;
    this.authorUserId = authorUser.getId();
    this.repliedToUserId = repliedToUserId;
    this.retweetedFromUserId = retweetedFromUserId;
    this.favoritedCount = favoritedCount;
    this.retweetedCount = retweetedCount;
  }

  public long getId() {
    return id;
  }

  public String getContent() {
    return content;
  }

  public User getAuthorUser() {
    return authorUser;
  }

  public long getAuthorUserId() {
    return authorUserId;
  }

  public long getRepliedToUserId() {
    return repliedToUserId;
  }

  public long getRetweetedFromUserId() {
    return retweetedFromUserId;
  }

  public int getFavoritedCount() {
    return favoritedCount;
  }

  public int getRetweetedCount() {
    return retweetedCount;
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

    if (!(obj instanceof Tweet)) {
      return false;
    }

    final Tweet other = (Tweet) obj;

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
