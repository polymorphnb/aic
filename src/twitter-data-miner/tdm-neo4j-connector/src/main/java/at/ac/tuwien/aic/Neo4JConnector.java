package at.ac.tuwien.aic;

import ac.at.tuwien.tdm.commons.pojo.User;


public interface Neo4JConnector {
  
  public void connectBatchInsert();
  
  public void disconnectBatchInsert();
  
  public void connect();
  
  public void disconnect();
  
  public void addUserNode(Long id);
  
  public void addUser(User user, boolean fullUser);
  
  public void addFollowsRelationship(Long userID1, Long userID2);
  
  public void addFriendsRelationship(Long userID1, Long userID2);
  
  public void addRepliesRelationship(Long userID1, Long userID2);
  
  public void addRetweetsRelationship(Long userID1, Long userID2);
  
  public void addInterestedInRelationship(Long userID1, Long topicID, int weight);
  
  public void startTransaction();
  
  public void closeTransaction();

}
