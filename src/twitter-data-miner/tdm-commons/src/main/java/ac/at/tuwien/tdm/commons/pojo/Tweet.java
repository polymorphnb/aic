package ac.at.tuwien.tdm.commons.pojo;

import ac.at.tuwien.tdm.commons.GsonInstance;

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
  private long id; // 8 bytes

  private String content; // 255 * 2 bytes

  // not to persist, only used by other processing logic
  private transient User authorUser; // zero bytes

  private long authorUserId; // 8 bytes

  private long repliedToUserId; // 8 bytes

  private long repliedToTweetId; // 8 bytes

  private long retweetedFromUserId; // 8 bytes

  private long retweetedFromTweetId; // 8 bytes

  private int favoritedCount; // 4 bytes

  private int retweetedCount; // 4 bytes

  private String searchTerm; // approx. 12 * 2 bytes

  public Tweet() {
    id = -1l;
    authorUserId = -1l;
    repliedToUserId = -1l;
    repliedToTweetId = -1l;
    retweetedFromUserId = -1l;
    retweetedFromTweetId = -1l;
    favoritedCount = 0;
    retweetedCount = 0;
  }

  public Tweet(final long id, final String content, final User authorUser, final long repliedToUserId,
      final long repliedToTweetId, final long retweetedFromUserId, final long retweetedFromTweetId,
      final int favoritedCount, final int retweetedCount, final String searchTerm) {
    this();
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

  public void setId(long id) {
    this.id = id;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public User getAuthorUser() {
    return authorUser;
  }

  public long getAuthorUserId() {
    return authorUserId;
  }

  public void setAuthorUserId(long authorUserId) {
    this.authorUserId = authorUserId;
  }

  public long getRepliedToUserId() {
    return repliedToUserId;
  }

  public void setRepliedToUserId(long repliedToUserId) {
    this.repliedToUserId = repliedToUserId;
  }

  public long getRepliedToTweetId() {
    return repliedToTweetId;
  }

  public void setRepliedToTweetId(long repliedToTweetId) {
    this.repliedToTweetId = repliedToTweetId;
  }

  public long getRetweetedFromUserId() {
    return retweetedFromUserId;
  }

  public void setRetweetedFromUserId(long retweetedFromUserId) {
    this.retweetedFromUserId = retweetedFromUserId;
  }

  public long getRetweetedFromTweetId() {
    return retweetedFromTweetId;
  }

  public void setRetweetedFromTweetId(long retweetedFromTweetId) {
    this.retweetedFromTweetId = retweetedFromTweetId;
  }

  public int getFavoritedCount() {
    return favoritedCount;
  }

  public void setFavoritedCount(int favoritedCount) {
    this.favoritedCount = favoritedCount;
  }

  public int getRetweetedCount() {
    return retweetedCount;
  }

  public void setRetweetedCount(int retweetedCount) {
    this.retweetedCount = retweetedCount;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public void setSearchTerm(String searchTerm) {
    this.searchTerm = searchTerm;
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
