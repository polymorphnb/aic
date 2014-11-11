package ac.at.tuwien.tdm.twitter.connector;

public interface TwitterConnector {

  void openConnection() throws TwitterException;
  
  void closeConnection();
}
