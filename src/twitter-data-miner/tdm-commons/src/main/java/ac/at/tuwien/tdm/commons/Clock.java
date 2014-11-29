package ac.at.tuwien.tdm.commons;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.TimeZone;

public final class Clock {

  private static final DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.LONG);

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

  public static String dateForGivenTimeInLocalTimeZone(final long timestamp) {
    final Calendar calendar = Calendar.getInstance();
    calendar.setTimeInMillis(timestamp);
    return formatter.format(calendar.getTime());
  }
}
