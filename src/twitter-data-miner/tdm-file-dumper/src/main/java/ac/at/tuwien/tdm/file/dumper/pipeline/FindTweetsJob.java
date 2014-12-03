package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.commons.GsonInstance;
import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;
import ac.at.tuwien.tdm.file.dumper.writer.TweetsForUserFileWriter;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Finds up to 3200 tweets for every user (or only author users) in a user file
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class FindTweetsJob implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindTweetsJob.class);

  private final TwitterConnector connector;

  private final CountDownLatch latch;

  private final File userFile;

  private final boolean includeFriends;

  private final boolean includeFollowers;

  public FindTweetsJob(final TwitterConnector connector, final CountDownLatch latch, final File userFile,
      final boolean includeFriends, final boolean includeFollowers) {
    this.connector = connector;
    this.latch = latch;
    this.userFile = userFile;
    this.includeFriends = includeFriends;
    this.includeFollowers = includeFollowers;
  }

  @Override
  public void run() {

    final Set<Long> idsToProcess = new HashSet<Long>();
    InputStream in = null;

    try {
      in = FileUtils.openInputStream(userFile);
      final LineIterator it = IOUtils.lineIterator(in, FileDumperConstants.ENCODING);
      LOGGER.info(String.format("Processing file \"%s\", include friends: %b, include followers: %b",
          userFile.getName(), includeFriends, includeFollowers));

      final Gson gson = GsonInstance.get();

      while (it.hasNext()) {
        final User user = gson.fromJson(it.next(), User.class);

        idsToProcess.add(user.getId());

        if (includeFriends) {
          for (final Long friendId : user.getFriendsUserIds()) {
            idsToProcess.add(friendId);
          }
        }

        if (includeFollowers) {
          for (final Long followerId : user.getFollowerUserIds()) {
            idsToProcess.add(followerId);
          }
        }
      }

      LOGGER.info(String.format("Found %d ids to process", idsToProcess.size()));

      for (final Long id : idsToProcess) {
        try {
          final Future<List<Tweet>> pendingResult = connector.findTweetsForUserId(id);
          final List<Tweet> tweetsForUser = pendingResult.get();

          LOGGER.info(String.format("Writing %d tweets for user with id %d", tweetsForUser.size(), id));
          TweetsForUserFileWriter.getInstance().writeToFile(id.toString(), tweetsForUser);
        } catch (final Exception e) {
          // simply skip it for now, logging should happen in the connector
        }
      }
    } catch (final Exception e) {
      LOGGER.error("Task failed", e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (final IOException e) {
          LOGGER.error(String.format("Couldn't close file input stream '%s'", userFile.getPath()), e);
        }
      }

      latch.countDown();
    }
  }
}
