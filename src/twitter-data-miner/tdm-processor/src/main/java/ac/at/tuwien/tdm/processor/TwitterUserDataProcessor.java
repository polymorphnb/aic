package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.processor.reader.ConfigConstants;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TwitterUserDataProcessor extends TwitterDataProcessor {
  
  public TwitterUserDataProcessor() {
    
  }
  
  public void process() {
    
    Iterator<?> it = reader.getFiles(ConfigConstants.USER_FOLDER);
    while(it.hasNext()) {
      File file = (File)it.next();
      final List<String> readLines = reader.getDataForFile(file);
      for (final String readLine : readLines) {
        
        User user = gson.fromJson(readLine, User.class);
        
        this.addUserFollowersRelationship(user);
        this.addUserFriendsRelationship(user);
      }
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
  

}
