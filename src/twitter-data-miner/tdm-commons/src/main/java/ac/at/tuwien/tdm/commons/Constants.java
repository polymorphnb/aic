package ac.at.tuwien.tdm.commons;

import java.nio.charset.Charset;

public class Constants {

  protected Constants() {
    // hide constructor
  }

  public static final long SECOND_IN_MILLISECONDS = 1000;

  public static final long MINUTE_IN_MILLISECONDS = (60 * SECOND_IN_MILLISECONDS);

  public static final long HOUR_IN_MILLISECONDS = (60 * MINUTE_IN_MILLISECONDS);

  public static final long DAY_IN_MILLISECONDS = (24 * HOUR_IN_MILLISECONDS);

  public static final Charset ENCODING = Charset.forName("UTF-8");

  public static final String LINE_ENDING = "\n";
  
  public static final String CONFIG_FILE_NAME = "config.properties";
}
