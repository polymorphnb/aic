package ac.at.tuwien.tdm.twitter.connector;

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

  public static final int MINUTE_IN_SECONDS = 60;

  public static final int TIME_WINDOW_IN_SECONDS = (15 * MINUTE_IN_SECONDS);
  
  public static final String TWEET_LANGUAGE = "en";
  
  public static final int DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH = 1000;

  public static final int DEFAULT_AMOUNT_OF_TWEETS_PER_RESULT_PAGE = 100;

}
