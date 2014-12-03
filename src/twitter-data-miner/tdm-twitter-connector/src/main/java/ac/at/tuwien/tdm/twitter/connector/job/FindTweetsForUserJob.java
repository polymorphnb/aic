package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.ListTaskResult;
import ac.at.tuwien.tdm.twitter.connector.result.MapTaskResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import twitter4j.RateLimitStatus;
import twitter4j.TwitterException;

public final class FindTweetsForUserJob extends AbstractJob<List<Tweet>> {

  public static final int AMOUNT_OF_TWEETS_PER_REQUEST = 200;

  private final long userId;

  private final int maxResults;

  private FindTweetsForUserJob(final Builder b) {
    this.userId = b.userId;
    this.maxResults = b.maxResults;
  }

  @Override
  public List<Tweet> call() throws TwitterConnectorException {

    final List<Tweet> tweets = new ArrayList<Tweet>(Integer.highestOneBit(maxResults) * 2);

    FindTweetsForUserTask task = FindTweetsForUserTask.newInstanceForFirstSearch(userId);
    ListTaskResult<Tweet> taskResult = null;

    try {
      do {
        try {
          checkRateLimit();
          taskResult = task.execute();
        } catch (final LimitReachedException e) {
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (taskResult == null);

      List<Tweet> result = taskResult.getResult();
      tweets.addAll(result);

      // amount of results < theoretically possible result amount, therefore no further requests are needed
      if (tweets.size() < AMOUNT_OF_TWEETS_PER_REQUEST) {
        return tweets;
      }

      long maxIdForNextRequest = Collections.min(result).getId();
      checkRateLimit(taskResult);

      do {
        try {
          task = FindTweetsForUserTask.newInstanceForContinuedSearch(userId, maxIdForNextRequest);
          taskResult = task.execute();

          result = taskResult.getResult();
          tweets.addAll(result);
          maxIdForNextRequest = Collections.min(result).getId();
          checkRateLimit(taskResult);

        } catch (final LimitReachedException e) {
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (tweets.size() < maxResults && result.size() == AMOUNT_OF_TWEETS_PER_REQUEST);

    } catch (final TwitterException e) {
      throw new TwitterConnectorException(e);
    }

    return tweets;
  }

  private void checkRateLimit() throws LimitReachedException, TwitterConnectorException, ConnectionException,
      HttpRetryProblemException {
    final MapTaskResult<String, RateLimitStatus> taskResult = GetRateLimitTask.newInstance().execute();
    final Map<String, RateLimitStatus> rateLimits = taskResult.getResult();
    final int remainingRequests = rateLimits.get("/statuses/user_timeline").getRemaining();

    if (remainingRequests == 0) {
      throw new LimitReachedException(rateLimits.get("/statuses/user_timeline"));
    }
  }

  @Override
  protected String getRequestType() {
    return "findTweets";
  }

  public static class Builder {

    private final long userId;

    // optional
    private int maxResults = TwitterConnectorConstants.DEFAULT_MAX_TWEETS_PER_USER_SEARCH;

    public Builder(final long userId) {
      this.userId = userId;
    }

    public Builder maxResults(final int maxResults) {
      this.maxResults = maxResults;
      return this;
    }

    public FindTweetsForUserJob build() {
      return new FindTweetsForUserJob(this);
    }
  }
}
