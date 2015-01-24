package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.processor.reader.ConfigConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TwitterUserDataProcessor extends TwitterDataProcessor {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterUserDataProcessor.class);
  
  public TwitterUserDataProcessor(String neo4jDBPath, String neo4jPropertiesPath, String userDBPath, String userDBTablePath) throws FileNotFoundException {
    super(ConfigConstants.USER_FOLDER, ConfigConstants.USER_FOLDER_PROCESSED, neo4jDBPath, neo4jPropertiesPath, userDBPath, userDBTablePath);
  }
  
  public void process() {
    int i = 0;
    
    Iterator<?> it = reader.getFiles(this.fileFolder);
    
    if(it == null) {
      return;
    }
    
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
        // System.out.println("User " + user.getId() + " processed");
        i++;
        
        if(i%100 == 0) {
          LOGGER.info(i + " User processed from file " + file.getName() + "!");
        }
      }
      this.neo4j.closeTransaction();
      
      reader.closeLineIterator();
      try {
        java.nio.file.Files.move(file.toPath(), new File(this.folderProcessed + file.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException e) {
        LOGGER.info("Could not move file " + file.getName() + " to " + this.folderProcessed + file.getName() + "!");
      }
      
      LOGGER.info("File " + file.getName() + " done!");
      
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
  
  private void addUserToUserDB(User user) {
    this.userDB.insertUser(user);
  }
  
  private void addUserToNeo4J(User user) {
    this.neo4j.addUser(user, true);
  }
}
