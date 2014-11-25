package ac.at.tuwien.tdm.twitter.connector;

import java.nio.charset.Charset;

/**
 * Twitter connector constants
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TwitterConnectorConstants {

  private TwitterConnectorConstants() {
    //hide constructor
  }

  public static final String CREDENTIALS_FILE_NAME = "twitterAuthentication.txt";

  public static final Charset ENCODING = Charset.forName("UTF-8");

  private static final int MINUTE_IN_SECONDS = 60;

  public static final int TIME_WINDOW_IN_SECONDS = (15 * MINUTE_IN_SECONDS);

  public static final String TWEET_LANGUAGE = "en";

  public static final int DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH = 1000;

  // must be <= 100
  public static final int DEFAULT_AMOUNT_OF_TWEETS_PER_RESULT_PAGE = 100;
  
  public static final int ID_LIST_APPROACH_THRESHOLD = 140;
}
