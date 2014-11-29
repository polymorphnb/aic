package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.file.dumper.TweetSearchTerm;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Pipeline implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(Pipeline.class);

  private final Queue<Task> tasks;

  private final CountDownLatch latch;

  private Pipeline(final TwitterConnector twitterConnector, final CountDownLatch latch, final TweetSearchTerm topic) {
    this.latch = latch;

    tasks = new LinkedList<Task>();
    tasks.add(new TweetSearchTask(twitterConnector, topic));
    tasks.add(new UserLookUpTask(twitterConnector));
    tasks.add(new RelationshipBuilderTask(twitterConnector));
    //    tasks.add(new WriteToFilesTask(topic.getSearchTerm()));
  }

  public static Pipeline newInstance(final TwitterConnector twitterConnector, final CountDownLatch latch,
      final TweetSearchTerm topic) {
    return new Pipeline(twitterConnector, latch, topic);
  }

  @Override
  public void run() {
    final List<Tweet> tweets = new ArrayList<Tweet>(16384);
    final Set<User> users = new HashSet<User>(16384);

    try {
      for (final Task task : tasks) {
        task.execute(tweets, users);
      }
    } catch (final Exception e) {
      LOGGER.error("Task failed", e);
    } finally {
      latch.countDown();
    }
  }
}
