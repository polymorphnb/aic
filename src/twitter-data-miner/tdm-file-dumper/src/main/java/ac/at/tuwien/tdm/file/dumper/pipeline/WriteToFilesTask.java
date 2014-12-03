package ac.at.tuwien.tdm.file.dumper.pipeline;

import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.file.dumper.writer.SearchTweetFileWriter;
import ac.at.tuwien.tdm.file.dumper.writer.UserFileWriter;

import java.util.List;
import java.util.Set;

public final class WriteToFilesTask implements Task {

  private final String searchTerm;

  public WriteToFilesTask(final String searchTerm) {
    this.searchTerm = searchTerm;
  }

  @Override
  public void execute(final List<Tweet> tweets, final Set<User> users) throws Exception {

    if (!tweets.isEmpty()) {
      final SearchTweetFileWriter tweetWriter = SearchTweetFileWriter.getInstance();
      tweetWriter.writeToFile(searchTerm, tweets);
    }

    if (!users.isEmpty()) {
      final UserFileWriter userWriter = UserFileWriter.getInstance();
      userWriter.writeToFile(searchTerm, users);
    }
  }
}
