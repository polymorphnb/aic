package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;

public abstract class TwitterTask implements Task {

  protected final TwitterConnector twitterConnector;

  protected TwitterTask(final TwitterConnector twitterConnector) {
    this.twitterConnector = twitterConnector;
  }
}
