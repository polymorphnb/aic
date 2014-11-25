package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.file.dumper.TweetSearchTerm;
import ac.at.tuwien.tdm.file.dumper.writer.TweetFileWriter;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TweetSearchTask extends TwitterTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(TweetSearchTask.class);

  private final TweetSearchTerm searchTerm;

  public TweetSearchTask(final TwitterConnector twitterConnector, final TweetSearchTerm searchTerm) {
    super(twitterConnector);
    this.searchTerm = searchTerm;
  }

  @Override
  public void execute(final List<Tweet> tweets, final Set<User> users) throws TwitterConnectorException,
      InterruptedException, ExecutionException {

    final Future<List<Tweet>> pendingResult = twitterConnector.findByKeyWord(searchTerm.getSearchTerm(),
        searchTerm.isSearchOnlyInHashTags());

    tweets.addAll(pendingResult.get());
    writeAllTweetsToFile(tweets);

    LOGGER.info(String.format("Found %d tweets for search term %s", tweets.size(), searchTerm));
  }

  private void writeAllTweetsToFile(final List<Tweet> tweets) {
    if (!tweets.isEmpty()) {
      final TweetFileWriter tweetWriter = TweetFileWriter.getInstance();
      try {
        tweetWriter.appendToFile(searchTerm.getSearchTerm(), tweets);
      } catch (final Exception e) {
        // fail fast
        throw new RuntimeException(e);
      }

    }
  }
}
