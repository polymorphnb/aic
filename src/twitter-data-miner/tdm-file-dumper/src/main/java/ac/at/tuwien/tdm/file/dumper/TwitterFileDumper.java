package ac.at.tuwien.tdm.file.dumper;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorFactory;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Future;

public final class TwitterFileDumper {

  private final Queue<Future<List<Tweet>>> pendingTweetSearchResults = new LinkedList<>();

  private final Queue<Future<List<User>>> pendingUserLookUpResults = new LinkedList<>();

  private final LinkedList<Long> userIdsToLookUp = new LinkedList<>();

  private final Set<Long> alreadyLookedUpUserIds = new HashSet<>();

  private TwitterFileDumper() {
  }

  public static void main(final String[] args) throws IOException, TwitterConnectorException {

    final TwitterConnector connector = TwitterConnectorFactory.createTwitterConnector();
    TwitterFileDumper fileDumper = null;

    try {
      fileDumper = new TwitterFileDumper();
      fileDumper.perform(connector);
    } finally {
      if (fileDumper != null) {
        fileDumper.cleanUpResources();
      }
      connector.shutdownService();
    }
  }

  private void perform(final TwitterConnector connector) throws IOException, TwitterConnectorException {

    final List<TweetSearchTopic> topics = readTopicsFromDefaultFile();

    for (final TweetSearchTopic topic : topics) {
      pendingTweetSearchResults.add(connector.findByKeyWord(topic.getSearchTerm(), topic.isSearchOnlyInHashTags()));
    }

    while (pendingTweetSearchResults.peek() != null) {

      final Future<List<Tweet>> pendingSearchResult = pendingTweetSearchResults.poll();
      List<Tweet> searchResult = null;

      try {
        searchResult = pendingSearchResult.get();
      } catch (final Exception e) {
        // dismiss search
      }

      if (searchResult != null) {
        retrieveUserIdsForLookUp(searchResult);
        processTweetResults(searchResult);
      }

      while (userIdsToLookUp.size() >= 100) {
        final List<Long> firstUserIds = pollFirstOneHundredEntries(userIdsToLookUp);
        pendingUserLookUpResults.add(connector.lookUpUsersById(firstUserIds));
      }

      while (pendingUserLookUpResults.peek() != null) {
        final Future<List<User>> pendingLookUpResult = pendingUserLookUpResults.poll();

        List<User> lookUpResult = null;

        try {
          lookUpResult = pendingLookUpResult.get();
        } catch (final Exception e) {
          // dismiss search
        }

        if (lookUpResult != null) {
          processUserResults(lookUpResult);
        }
      }
    }

    if (userIdsToLookUp.size() > 0) {
      pendingUserLookUpResults.add(connector.lookUpUsersById((List<Long>) userIdsToLookUp));
    }

    while (pendingUserLookUpResults.peek() != null) {
      final Future<List<User>> pendingLookUpResult = pendingUserLookUpResults.poll();

      List<User> lookUpResult = null;

      try {
        lookUpResult = pendingLookUpResult.get();
      } catch (final Exception e) {
        // dismiss search
      }

      if (lookUpResult != null) {
        processUserResults(lookUpResult);
      }
    }
  }

  private List<Long> pollFirstOneHundredEntries(final Queue<Long> userIdQueue) {
    final List<Long> userIds = new ArrayList<>(128);

    for (int i = 0; i < 100; i++) {
      final Long userId = userIdQueue.poll();

      if (userId == null) {
        break;
      }

      if (!alreadyLookedUpUserIds.contains(userId)) {
        userIds.add(userId);
        alreadyLookedUpUserIds.add(userId);
      }
    }

    return userIds;
  }

  private void retrieveUserIdsForLookUp(final List<Tweet> tweets) {
    for (final Tweet tweet : tweets) {

      if (tweet.getRepliedToUserId() != -1) {
        userIdsToLookUp.add(tweet.getRepliedToUserId());
      }

      if (tweet.getRetweetedFromUserId() != -1) {
        userIdsToLookUp.add(tweet.getRetweetedFromUserId());
      }
    }
  }

  private void processTweetResults(final List<Tweet> tweets) throws IOException {
    TweetFileWriter.getInstance().appendToFile(tweets);

    final List<User> authorUsers = retrieveAuthorUsersFromTweets(tweets);
    UserFileWriter.getInstance().appendToFile(authorUsers);
  }

  private void processUserResults(final List<User> users) throws IOException {
    UserFileWriter.getInstance().appendToFile(users);
  }

  private List<User> retrieveAuthorUsersFromTweets(final List<Tweet> tweets) {
    final List<User> authorUsers = new ArrayList<>(Integer.highestOneBit(tweets.size()) * 2);

    for (final Tweet tweet : tweets) {
      authorUsers.add(tweet.getAuthorUser());
      alreadyLookedUpUserIds.add(tweet.getAuthorUserId());
    }

    return authorUsers;
  }

  private List<TweetSearchTopic> readTopicsFromDefaultFile() {
    final List<TweetSearchTopic> topics = new ArrayList<>();
    final InputStream resource = ClassLoader.getSystemResourceAsStream(Constants.TOPICS_FILE_NAME);

    BufferedReader br = new BufferedReader(new InputStreamReader(resource));
    String readLine;

    try {
      while ((readLine = br.readLine()) != null) {
        final String[] values = readLine.split(";");

        if (values.length != 2) {
          throw new IllegalArgumentException("Topics file is corrupt. Read line does not contain 2 values.");
        }

        final String searchTerm = values[0].trim();
        final boolean onlyHashTags = Boolean.parseBoolean(values[1].trim());

        topics.add(new TweetSearchTopic(searchTerm, onlyHashTags));
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final Exception e) {
      throw e;
    } finally {
      try {
        br.close();
      } catch (IOException e) {
        // log and forget
      }
    }

    return topics;
  }

  private void cleanUpResources() {
    TweetFileWriter.getInstance().closeFileStream();
  }
}
