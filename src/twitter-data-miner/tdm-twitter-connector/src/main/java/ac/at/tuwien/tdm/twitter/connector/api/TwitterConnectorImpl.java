package ac.at.tuwien.tdm.twitter.connector.api;

import ac.at.tuwien.tdm.twitter.connector.job.JobBuilder;
import ac.at.tuwien.tdm.twitter.connector.job.LookUpUsersJob;
import ac.at.tuwien.tdm.twitter.connector.job.SearchTweetsJob;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class TwitterConnectorImpl implements TwitterConnector {

  private final ExecutorService searchTweetsExecutor = Executors.newSingleThreadExecutor();
  
  private final ExecutorService findUserExecutor = Executors.newSingleThreadExecutor();
  
  @Override
  public Future<List<Tweet>> findByKeyWord(final String searchTerm, final boolean searchOnlyInHashTags) {

    final SearchTweetsJob searchTweetsJob = 
        JobBuilder
          .SearchTweetsJob(searchTerm)
          .searchOnlyInHashTags(searchOnlyInHashTags)
          .build();
      
    return searchTweetsExecutor.submit(searchTweetsJob);
  }

  @Override
  public Future<List<Tweet>> findByKeyWord(final String searchTerm, final boolean searchOnlyInHashTags, 
      final int maxResults) {

    final SearchTweetsJob searchTweetsJob = 
        JobBuilder
          .SearchTweetsJob(searchTerm)
          .searchOnlyInHashTags(searchOnlyInHashTags)
          .maxResults(maxResults)
          .build();
      
    return searchTweetsExecutor.submit(searchTweetsJob);
  }

  @Override
  public Future<List<User>> lookUpUsersById(final List<Long> userIdsToLookUp) {

    final LookUpUsersJob findUserJob =
        JobBuilder
          .FindUserJob(userIdsToLookUp)
          .build();
    
    return findUserExecutor.submit(findUserJob);
  }

  @Override
  public void shutdownService() {
    searchTweetsExecutor.shutdownNow();
    findUserExecutor.shutdownNow();
  }
}
