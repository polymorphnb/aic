package ac.at.tuwien.tdm.file.dumper.writer;

import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;

public class TweetsForUserFileWriter extends TwitterFileWriter<Tweet> {

  private static final TweetsForUserFileWriter INSTANCE = new TweetsForUserFileWriter();

  private TweetsForUserFileWriter() {
    super(FileDumperConstants.TWEETS_FOR_USER_FILE_NAME, FileDumperConstants.TEXT_FILE_EXTENSION, false);
  }

  public static TweetsForUserFileWriter getInstance() {
    return INSTANCE;
  }
}
