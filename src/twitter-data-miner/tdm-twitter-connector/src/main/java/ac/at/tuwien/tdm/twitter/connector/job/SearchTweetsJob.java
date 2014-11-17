package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.api.Tweet;

import java.util.ArrayList;
import java.util.List;

public final class SearchTweetsJob implements Job<List<Tweet>> {

  private static final int DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH = 1000;

  private static final int DEFAULT_TWEETS_PER_PAGE = 100;

  private final String searchTerm;

  private final boolean searchOnlyInHashTags;

  private final int maxResults;

  private SearchTweetsJob(final Builder b) {
    this.searchTerm = b.searchTerm;
    this.searchOnlyInHashTags = b.searchOnlyInHashTags;
    this.maxResults = b.maxResults;
  }

  @Override
  public List<Tweet> call() throws Exception {

    final List<Tweet> allTweets = new ArrayList<>(Integer.highestOneBit(DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH) * 2);

    SearchTweetsTask task = SearchTweetsTask.newInstanceForFirstSearch(this, DEFAULT_TWEETS_PER_PAGE);
    TweetSearchResult result = task.execute();
    allTweets.addAll(result.getTweets());

    int searchRuns = 1;

    while (result.getNextQuery() != null) {

      boolean isLastSearch = false;
      int tweetsPerPage = DEFAULT_TWEETS_PER_PAGE;

      if ((searchRuns + 1) * DEFAULT_TWEETS_PER_PAGE >= DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH) {
        isLastSearch = true;
        tweetsPerPage = (DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH - (searchRuns * DEFAULT_TWEETS_PER_PAGE));
      }

      task = SearchTweetsTask.newInstanceForContinuingSearch(result.getNextQuery(), tweetsPerPage);
      result = task.execute();

      allTweets.addAll(result.getTweets());
      searchRuns++;

      if (isLastSearch) {
        break;
      }
    }

    return allTweets;
  }

  public String getSearchTerm() {
    return searchTerm;
  }

  public boolean isSearchOnlyInHashTags() {
    return searchOnlyInHashTags;
  }

  public int getMaxResults() {
    return maxResults;
  }

  public static class Builder {

    private String searchTerm;

    // optional
    private boolean searchOnlyInHashTags = false;

    // optional
    private int maxResults = DEFAULT_MAX_RESULTS_PER_TWEET_SEARCH;

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
