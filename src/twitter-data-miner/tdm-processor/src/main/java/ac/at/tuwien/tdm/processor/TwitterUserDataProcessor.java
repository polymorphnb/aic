package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.processor.reader.ConfigConstants;
import ac.at.tuwien.tdm.userdb.UserDBConnector;

import java.io.File;
import java.util.Iterator;

public class TwitterUserDataProcessor extends TwitterDataProcessor {
  
  protected final UserDBConnector userDB = UserDBConnector.getInstance();
  
  public TwitterUserDataProcessor() {
    
  }
  
  public void process() {
    
    Iterator<?> it = reader.getFiles(ConfigConstants.USER_FOLDER);
    while(it.hasNext()) {
      File file = (File)it.next();
      
      reader.loadFileContent(file);
      String temp = "";
      while((temp = reader.getNextLine()) != null) {
        User user = gson.fromJson(temp, User.class);
        
        this.addUserToUserDB(user);
        this.addUserToNeo4J(user);
        this.addUserFollowersRelationship(user);
        this.addUserFriendsRelationship(user);
        System.out.println("User " + user.getId() + " processed");
      }
      System.out.println("File " + file.getName() + " done!");
      
//      final List<String> readLines = reader.getDataForFile(file);
//      for (final String readLine : readLines) {
//        
//        User user = gson.fromJson(readLine, User.class);
//        
//        this.addUserFollowersRelationship(user);
//        this.addUserFriendsRelationship(user);
//      }
    }
  }
  
  private void addUserFollowersRelationship(User user) {
    for(Long userID : user.getFollowerUserIds()) {
      this.neo4j.addFollowsRelationship(String.valueOf(user.getId()), userID.toString());
    }
  }
  
  private void addUserFriendsRelationship(User user) {
    for(Long userID : user.getFollowerUserIds()) {
      this.neo4j.addFriendsRelationship(String.valueOf(user.getId()), userID.toString());
    }
  }
  
  private void addUserToUserDB(User user) {
    this.userDB.insertUser(user);
  }
  
  private void addUserToNeo4J(User user) {
    this.neo4j.addUserNode(String.valueOf(user.getId()));
  }
}
