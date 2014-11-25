package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RelationshipBuilderTask extends TwitterTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(RelationshipBuilderTask.class);

  public RelationshipBuilderTask(final TwitterConnector twitterConnector) {
    super(twitterConnector);
  }

  @Override
  public void execute(final List<Tweet> tweets, final List<User> users) throws Exception {
    for (final User user : users) {
      lookUpFollowers(user);
      lookUpFriends(user);
    }
  }

  private void lookUpFollowers(final User user) throws TwitterConnectorException, InterruptedException,
      ExecutionException {
    final Future<List<Long>> pendingResult = twitterConnector.findFollowerIdsForUserId(user.getId(),
        user.getFollowersCount());
    final List<Long> followerUserIds = pendingResult.get();

    LOGGER
        .info(String.format("Found %d follower user ids for user with id '%d'", followerUserIds.size(), user.getId()));

    user.addFollowerUserIds(followerUserIds);

  }

  private void lookUpFriends(final User user) throws TwitterConnectorException, InterruptedException,
      ExecutionException {
    final Future<List<Long>> pendingResult = twitterConnector.findFriendIdsForUserId(user.getId(),
        user.getFriendsCount());
    final List<Long> friendsUserIds = pendingResult.get();

    LOGGER.info(String.format("Found %d friends user ids for user with id '%d'", friendsUserIds.size(), user.getId()));

    user.addFriendsUserIds(friendsUserIds);
  }
}