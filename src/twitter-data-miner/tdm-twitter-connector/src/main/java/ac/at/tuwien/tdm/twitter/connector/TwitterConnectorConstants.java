package ac.at.tuwien.tdm.twitter.connector;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

  public static final int DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH = 10000;

  // must be <= 100
  public static final int DEFAULT_AMOUNT_OF_TWEETS_PER_RESULT_PAGE = 100;

  public static final int ID_LIST_APPROACH_THRESHOLD = 160;

  private static final int HTTP_UNAUTHORIZED = 401;

  private static final int HTTP_FORBIDDEN = 403;

  private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

  private static final int HTTP_SERVICE_UNAVAILABLE = 503;

  public static final Set<Integer> HTTP_RETRY_PROBLEMS = new HashSet<Integer>(Arrays.asList(HTTP_UNAUTHORIZED,
      HTTP_FORBIDDEN, HTTP_INTERNAL_SERVER_ERROR, HTTP_SERVICE_UNAVAILABLE));
}
