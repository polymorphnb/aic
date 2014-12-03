package ac.at.tuwien.tdm.file.dumper.writer;

import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;

/**
 * A simple file writer for tweets (one tweet per line as json)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class SearchTweetFileWriter extends TwitterFileWriter<Tweet> {

  private static final SearchTweetFileWriter INSTANCE = new SearchTweetFileWriter();

  private SearchTweetFileWriter() {
    super(FileDumperConstants.TWEETS_FILE_NAME, FileDumperConstants.TEXT_FILE_EXTENSION, true);
  }

  public static SearchTweetFileWriter getInstance() {
    return INSTANCE;
  }
}
