package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;

public final class UserRelationBuilderTask extends TwitterTask {

  public UserRelationBuilderTask(final TwitterConnector twitterConnector) {
    super(twitterConnector);
  }

  @Override
  public void execute(final List<Tweet> tweets, final List<User> users) throws Exception {
    // TODO Auto-generated method stub

  }
}
