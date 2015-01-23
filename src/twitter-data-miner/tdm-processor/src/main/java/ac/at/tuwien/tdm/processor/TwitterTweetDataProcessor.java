package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.processor.reader.ConfigConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;


public class TwitterTweetDataProcessor extends TwitterDataProcessor {
  
  
  public TwitterTweetDataProcessor() {
    super(ConfigConstants.TWEETS_FOLDER, ConfigConstants.TWEETS_FOLDER_PROCESSED);
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
        Tweet tweet = gson.fromJson(temp, Tweet.class);
                
        this.addTopicInterestsUser(tweet);
        this.addInteractsWithUser(tweet);
        // System.out.println("Tweet " + tweet.getId() + " processed");
        i++;
        
        if(i%100 == 0) {
          System.out.println(i + " Tweet processed!");
        }
      }
      this.neo4j.closeTransaction();
      
      reader.closeLineIterator();
      try {
        java.nio.file.Files.move(file.toPath(), new File(this.folderProcessed + file.getName()).toPath(), StandardCopyOption.ATOMIC_MOVE);
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
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

  private void addTopicInterestsUser(Tweet tweet) {
    Long topicID = this.docStore.getTopicIDForKeyword(tweet.getSearchTerm());
    this.neo4j.addInterestedInRelationship(tweet.getAuthorUserId(), topicID, 0);
  }
  
  private void addInteractsWithUser(Tweet tweet) {
    if(tweet.getRepliedToUserId() != -1) {
      this.neo4j.addInteractsWithRelationship(tweet.getAuthorUserId(), tweet.getRepliedToUserId());
    }
    if(tweet.getRetweetedFromUserId() != -1) {
      this.neo4j.addInteractsWithRelationship(tweet.getAuthorUserId(), tweet.getRetweetedFromUserId());
    }
  }
  
}
