package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.commons.pojo.Tweet;
import ac.at.tuwien.tdm.processor.reader.ConfigConstants;

import java.io.File;
import java.io.IOException;
import java.nio.file.StandardCopyOption;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class TwitterTweetDataProcessor extends TwitterDataProcessor {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTweetDataProcessor.class);
  
  public TwitterTweetDataProcessor(String neo4jDBPath, String neo4jPropertiesPath, String userDBPath, String userDBTablePath) {
    super(ConfigConstants.TWEETS_FOLDER, ConfigConstants.TWEETS_FOLDER_PROCESSED, neo4jDBPath, neo4jPropertiesPath, userDBPath, userDBTablePath);
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
        this.updateUserInUserDB(tweet);
//        this.addRetweetCountToUser(tweet);
//        this.updateTweetCountForUser(tweet);
//        this.updateFavoritesCountForUser(tweet);
        i++;
        
        if(i%100 == 0) {
          LOGGER.info(i + " Tweets processed from file " + file.getName() + "!");
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
    }
  }
  
  private void updateUserInUserDB(Tweet tweet) {
    this.userDB.updateFavoritedCountTweetCountRetweetCountForUser(tweet.getAuthorUserId(), tweet.getRetweetedCount(), tweet.getFavoritedCount());
  }
  
  @SuppressWarnings("unused")
  private void updateFavoritesCountForUser(Tweet tweet) {
    if(tweet.getFavoritedCount() > 0) {
      this.userDB.updateFavoritedCountForUser(tweet.getAuthorUserId(), tweet.getFavoritedCount());
    }
  }
  
  @SuppressWarnings("unused")
  private void updateTweetCountForUser(Tweet tweet) {
    this.userDB.updateTweetCountForUser(tweet.getAuthorUserId());
  }
  
  @SuppressWarnings("unused")
  private void addRetweetCountToUser(Tweet tweet) {
    if(tweet.getRetweetedCount() > 0) {
      this.userDB.updateRetweetCountForUser(tweet.getAuthorUserId(), tweet.getRetweetedCount());
    }
  }

  private void addTopicInterestsUser(Tweet tweet) {
    Long topicID = this.docStore.getTopicIDForKeyword(tweet.getSearchTerm());
    this.neo4j.addInterestedInRelationship(tweet.getAuthorUserId(), topicID);
    //this.docStore.addTopicToUser(tweet.getAuthorUserId(), topicID);
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
