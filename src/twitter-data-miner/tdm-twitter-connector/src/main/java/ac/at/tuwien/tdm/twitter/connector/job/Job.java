package ac.at.tuwien.tdm.twitter.connector.job;

import java.util.concurrent.Callable;


public interface Job<T> extends Callable<T>{
  
}
