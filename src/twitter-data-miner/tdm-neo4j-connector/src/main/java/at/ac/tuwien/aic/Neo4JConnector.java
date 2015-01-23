package at.ac.tuwien.aic;

import java.util.List;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.docstore.DocStoreConnector;
import ac.at.tuwien.tdm.results.DirectInterestResult;


public interface Neo4JConnector {
  
  public void connectBatchInsert();
  
  public void disconnectBatchInsert();
  
  public void connect(boolean batchInsert);
  
  public void disconnect();
  
  public String getUserViaCypher(Long userID);
  
  public String getUserAsString(Long userID);
  
  public void addTopic(Long topicID);
  
  public void addUserNode(Long id);
  
  public void addUser(User user, boolean fullUser);
  
  public void addFollowsRelationship(Long userID1, Long userID2);
  
  public void addFriendsRelationship(Long userID1, Long userID2);
  
  public void addRepliesRelationship(Long userID1, Long userID2);
  
  public void addRetweetsRelationship(Long userID1, Long userID2);
  
  public void addInteractsWithRelationship(Long userID1, Long userID2);
  
  public void addInterestedInRelationship(Long userID1, Long topicID, int weight);
  
  public void startTransaction();
  
  public void closeTransaction();
  
  public List<DirectInterestResult> getDirectInterestsForUser(Long userId, int interestThreshold, DocStoreConnector docstore);
  
  public void getIndirectInterestsForUser(Long userId, int maxDepth, int interestThreshold);

}
