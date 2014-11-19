package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.Tweet;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;

import java.util.ArrayList;
import java.util.List;

/**
 * Looks for up to 'maxResults' tweets containing a given search term. <br />
 * default value: {@link TwitterConnectorConstants.DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH}
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class SearchTweetsJob implements Job<List<Tweet>> {

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

    final List<Tweet> allTweets = new ArrayList<>(Integer.highestOneBit(maxResults) * 2);
    int resultsPerPage = TwitterConnectorConstants.DEFAULT_AMOUNT_OF_TWEETS_PER_RESULT_PAGE;

    try {
      SearchTweetsTask task = SearchTweetsTask.newInstanceForFirstSearch(this, resultsPerPage);
      TweetSearchResult result = task.execute();
      allTweets.addAll(result.getTweets());

      int searchRuns = 1;

      while (result.getNextQuery() != null) {

        boolean isLastSearch = false;

        if ((searchRuns + 1) * resultsPerPage >= maxResults) {
          isLastSearch = true;
          resultsPerPage = (maxResults - (searchRuns * resultsPerPage));
        }

        task = SearchTweetsTask.newInstanceForContinuingSearch(result.getNextQuery(), resultsPerPage);
        result = task.execute();

        allTweets.addAll(result.getTweets());
        searchRuns++;

        if (isLastSearch) {
          break;
        }
      }
    } catch (final LimitReachedException e) {
      //FIXME
      throw new RuntimeException(e);
    }

    return allTweets;
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
