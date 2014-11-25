package ac.at.tuwien.tdm.file.dumper;

import ac.at.tuwien.tdm.file.dumper.pipeline.Pipeline;
import ac.at.tuwien.tdm.file.dumper.writer.TweetFileWriter;
import ac.at.tuwien.tdm.file.dumper.writer.UserFileWriter;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorException;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Collects tweets and users related to topics defined by topics.txt
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TwitterFileDumper {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFileDumper.class);

  private final ExecutorService executorService;

  private TwitterFileDumper(final int amountWorkerThreads) {
    executorService = Executors.newFixedThreadPool(amountWorkerThreads);
  }

  public static void main(final String[] args) {

    final long mb = 1024 * 1024;

    final Runtime runtime = Runtime.getRuntime();
    final int maxMemoryInMb = (int) (runtime.maxMemory() / mb);

    final long startUpTime = Clock.currentTime().getTimeInMillis();

    final TwitterConnector connector = TwitterConnectorFactory.createTwitterConnector();
    TwitterFileDumper fileDumper = null;

    try {
      final int amountWorkerThreads = FileDumperConstants.AMOUNT_OF_WORKER_THREADS; // ((maxMemoryInMb / 2) / 100);
      LOGGER.info(String.format("Using %d worker thread(s)", amountWorkerThreads));
      fileDumper = new TwitterFileDumper(amountWorkerThreads);
      fileDumper.collect(connector);
    } catch (final Exception e) {
      LOGGER.error("Unexpected exception", e);
    } finally {
      if (fileDumper != null) {
        fileDumper.cleanUpResources();
      }
      connector.shutdownService();
    }

    final long durationInMilliSeconds = (Clock.currentTime().getTimeInMillis() - startUpTime);
    final long amountOfFoundTweets = TweetFileWriter.getInstance().getTotalAmountOfEntries();
    final long amountOfFoundUsers = UserFileWriter.getInstance().getTotalAmountOfEntries();

    final long durationMinutes = ((durationInMilliSeconds / 1000) / 60);
    final long durationSeconds = ((durationInMilliSeconds - (durationMinutes * 60 * 1000)) / 1000);

    LOGGER.info(String.format("Found %d tweets and %d users in %d minutes and %d seconds", amountOfFoundTweets,
        amountOfFoundUsers, durationMinutes, durationSeconds));
  }

  private void collect(final TwitterConnector connector) throws IOException, TwitterConnectorException,
      InterruptedException {

    final List<TweetSearchTerm> searchTerms = readSearchTermsFromDefaultFile();
    final CountDownLatch latch = new CountDownLatch(searchTerms.size());

    for (final TweetSearchTerm searchTerm : searchTerms) {
      final Pipeline pipeline = Pipeline.newInstance(connector, latch, searchTerm);
      executorService.submit(pipeline);
    }

    latch.await();
    executorService.shutdownNow();
  }

  private List<TweetSearchTerm> readSearchTermsFromDefaultFile() {
    final List<TweetSearchTerm> searchTerms = new ArrayList<>();
    final InputStream resource = ClassLoader.getSystemResourceAsStream(FileDumperConstants.SEARCH_TERMS_FILE_NAME);

    try {
      final List<String> readLines = IOUtils.readLines(resource, FileDumperConstants.ENCODING);

      for (final String readLine : readLines) {
        final String[] values = readLine.split(";");

        if (values.length != 2) {
          throw new IllegalArgumentException("Topics file is corrupt. Read line does not contain 2 values.");
        }

        final String searchTerm = values[0].trim();
        final boolean onlyHashTags = Boolean.parseBoolean(values[1].trim());

        searchTerms.add(new TweetSearchTerm(searchTerm, onlyHashTags));
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    } catch (final Exception e) {
      throw e;
    } finally {
      try {
        resource.close();
      } catch (IOException e) {
        LOGGER.error(
            String.format("File stream for file '%s' couldn't be closed", FileDumperConstants.SEARCH_TERMS_FILE_NAME),
            e);
      }
    }

    return searchTerms;
  }

  private void cleanUpResources() {
    TweetFileWriter.getInstance().closeFileStream();
    UserFileWriter.getInstance().closeFileStream();
  }
}
