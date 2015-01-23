package ac.at.tuwien.tdm.processor.reader;

import java.nio.charset.Charset;

public final class ConfigConstants {

  private static final String DESTINATION_FOLDER = "./dist";
  
  public static final String TWEETS_FOLDER = DESTINATION_FOLDER + "/tweets/";
  
  public static final String TWEETS_FOLDER_PROCESSED = DESTINATION_FOLDER + "/tweets/processed/";

  public static final String USER_FOLDER = DESTINATION_FOLDER + "/users/";
  
  public static final String USER_FOLDER_PROCESSED = DESTINATION_FOLDER + "/users/processed/";

  public static final String TEXT_FILE_EXTENSION = "txt";
  
  public static final Charset ENCODING = Charset.forName("UTF-8");
}
