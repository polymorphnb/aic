package ac.at.tuwien.tdm.file.dumper;

import ac.at.tuwien.tdm.commons.Constants;

/**
 * File dumper constants
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class FileDumperConstants extends Constants {

  // hide constructor
  private FileDumperConstants() {
    super();
  }

  private static final String DESTINATION_FOLDER = "./dist";

  private static final String TWEET_FOR_USER_FILE_NAME_PREFIX = "tweetsForUser_";

  private static final String TWEET_FILE_NAME_PREFIX = "tweet_";

  public static final String USER_FILE_NAME_PREFIX = "user_";

  public static final String TWEETS_FOR_USER_FILE_NAME = (DESTINATION_FOLDER + "/tweets/" + TWEET_FOR_USER_FILE_NAME_PREFIX);

  public static final String TWEETS_FILE_NAME = (DESTINATION_FOLDER + "/tweets/" + TWEET_FILE_NAME_PREFIX);

  public static final String USER_FILE_NAME = (DESTINATION_FOLDER + "/users/" + USER_FILE_NAME_PREFIX);

  public static final String TEXT_FILE_EXTENSION = ".txt";

  public static final String SEARCH_TERMS_FILE_NAME = "searchTerms.txt";

  public static final String CONFIG_PROPERTIES_FILE_NAME = "config.properties";

  public static final String WHITELISTED_FILES_FILE_NAME = "userFileWhiteList.txt";

  public static final int AMOUNT_OF_WORKER_THREADS = 1;

  public static final String USER_FILE_FOLDER_KEY = "user.files.folder";

  public static final int ASSUMPTION_AMOUNT_CREDENTIALS = 29;

  public static final int ASSUMPTION_TIME_WINDOW_IN_MINUTES = 17;
}
