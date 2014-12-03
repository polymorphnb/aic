package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.commons.Maybe;
import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.DtoFactory;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.ListTaskResult;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class FindTweetsForUserTask implements Task<ListTaskResult<Tweet>> {

  private static final Logger LOGGER = LoggerFactory.getLogger(FindTweetsForUserTask.class);

  private final long userId;

  private final Long maxId;

  private FindTweetsForUserTask(final long userId, final Long maxId) {
    this.userId = userId;
    this.maxId = maxId;
  }

  public static FindTweetsForUserTask newInstanceForFirstSearch(final long userId) {
    return new FindTweetsForUserTask(userId, null);
  }

  public static FindTweetsForUserTask newInstanceForContinuedSearch(final long userId, final long maxId) {
    return new FindTweetsForUserTask(userId, maxId);
  }

  @Override
  public ListTaskResult<Tweet> execute() throws LimitReachedException, TwitterConnectorException, ConnectionException,
      HttpRetryProblemException {

    final Twitter twitter = TwitterAuthenticationService.getInstance().getTwitter();

    final Paging paging = new Paging();
    paging.setCount(FindTweetsForUserJob.AMOUNT_OF_TWEETS_PER_REQUEST);

    if (maxId != null) {
      paging.setMaxId(maxId);
    }

    final List<Tweet> tweets = new ArrayList<Tweet>(
        Integer.highestOneBit(FindTweetsForUserJob.AMOUNT_OF_TWEETS_PER_REQUEST) * 2);

    try {
      final ResponseList<Status> result = twitter.timelines().getUserTimeline(userId, paging);

      for (final Status status : result) {
        if (maxId != null && maxId.longValue() == status.getId()) {
          // do not include already sampled tweet
          continue;
        }

        // dto factory performs sanity checks
        final Maybe<Tweet> maybeTweet = DtoFactory.createTweetFromStatus("<user>", status, false);

        if (maybeTweet.isKnown()) {
          tweets.add(maybeTweet.value());
        }
      }

      return new ListTaskResult<Tweet>(result.getRateLimitStatus(), tweets);

    } catch (final TwitterException e) {
      if (e.exceededRateLimitation()) {
        throw new LimitReachedException(e, e.getRateLimitStatus());
      } else if (e.isCausedByNetworkIssue()) {
        throw new ConnectionException(e);
      } else if (TwitterConnectorConstants.HTTP_RETRY_PROBLEMS.contains(e.getStatusCode())) {
        if (e.getStatusCode() == TwitterConnectorConstants.HTTP_UNAUTHORIZED) {
          LOGGER.warn("Encountered HTTP 401 error for user id " + userId);
          //might be a protected user ...
          throw new TwitterConnectorException(e);
        } else {
          throw new HttpRetryProblemException(e);
        }
      } else {
        throw new TwitterConnectorException(e);
      }
    }
  }
}
