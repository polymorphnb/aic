package ac.at.tuwien.tdm.twitter.connector.api;

import ac.at.tuwien.tdm.twitter.connector.Defense;
import ac.at.tuwien.tdm.twitter.connector.TwitterAuthenticationService;
import ac.at.tuwien.tdm.twitter.connector.job.FindFollowersJob;
import ac.at.tuwien.tdm.twitter.connector.job.FindFriendsJob;
import ac.at.tuwien.tdm.twitter.connector.job.JobBuilder;
import ac.at.tuwien.tdm.twitter.connector.job.LimitReachedException;
import ac.at.tuwien.tdm.twitter.connector.job.LookUpUsersJob;
import ac.at.tuwien.tdm.twitter.connector.job.SearchTweetsJob;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import twitter4j.TwitterException;

/**
 * Implementation of {@link TwitterConnector}
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 *
 */
public final class TwitterConnectorImpl implements TwitterConnector {

  private final ExecutorService requestExecutor = Executors.newSingleThreadExecutor();
  
  public TwitterConnectorImpl() throws LimitReachedException, TwitterException {
    TwitterAuthenticationService.getInstance().prepareFirstUse();
  }
  
  @Override
  public Future<List<Tweet>> findByKeyWord(final String searchTerm, final boolean searchOnlyInHashTags) {
    Defense.notBlank("searchTerm", searchTerm);
    
    final SearchTweetsJob searchTweetsJob = 
        JobBuilder
          .SearchTweetsJob(searchTerm)
          .searchOnlyInHashTags(searchOnlyInHashTags)
          .build();
      
    return requestExecutor.submit(searchTweetsJob);
  }

  @Override
  public Future<List<Tweet>> findByKeyWord(final String searchTerm, final boolean searchOnlyInHashTags, 
      final int maxResults) {
    Defense.notBlank("searchTerm", searchTerm);
    Defense.biggerThanZero("maxResults", maxResults);

    final SearchTweetsJob searchTweetsJob = 
        JobBuilder
          .SearchTweetsJob(searchTerm)
          .searchOnlyInHashTags(searchOnlyInHashTags)
          .maxResults(maxResults)
          .build();
      
    return requestExecutor.submit(searchTweetsJob);
  }

  @Override
  public Future<List<User>> lookUpUsersById(final List<Long> userIdsToLookUp) {
    Defense.notEmpty("userIdsToLookUp", userIdsToLookUp);
    
    final LookUpUsersJob findUserJob =
        JobBuilder
          .LookUpUsersJob(userIdsToLookUp)
          .build();
    
    return requestExecutor.submit(findUserJob);
  }

  @Override
  public Future<List<Long>> findFollowerIdsForUserId(final Long userIdToLookUp, int followersCount) {
    Defense.notNull("userIdToLookUp", userIdToLookUp);
    
    final FindFollowersJob findFollowersJob = 
        JobBuilder
          .FindFollowersJob(userIdToLookUp)
          .withFollowersCount(followersCount)
          .build();
    
    return requestExecutor.submit(findFollowersJob);
  }

  @Override
  public Future<List<Long>> findFriendIdsForUserId(final Long userIdToLookUp, int friendsCount) {
    Defense.notNull("userIdToLookUp", userIdToLookUp);
    
    final FindFriendsJob findFriendsJob = 
        JobBuilder
          .FindFriendsJob(userIdToLookUp)
          .withFriendsCount(friendsCount)
          .build();
    
    return requestExecutor.submit(findFriendsJob);
  }
  
  @Override
  public void shutdownService() {
    requestExecutor.shutdownNow();
    TwitterAuthenticationService.getInstance().shutdown();
  }
}
