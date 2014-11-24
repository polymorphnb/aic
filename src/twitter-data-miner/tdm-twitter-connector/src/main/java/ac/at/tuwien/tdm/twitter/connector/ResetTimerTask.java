package ac.at.tuwien.tdm.twitter.connector;

import java.util.concurrent.locks.Lock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ResetTimerTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ResetTimerTask.class);

  private final Lock lock;

  public ResetTimerTask(final Lock lock) {
    this.lock = lock;
  }

  @Override
  public void run() {
    synchronized (lock) {
      lock.notify();
    }

    LOGGER.info("Resuming operations");
  }
}
