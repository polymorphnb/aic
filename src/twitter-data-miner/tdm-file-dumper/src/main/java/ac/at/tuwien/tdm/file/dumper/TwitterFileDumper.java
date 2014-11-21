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

  private final ExecutorService executorService = Executors
      .newFixedThreadPool(FileDumperConstants.AMOUNT_OF_WORKER_THREADS);

  private TwitterFileDumper() {
    // hide constructor
  }

  public static void main(final String[] args) {

    final TwitterConnector connector = TwitterConnectorFactory.createTwitterConnector();
    TwitterFileDumper fileDumper = null;

    try {
      fileDumper = new TwitterFileDumper();
      fileDumper.collect(connector);
    } catch (final Exception e) {
      LOGGER.error("Unexpected exception", e);
    } finally {
      if (fileDumper != null) {
        fileDumper.cleanUpResources();
      }
      connector.shutdownService();
    }
  }

  private void collect(final TwitterConnector connector) throws IOException, TwitterConnectorException,
      InterruptedException {

    final List<TweetSearchTerm> topics = readSearchTermsFromDefaultFile();
    final CountDownLatch latch = new CountDownLatch(topics.size());

    for (final TweetSearchTerm topic : topics) {
      final Pipeline pipeline = Pipeline.newInstance(connector, latch, topic);
      executorService.submit(pipeline);
    }

    latch.await();
    executorService.shutdownNow();
  }

  private List<TweetSearchTerm> readSearchTermsFromDefaultFile() {
    final List<TweetSearchTerm> searchTerms = new ArrayList<>();
    final InputStream resource = ClassLoader.getSystemResourceAsStream(FileDumperConstants.TOPICS_FILE_NAME);

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
        // log and forget
      }
    }

    return searchTerms;
  }

  private void cleanUpResources() {
    TweetFileWriter.getInstance().closeFileStream();
    UserFileWriter.getInstance().closeFileStream();
  }
}
