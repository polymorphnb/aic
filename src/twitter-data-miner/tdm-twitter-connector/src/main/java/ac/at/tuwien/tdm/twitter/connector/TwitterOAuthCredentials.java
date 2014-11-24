package ac.at.tuwien.tdm.twitter.connector;

public final class TwitterOAuthCredentials {

  private static int instanceCounter = 0;

  private final int credentialNumber;

  private final String consumerKey;

  private final String consumerSecret;

  private final String accessToken;

  private final String accessTokenSecret;

  private Long rateLimitResetTimestamp;

  public TwitterOAuthCredentials(final String consumerKey, final String consumerSecret, final String accessToken,
      final String accessTokenSecret) {
    this.consumerKey = consumerKey;
    this.consumerSecret = consumerSecret;
    this.accessToken = accessToken;
    this.accessTokenSecret = accessTokenSecret;

    instanceCounter++;
    credentialNumber = instanceCounter;
  }

  public String getConsumerKey() {
    return consumerKey;
  }

  public String getConsumerSecret() {
    return consumerSecret;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getAccessTokenSecret() {
    return accessTokenSecret;
  }

  public Long getRateLimitResetTimestamp() {
    return rateLimitResetTimestamp;
  }

  public void invalidate(final long rateLimitResetTimestamp) {
    this.rateLimitResetTimestamp = rateLimitResetTimestamp;
  }

  public void clearResetTimestamp() {
    this.rateLimitResetTimestamp = null;
  }

  public int getCredentialNumber() {
    return credentialNumber;
  }
}
