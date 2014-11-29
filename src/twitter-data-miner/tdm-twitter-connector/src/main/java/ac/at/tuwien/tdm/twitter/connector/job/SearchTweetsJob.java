package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.ConnectionException;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.result.TweetSearchResult;

import java.util.ArrayList;
import java.util.List;

import twitter4j.Query;
import twitter4j.TwitterException;

/**
 * Looks for up to 'maxResults' tweets containing a given search term. <br />
 * default value: {@link TwitterConnectorConstants.DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH}
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class SearchTweetsJob extends AbstractJob<List<Tweet>> {

  private final String searchTerm;

  private final boolean searchOnlyInHashTags;

  private final int maxResults;

  private SearchTweetsJob(final Builder b) {
    this.searchTerm = b.searchTerm;
    this.searchOnlyInHashTags = b.searchOnlyInHashTags;
    this.maxResults = b.maxResults;
  }

  @Override
  public List<Tweet> call() throws TwitterConnectorException {

    final List<Tweet> allTweets = new ArrayList<Tweet>(Integer.highestOneBit(maxResults) * 2);
    int resultsPerPage = TwitterConnectorConstants.DEFAULT_AMOUNT_OF_TWEETS_PER_RESULT_PAGE;

    if (resultsPerPage < 1 || resultsPerPage > 100) {
      throw new IllegalArgumentException(String.format("ResultsPerPage must be between 1 and 100. Actual value: %d",
          resultsPerPage));
    }

    try {
      TweetSearchResult result = null;
      SearchTweetsTask task = SearchTweetsTask.newInstanceForFirstSearch(this, resultsPerPage);

      do {
        try {
          result = task.execute();
        } catch (final LimitReachedException e) {
          handleReachedLimit(e.getResetTimestamp());
        } catch (final ConnectionException e) {
          handleConnectionError();
        } catch (final HttpRetryProblemException e) {
          handleHttpProblem(e.getResetTimestamp());
        }
      } while (result == null);

      allTweets.addAll(result.getResult());
      checkRateLimit(result);

      Query nextQuery = result.getNextQuery();
      int searchRuns = 1;

      if (allTweets.size() < maxResults && nextQuery != null) {
        do {
          try {
            boolean isLastSearch = false;

            if (((searchRuns + 1) * resultsPerPage >= maxResults) && !isLastSearch) {
              isLastSearch = true;
              resultsPerPage = (maxResults - (searchRuns * resultsPerPage));
            }

            task = SearchTweetsTask.newInstanceForContinuingSearch(searchTerm, nextQuery, resultsPerPage);
            result = task.execute();

            allTweets.addAll(result.getResult());
            searchRuns++;

            nextQuery = (isLastSearch ? null : result.getNextQuery());
            checkRateLimit(result);
          } catch (final LimitReachedException e) {
            handleReachedLimit(e.getResetTimestamp());
          } catch (final ConnectionException e) {
            handleConnectionError();
          } catch (final HttpRetryProblemException e) {
            handleHttpProblem(e.getResetTimestamp());
          }
        } while (nextQuery != null && allTweets.size() < maxResults);
      }
    } catch (final TwitterException e) {
      throw new TwitterConnectorException(e);
    }

    return allTweets;
  }

  @Override
  protected String getRequestType() {
    return "searchTweets";
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public boolean isSearchOnlyInHashTags() {
    return searchOnlyInHashTags;
  }

  public static class Builder {

    private String searchTerm;

    // optional
    private boolean searchOnlyInHashTags = false;

    // optional
    private int maxResults = TwitterConnectorConstants.DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH;

    public Builder(final String searchTerm) {
      this.searchTerm = searchTerm;
    }

    public Builder searchOnlyInHashTags(final boolean searchOnlyInHashTags) {
      this.searchOnlyInHashTags = searchOnlyInHashTags;
      return this;
    }

    public Builder maxResults(final int maxResults) {
      this.maxResults = maxResults;
      return this;
    }

    public SearchTweetsJob build() {
      return new SearchTweetsJob(this);
    }
  }
}
