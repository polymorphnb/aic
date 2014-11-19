package ac.at.tuwien.tdm.twitter.connector.api;

import ac.at.tuwien.tdm.twitter.connector.Defense;
import ac.at.tuwien.tdm.twitter.connector.job.JobBuilder;
import ac.at.tuwien.tdm.twitter.connector.job.LookUpUsersJob;
import ac.at.tuwien.tdm.twitter.connector.job.SearchTweetsJob;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Implementation of {@link TwitterConnector}
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 *
 */
public final class TwitterConnectorImpl implements TwitterConnector {

  private final ExecutorService searchTweetsExecutor = Executors.newSingleThreadExecutor();
  
  private final ExecutorService lookupUsersExecutor = Executors.newSingleThreadExecutor();
  
  @Override
  public Future<List<Tweet>> findByKeyWord(final String searchTerm, final boolean searchOnlyInHashTags) 
      throws TwitterConnectorException {
    Defense.notBlank("searchTerm", searchTerm);
    
    final SearchTweetsJob searchTweetsJob = 
        JobBuilder
          .SearchTweetsJob(searchTerm)
          .searchOnlyInHashTags(searchOnlyInHashTags)
          .build();
      
    return searchTweetsExecutor.submit(searchTweetsJob);
  }

  @Override
  public Future<List<Tweet>> findByKeyWord(final String searchTerm, final boolean searchOnlyInHashTags, 
      final int maxResults) throws TwitterConnectorException {
    Defense.notBlank("searchTerm", searchTerm);
    Defense.biggerThanZero("maxResults", maxResults);

    final SearchTweetsJob searchTweetsJob = 
        JobBuilder
          .SearchTweetsJob(searchTerm)
          .searchOnlyInHashTags(searchOnlyInHashTags)
          .maxResults(maxResults)
          .build();
      
    return searchTweetsExecutor.submit(searchTweetsJob);
  }

  @Override
  public Future<List<User>> lookUpUsersById(final List<Long> userIdsToLookUp) throws TwitterConnectorException {
    Defense.notEmpty("userIdsToLookUp", userIdsToLookUp);
    
    final LookUpUsersJob findUserJob =
        JobBuilder
          .LookUpUsersJob(userIdsToLookUp)
          .build();
    
    return lookupUsersExecutor.submit(findUserJob);
  }

  @Override
  public void shutdownService() {
    searchTweetsExecutor.shutdownNow();
    lookupUsersExecutor.shutdownNow();
  }
}
