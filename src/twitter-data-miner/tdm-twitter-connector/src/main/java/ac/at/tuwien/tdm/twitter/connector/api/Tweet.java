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
// approx. 588 bytes 
public final class Tweet {

  // unique and used for equals & hashCode
  private final long id; // 8 bytes

  private final String content; // 255 * 2 bytes

  // not to persist, only used by other processing logic
  private transient final User authorUser; // zero bytes

  private final long authorUserId; // 8 bytes

  private final long repliedToUserId; // 8 bytes

  private final long repliedToTweetId; // 8 bytes

  private final long retweetedFromUserId; // 8 bytes

  private final long retweetedFromTweetId; // 8 bytes

  private final int favoritedCount; // 4 bytes

  private final int retweetedCount; // 4 bytes

  private final String searchTerm; // approx. 12 * 2 bytes

  public Tweet(final long id, final String content, final User authorUser, final long repliedToUserId,
      final long repliedToTweetId, final long retweetedFromUserId, final long retweetedFromTweetId,
      final int favoritedCount, final int retweetedCount, final String searchTerm) {
    this.id = id;
    this.content = content;
    this.authorUser = authorUser;
    this.authorUserId = authorUser.getId();
    this.repliedToUserId = repliedToUserId;
    this.repliedToTweetId = repliedToTweetId;
    this.retweetedFromUserId = retweetedFromUserId;
    this.retweetedFromTweetId = retweetedFromTweetId;
    this.favoritedCount = favoritedCount;
    this.retweetedCount = retweetedCount;
    this.searchTerm = searchTerm;
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

  public long getRepliedToTweetId() {
    return repliedToTweetId;
  }

  public long getRetweetedFromUserId() {
    return retweetedFromUserId;
  }

  public long getRetweetedFromTweetId() {
    return retweetedFromTweetId;
  }

  public int getFavoritedCount() {
    return favoritedCount;
  }

  public int getRetweetedCount() {
    return retweetedCount;
  }

  public String getSearchTerm() {
    return searchTerm;
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
