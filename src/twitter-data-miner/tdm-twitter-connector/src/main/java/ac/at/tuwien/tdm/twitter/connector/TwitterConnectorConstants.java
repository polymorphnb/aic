package ac.at.tuwien.tdm.twitter.connector;

import ac.at.tuwien.tdm.commons.Constants;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Twitter connector constants
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TwitterConnectorConstants extends Constants {

  //hide constructor
  private TwitterConnectorConstants() {
    super();
  }

  public static final String CREDENTIALS_FILE_NAME = "twitterAuthentication.txt";

  public static final int TIME_WINDOW_IN_SECONDS = (15 * 60);

  public static final String TWEET_LANGUAGE = "en";

  public static final int DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH = 20000;

  public static final int DEFAULT_MAX_TWEETS_PER_USER_SEARCH = 1000; // 3200

  public static final int ID_LIST_APPROACH_THRESHOLD = 160;

  public static final int HTTP_UNAUTHORIZED = 401;

  private static final int HTTP_FORBIDDEN = 403;

  private static final int HTTP_INTERNAL_SERVER_ERROR = 500;

  private static final int HTTP_SERVICE_UNAVAILABLE = 503;

  public static final Set<Integer> HTTP_RETRY_PROBLEMS = new HashSet<Integer>(Arrays.asList(HTTP_UNAUTHORIZED,
      HTTP_FORBIDDEN, HTTP_INTERNAL_SERVER_ERROR, HTTP_SERVICE_UNAVAILABLE));
}
