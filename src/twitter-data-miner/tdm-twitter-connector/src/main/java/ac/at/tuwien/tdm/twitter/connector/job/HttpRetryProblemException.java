package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.Clock;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;

import java.util.Calendar;

public class HttpRetryProblemException extends Exception {

  private final long resetTimestamp;

  public HttpRetryProblemException(final Throwable t) {
    super(t);
    resetTimestamp = buildResetTimestamp();
  }
  
  public long getResetTimestamp(){
    return resetTimestamp;
  }

  private long buildResetTimestamp() {
    final Calendar time = Clock.currentTime();

    time.add(Calendar.SECOND, TwitterConnectorConstants.TIME_WINDOW_IN_SECONDS);
    time.add(Calendar.MINUTE, 1);

    return time.getTimeInMillis();
  }
}
