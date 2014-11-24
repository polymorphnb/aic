package ac.at.tuwien.tdm.file.dumper.writer;

import ac.at.tuwien.tdm.file.dumper.Clock;
import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple file writer that persists a list of data as json (one entry per line). Appending content is thread safe.
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 */
public abstract class TwitterFileWriter<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFileWriter.class);

  private final String fileName;

  private final String fileExtension;

  private String filePath;

  private BufferedOutputStream outputStream;

  private long archivedEntryCount = 0;

  private long currentEntryCount = 0;

  protected TwitterFileWriter(final String fileName, final String fileExtension) {
    this.fileName = fileName;
    this.fileExtension = fileExtension;
    this.filePath = buildNewFilePathForCurrentTime(fileName, fileExtension);
  }

  public synchronized void appendToFile(final List<T> dataList) throws IOException {
    final BufferedOutputStream outputStream = openFileAsStream();

    IOUtils.writeLines(dataList, FileDumperConstants.LINE_ENDING, outputStream, FileDumperConstants.ENCODING);
    outputStream.flush();

    LOGGER.info(String.format("Wrote %d data entries to %s", dataList.size(), filePath));
    currentEntryCount += dataList.size();

    rollOverIfNecessary();
  }

  private void rollOverIfNecessary() throws IOException {
    if (currentEntryCount > FileDumperConstants.MAX_ENTRIES_PER_FILE) {
      closeFileStream();
      LOGGER.info(String.format("Closed file '%s', because of roll over to new file", filePath));

      archivedEntryCount += currentEntryCount;
      currentEntryCount = 0;
      filePath = buildNewFilePathForCurrentTime(fileName, fileExtension);
      outputStream = null;
      openFileAsStream();
    }
  }

  private BufferedOutputStream openFileAsStream() throws IOException {
    if (outputStream == null) {
      final File tweetFile = new File(filePath);
      outputStream = new BufferedOutputStream(FileUtils.openOutputStream(tweetFile, true));
      LOGGER.info(String.format("Opened file '%s'", filePath));
    }

    return outputStream;
  }

  public void closeFileStream() {
    try {
      if (outputStream != null) {
        outputStream.close();
      }
    } catch (IOException e) {
      LOGGER.error(String.format("Couldn't close output stream of file '%s'", filePath), e);
    }
  }

  public long getTotalAmountOfEntries() {
    return archivedEntryCount + currentEntryCount;
  }

  private String buildNewFilePathForCurrentTime(final String fileName, final String fileExtension) {
    return (fileName + Clock.currentTime().getTimeInMillis() + fileExtension);
  }
}
