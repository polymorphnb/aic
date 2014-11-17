package ac.at.tuwien.tdm.file.dumper;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;

final class TweetFileWriter extends TwitterFileWriter<Tweet> {

  private static final TweetFileWriter INSTANCE = new TweetFileWriter();

  private TweetFileWriter() {
    super(Constants.TWEETS_FILE_NAME, Constants.TEXT_FILE_EXTENSION);
  }

  public static TweetFileWriter getInstance() {
    return INSTANCE;
  }
}
