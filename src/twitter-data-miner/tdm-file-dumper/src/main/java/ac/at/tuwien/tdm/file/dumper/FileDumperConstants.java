package ac.at.tuwien.tdm.file.dumper;

import java.nio.charset.Charset;

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

  public static final String SEARCH_TERMS_FILE_NAME = "searchTerms.txt";
  
  public static final int AMOUNT_OF_WORKER_THREADS = 3;
  
  public static final String LINE_ENDING = "\n";
  
  public static final Charset ENCODING = Charset.forName("UTF-8");
  
  public static final long MAX_ENTRIES_PER_FILE = 1000;
}
