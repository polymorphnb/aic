package ac.at.tuwien.tdm.twitter.connector;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.User;
import twitter4j.Status;

public final class DtoFactory {

  private DtoFactory() {
  }

  public static Tweet createTweetFromStatus(final Status status, final boolean includeRetweetedStatus) {
    final Long retweetedFromUserId = ((includeRetweetedStatus && status.getRetweetedStatus() != null) ? status
        .getRetweetedStatus().getUser().getId() : -1l);

    return new Tweet(status.getId(), status.getText(), createUserFromTwitterUser(status.getUser()),
        status.getInReplyToUserId(), retweetedFromUserId, status.getFavoriteCount(), status.getRetweetCount());
  }

  public static User createUserFromTwitterUser(final twitter4j.User twitterUser) {
    return new User(twitterUser.getId(), twitterUser.getScreenName(), twitterUser.getName(), twitterUser.getLocation(),
        twitterUser.getLang(), twitterUser.getStatusesCount(), twitterUser.getFollowersCount());
  }
}
