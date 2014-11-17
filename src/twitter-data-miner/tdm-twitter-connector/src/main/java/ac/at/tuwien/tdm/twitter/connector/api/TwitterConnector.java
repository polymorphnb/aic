package ac.at.tuwien.tdm.twitter.connector.api;

import java.util.List;
import java.util.concurrent.Future;

public interface TwitterConnector {

  Future<List<Tweet>> findByKeyWord(String searchTerm, boolean searchOnlyInHashTags);

  Future<List<Tweet>> findByKeyWord(String searchTerm, boolean searchOnlyInHashTags, int maxResults);

  // limited to 100 ids
  Future<List<User>> lookUpUsersById(List<Long> ids);
  
  void shutdownService();
}
