package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.file.dumper.TweetSearchTerm;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class TweetSearchTask extends TwitterTask {

  private final TweetSearchTerm topic;

  public TweetSearchTask(final TwitterConnector twitterConnector, final TweetSearchTerm topic) {
    super(twitterConnector);
    this.topic = topic;
  }

  @Override
  public void execute(final List<Tweet> tweets, final List<User> users) throws TwitterConnectorException,
      InterruptedException, ExecutionException {

    final Future<List<Tweet>> pendingResult = twitterConnector.findByKeyWord(topic.getSearchTerm(),
        topic.isSearchOnlyInHashTags());

    tweets.addAll(pendingResult.get());
  }
}
