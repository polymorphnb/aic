package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;

public interface Task {

  void execute(final List<Tweet> tweets, final List<User> users) throws Exception;

}
