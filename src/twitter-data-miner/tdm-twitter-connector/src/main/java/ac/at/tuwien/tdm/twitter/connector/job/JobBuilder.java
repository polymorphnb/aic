package ac.at.tuwien.tdm.twitter.connector.job;

import java.util.List;

/**
 * Twitter request factory
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class JobBuilder {

  private JobBuilder() {
    // hide constructor
  }

  public static SearchTweetsJob.Builder SearchTweetsJob(final String searchTerm) {
    return new SearchTweetsJob.Builder(searchTerm);
  }

  public static LookUpUsersJob.Builder LookUpUsersJob(final List<Long> userIdsToLookUp) {
    return new LookUpUsersJob.Builder(userIdsToLookUp);
  }
}
