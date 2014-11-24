package at.ac.tuwien.aic;


public interface Neo4JConnector {
  
  public void connect();
  
  public void disconnect();
  
  public void addUserNode(String id);
  
  public void addFollowsRelationship(String userID1, String userID2);
  
  public void addFriendsRelationship(String userID1, String userID2);
  
  public void addRepliesRelationship(String userID1, String userID2);
  
  public void addRetweetsRelationship(String userID1, String userID2);

}
