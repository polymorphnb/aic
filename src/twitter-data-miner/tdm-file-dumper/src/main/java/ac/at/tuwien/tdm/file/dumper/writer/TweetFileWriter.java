package ac.at.tuwien.tdm.file.dumper.writer;

import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;

/**
 * A simple file writer for tweets (one tweet per line as json)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TweetFileWriter extends TwitterFileWriter<Tweet> {

  private static final TweetFileWriter INSTANCE = new TweetFileWriter();

  private TweetFileWriter() {
    super(FileDumperConstants.TWEETS_FILE_NAME, FileDumperConstants.TEXT_FILE_EXTENSION);
  }

  public static TweetFileWriter getInstance() {
    return INSTANCE;
  }
}
