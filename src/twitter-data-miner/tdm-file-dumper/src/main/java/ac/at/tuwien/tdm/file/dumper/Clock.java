package ac.at.tuwien.tdm.file.dumper;

import java.util.Calendar;
import java.util.TimeZone;

public final class Clock {

  private Clock() {
    // hide constructor
  }

  public static Calendar currentTime() {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  }

  public static Calendar givenTime(final long timestamp) {
    final Calendar time = currentTime();
    time.setTimeInMillis(timestamp);
    return time;
  }
}
