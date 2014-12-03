package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.processor.reader.ConfigConstants;
import ac.at.tuwien.tdm.userdb.UserDBConnector;

import java.io.File;
import java.util.Iterator;

public class TwitterUserDataProcessor extends TwitterDataProcessor {
  
  protected final UserDBConnector userDB = UserDBConnector.getInstance();
  
  public TwitterUserDataProcessor() {
    //this.neo4j.connect(false);
    this.neo4j.connectBatchInsert();
  }
  
  public void process() {
    
    int i = 0;
    
    Iterator<?> it = reader.getFiles(ConfigConstants.USER_FOLDER);
    while(it.hasNext()) {
      File file = (File)it.next();
      
      reader.loadFileContent(file);
      String temp = "";
      this.neo4j.startTransaction();
      i = 0;
      while((temp = reader.getNextLine()) != null) {
        User user = gson.fromJson(temp, User.class);
        
        this.addUserToUserDB(user);
        this.addUserToNeo4J(user);
        System.out.println("User " + user.getId() + " processed");
        i++;
        
        if(i%100 == 0) {
          System.out.println(i + " User processed!");
        }
      }
      this.neo4j.closeTransaction();
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
    this.neo4j.disconnect();
  }
  
  private void addUserToUserDB(User user) {
    this.userDB.insertUser(user);
  }
  
  private void addUserToNeo4J(User user) {
    this.neo4j.addUser(user, true);
  }
  
  public void getUser(Long userID) {
    this.neo4j.startTransaction();
    System.out.println(this.neo4j.getUserViaCypher(userID));
    System.out.println(this.neo4j.getUserAsString(userID));
    this.neo4j.closeTransaction();
  }
}
