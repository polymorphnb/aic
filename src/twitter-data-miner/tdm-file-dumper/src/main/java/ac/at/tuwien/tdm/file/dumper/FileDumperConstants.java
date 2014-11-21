package ac.at.tuwien.tdm.file.dumper;

/**
 * File dumper constants
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class FileDumperConstants {

  private static final String DESTINATION_FOLDER = "./dist";

  public static final String TWEETS_FILE_NAME = DESTINATION_FOLDER + "/tweets/tweet_";

  public static final String USER_FILE_NAME = DESTINATION_FOLDER + "/users/user_";

  public static final String TEXT_FILE_EXTENSION = ".txt";

  public static final String TOPICS_FILE_NAME = "topics.txt";
  
  public static final int AMOUNT_OF_WORKER_THREADS = 10;
}
