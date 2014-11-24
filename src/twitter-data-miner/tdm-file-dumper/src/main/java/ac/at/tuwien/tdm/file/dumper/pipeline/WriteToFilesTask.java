package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.file.dumper.writer.TweetFileWriter;
import ac.at.tuwien.tdm.file.dumper.writer.UserFileWriter;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.util.List;

public final class WriteToFilesTask implements Task {

  @Override
  public void execute(final List<Tweet> tweets, final List<User> users) throws Exception {

    if (!tweets.isEmpty()) {
      final TweetFileWriter tweetWriter = TweetFileWriter.getInstance();
      tweetWriter.appendToFile(tweets);
    }

    if (!users.isEmpty()) {
      final UserFileWriter userWriter = UserFileWriter.getInstance();
      userWriter.appendToFile(users);
    }
  }
}
