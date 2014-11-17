package ac.at.tuwien.tdm.twitter.connector.job;

import ac.at.tuwien.tdm.twitter.connector.job.LookUpUsersJob.Builder;

import java.util.List;

public final class JobBuilder {

  private JobBuilder() {
  }

  public static SearchTweetsJob.Builder SearchTweetsJob(final String searchTerm) {
    return new SearchTweetsJob.Builder(searchTerm);
  }

  public static LookUpUsersJob.Builder FindUserJob(final List<Long> userIdsToLookUp) {
    return new LookUpUsersJob.Builder(userIdsToLookUp);
  }
}
