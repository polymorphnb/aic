package ac.at.tuwien.tdm.twitter.connector;

import ac.at.tuwien.tdm.twitter.connector.job.LimitReachedException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.OAuth2Token;
import twitter4j.conf.ConfigurationBuilder;

public final class TwitterAuthenticationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterAuthenticationService.class);

  private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private static final Lock lock = new ReentrantLock();

  private static TwitterAuthenticationService INSTANCE;

  private List<TwitterOAuthCredentials> credentialsList;

  private ListIterator<TwitterOAuthCredentials> credentialsIterator;

  private TwitterOAuthCredentials credentialsInUse;

  private Twitter twitter;

  private TwitterAuthenticationService() {
    // hide constructor
  }

  public static TwitterAuthenticationService getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new TwitterAuthenticationService();
      INSTANCE.loadCredentialsFromFile();
    }

    return INSTANCE;
  }

  public void prepareFirstUse() throws LimitReachedException, TwitterException {
    if (credentialsInUse != null) {
      throw new UnsupportedOperationException("Already called this method");
    }

    final TwitterOAuthCredentials nextCredentials = getNextCredentials();
    useCredentials(nextCredentials);
  }

  public void shutdown() {
    scheduler.shutdownNow();
  }

  public void invalidateCredentials(final long resetTimestamp) throws TwitterException {
    credentialsInUse.invalidate(resetTimestamp);
    LOGGER.info(String.format("Invalidated credentials #%d until %s", credentialsInUse.getCredentialNumber(),
        Clock.dateForGivenTimeInLocalTimeZone(resetTimestamp)));

    TwitterOAuthCredentials nextCredentials = null;

    do {
      try {
        nextCredentials = getNextCredentials();
      } catch (final LimitReachedException e) {
        waitUntilLimitIsReset(e.getResetTimestamp());
      }
    } while (nextCredentials == null);

    useCredentials(nextCredentials);
  }

  public Twitter getTwitter() {
    return twitter;
  }

  private void useCredentials(final TwitterOAuthCredentials credentials) throws TwitterException {
    OAuth2Token token = null;
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(false);
    cb.setApplicationOnlyAuthEnabled(true);
    cb.setOAuthConsumerKey(credentials.getConsumerKey());
    cb.setOAuthConsumerSecret(credentials.getConsumerSecret());

    token = new TwitterFactory(cb.build()).getInstance().getOAuth2Token();

    cb = new ConfigurationBuilder();
    cb.setDebugEnabled(false);
    cb.setApplicationOnlyAuthEnabled(true);
    cb.setOAuthConsumerKey(credentials.getConsumerKey());
    cb.setOAuthConsumerSecret(credentials.getConsumerSecret());

    cb.setOAuth2TokenType(token.getTokenType());
    cb.setOAuth2AccessToken(token.getAccessToken());

    credentialsInUse = credentials;
    twitter = new TwitterFactory(cb.build()).getInstance();

    LOGGER.info(String.format("Using credentials #%d", credentialsInUse.getCredentialNumber()));
  }

  private TwitterOAuthCredentials getNextCredentials() throws LimitReachedException {
    if (!credentialsIterator.hasNext()) {
      credentialsIterator = credentialsList.listIterator();
    }

    final TwitterOAuthCredentials nextCredentials = credentialsIterator.next();

    if (!areCredentialsUsable(nextCredentials)) {
      if (credentialsIterator.previousIndex() == -1) {
        credentialsIterator = credentialsList.listIterator();
      } else {
        credentialsIterator.previous();
      }

      throw new LimitReachedException(nextCredentials.getRateLimitResetTimestamp());
    }

    nextCredentials.clearResetTimestamp();
    return nextCredentials;
  }

  private boolean areCredentialsUsable(final TwitterOAuthCredentials credentials) {
    if (credentials.getRateLimitResetTimestamp() == null) {
      return true;
    }

    return (calculateWaitingTime(credentials.getRateLimitResetTimestamp()) < 0);
  }

  private void waitUntilLimitIsReset(final long resetTimestamp) {
    long waitingTime = calculateWaitingTime(resetTimestamp);

    if (waitingTime <= 0) {
      return;
    }

    synchronized (lock) {
      do {
        final ScheduledFuture<?> resetTaskFuture = scheduler.schedule(new ResetTimerTask(lock), waitingTime,
            TimeUnit.MILLISECONDS);

        LOGGER.info(String.format("No free credentials found, waiting until reset: %s",
            Clock.dateForGivenTimeInLocalTimeZone(resetTimestamp)));

        try {
          lock.wait();
        } catch (final InterruptedException e) {
          resetTaskFuture.cancel(true);
          Thread.interrupted();
          LOGGER.error("Waiting job was interrupted", e);
          break;
        }

        waitingTime = calculateWaitingTime(resetTimestamp);

      } while (waitingTime > 0);
    }
  }

  private long calculateWaitingTime(final long resetTimestamp) {
    final Calendar currentTime = Clock.currentTime();
    final Calendar resetTime = Clock.givenTime(resetTimestamp);

    return ((resetTime.getTimeInMillis() - currentTime.getTimeInMillis()) + 30000L);
  }

  private void loadCredentialsFromFile() {
    credentialsList = new LinkedList<TwitterOAuthCredentials>();

    final InputStream resource = ClassLoader.getSystemResourceAsStream(TwitterConnectorConstants.CREDENTIALS_FILE_NAME);

    try {
      final List<String> readLines = IOUtils.readLines(resource, TwitterConnectorConstants.ENCODING);

      String consumerKey = null;
      String consumerSecret = null;
      String accessToken = null;
      String accessTokenSecret = null;

      for (final String readLine : readLines) {
        final String line = readLine.trim();

        if (line.isEmpty()) {
          continue;
        }

        final String[] values = readLine.split("=");

        if (values.length != 2) {
          throw new IllegalArgumentException("Credentials file is corrupt. Read line does not contain 2 values.");
        }

        final String key = values[0].trim();
        final String value = values[1].trim();

        switch (key) {
          case "oauth.consumerKey":
            consumerKey = value;
            break;
          case "oauth.consumerSecret":
            consumerSecret = value;
            break;
          case "oauth.accessToken":
            accessToken = value;
            break;
          case "oauth.accessTokenSecret":
            accessTokenSecret = value;
            break;
          default:
            throw new IllegalArgumentException("Illegal key for credentials file: " + key);
        }

        if (consumerKey != null && consumerSecret != null && accessToken != null && accessTokenSecret != null) {
          final TwitterOAuthCredentials credentials = new TwitterOAuthCredentials(consumerKey, consumerSecret,
              accessToken, accessTokenSecret);
          credentialsList.add(credentials);

          consumerKey = null;
          consumerSecret = null;
          accessToken = null;
          accessTokenSecret = null;
        } else if (accessTokenSecret != null) {
          throw new IllegalArgumentException("Credentials file is corrupt");
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final Exception e) {
      throw e;
    } finally {
      try {
        resource.close();
      } catch (final IOException e) {
        LOGGER
            .error(String.format("File stream of file %s couldn't be closed",
                TwitterConnectorConstants.CREDENTIALS_FILE_NAME), e);
      }
    }

    credentialsIterator = credentialsList.listIterator();
  }
}
