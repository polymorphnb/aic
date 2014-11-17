package ac.at.tuwien.tdm.twitter.connector.api;

public final class Tweet {

  private final long id;

  private final String content;

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
  public String toString() {
    return "Tweet [id=" + id + ", content=" + content + ", authorUserId=" + authorUserId + ", repliedToUserId="
        + repliedToUserId + ", retweetedFromUserId=" + retweetedFromUserId + ", favoritedCount=" + favoritedCount
        + ", retweetedCount=" + retweetedCount + "]";
  }
}
