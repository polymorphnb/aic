package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

public final class UserLookUpTask extends TwitterTask {

  private static final long INVALID_VALUE = -1l;

  public UserLookUpTask(final TwitterConnector twitterConnector) {
    super(twitterConnector);
  }

  @Override
  public void execute(final List<Tweet> tweets, final List<User> users) throws Exception {

    transferAuthorUsers(tweets, users);

    final List<Long> userIds = retrieveUserIds(tweets);
    final List<List<Long>> userIdLists = splitIntoListsOfOneHundredEntriesEach(userIds);

    for (final List<Long> maxOneHundredUserIds : userIdLists) {
      final Future<List<User>> pendingResult = twitterConnector.lookUpUsersById(maxOneHundredUserIds);
      users.addAll(pendingResult.get());
    }
  }

  private void transferAuthorUsers(final List<Tweet> tweets, final List<User> users) {
    for (final Tweet tweet : tweets) {
      users.add(tweet.getAuthorUser());
    }
  }

  private List<Long> retrieveUserIds(final List<Tweet> tweets) {
    final List<Long> userIds = new ArrayList<>(tweets.size() * 2);

    for (final Tweet tweet : tweets) {
      if (tweet.getRepliedToUserId() != INVALID_VALUE) {
        userIds.add(tweet.getRepliedToUserId());
      }

      if (tweet.getRetweetedFromUserId() != INVALID_VALUE) {
        userIds.add(tweet.getRetweetedFromUserId());
      }
    }

    return userIds;
  }

  private List<List<Long>> splitIntoListsOfOneHundredEntriesEach(final List<Long> userIds) {
    final List<List<Long>> userIdLists = new ArrayList<>(Integer.highestOneBit((userIds.size() / 100) + 1) * 2);

    List<Long> idList = new ArrayList<>(128);

    for (final Long userId : userIds) {
      if (idList.size() == 100) {
        userIdLists.add(idList);
        idList = new ArrayList<>(128);
      }

      idList.add(userId);
    }

    userIdLists.add(idList);
    return userIdLists;
  }
}
