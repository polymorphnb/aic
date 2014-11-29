package ac.at.tuwien.tdm.commons;

import java.nio.charset.Charset;

public class Constants {

  protected Constants() {
    // hide constructor
  }

  protected static final int MINUTE_IN_SECONDS = 60;

  public static final Charset ENCODING = Charset.forName("UTF-8");

  public static final String LINE_ENDING = "\n";
}
