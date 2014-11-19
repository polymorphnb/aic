package ac.at.tuwien.tdm.twitter.connector;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.User;
import twitter4j.Status;

/**
 * Factory for converting twitter4j objects to DTOs for transfer.
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class DtoFactory {

  private DtoFactory() {
    // hide constructor
  }

  public static Maybe<Tweet> createTweetFromStatus(final Status status, final boolean includeRetweetedStatus) {
    Defense.notNull("status", status);

    final boolean isRetweetAndIncludeOriginalTweet = (status.getRetweetedStatus() != null && includeRetweetedStatus);
    final Long retweetedFromUserId = (isRetweetAndIncludeOriginalTweet ? status.getRetweetedStatus().getUser().getId()
        : -1l);

    final boolean isTweetUsable = (status.getId() != -1l && Utils.isNotBlank(status.getText()) && status
        .getUser() != null);

    // should not happen, but make it more robust anyway
    if (!isTweetUsable) {
      return Maybe.unknown(Tweet.class);
    }

    final Tweet tweet = new Tweet(status.getId(), status.getText(),
        createUserFromTwitterUser(status.getUser()).value(), status.getInReplyToUserId(), retweetedFromUserId,
        status.getFavoriteCount(), status.getRetweetCount());

    return Maybe.definitely(tweet);
  }

  public static Maybe<User> createUserFromTwitterUser(final twitter4j.User twitterUser) {
    Defense.notNull("twitterUser", twitterUser);

    final User user = new User(twitterUser.getId(), twitterUser.getScreenName(), twitterUser.getName(),
        twitterUser.getLocation(), twitterUser.getLang(), twitterUser.getStatusesCount(),
        twitterUser.getFollowersCount());

    return Maybe.definitely(user);
  }
}
