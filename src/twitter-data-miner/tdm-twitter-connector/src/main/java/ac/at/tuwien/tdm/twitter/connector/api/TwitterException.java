package ac.at.tuwien.tdm.twitter.connector.api;


final class TwitterException extends Exception {

  public TwitterException(final String msg){
    super(msg);
  }
  
  public TwitterException(final Throwable t){
    super(t);
  }
  
  public TwitterException(final String msg, final Throwable t){
    super(msg, t);
  }
}

