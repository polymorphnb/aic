package ac.at.tuwien.tdm.twitter.connector.job;

import twitter4j.TwitterException;

public interface Task<T> {

  T execute() throws LimitReachedException, TwitterException;

}
