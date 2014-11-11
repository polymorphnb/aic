package ac.at.tuwien.tdm.twitter.connector;


final class TwitterException extends Exception {

  public TwitterException(final String msg){
    super(msg);
  }
  
  public TwitterException(final String msg, final Throwable t){
    super(msg, t);
  }
}

