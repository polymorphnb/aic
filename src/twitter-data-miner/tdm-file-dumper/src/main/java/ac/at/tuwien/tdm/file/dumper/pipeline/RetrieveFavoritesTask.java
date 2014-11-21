package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;

public final class RetrieveFavoritesTask extends TwitterTask {

  public RetrieveFavoritesTask(final TwitterConnector twitterConnector) {
    super(twitterConnector);
  }

  @Override
  public void execute(List<Tweet> tweets, List<User> users) throws Exception {
    // TODO Auto-generated method stub

  }
}
