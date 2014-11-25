package ac.at.tuwien.tdm.file.dumper.writer;

import ac.at.tuwien.tdm.file.dumper.Clock;
import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Collection;

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

  private String currentSearchTerm;

  private long currentEntryCount = 0;

  protected TwitterFileWriter(final String fileName, final String fileExtension) {
    this.fileName = fileName;
    this.fileExtension = fileExtension;
  }

  public synchronized void appendToFile(final String searchTerm, final Collection<T> dataList) throws IOException {
    final BufferedOutputStream outputStream = openFileAsStream(searchTerm);

    IOUtils.writeLines(dataList, FileDumperConstants.LINE_ENDING, outputStream, FileDumperConstants.ENCODING);
    outputStream.flush();

    LOGGER.info(String.format("Wrote %d data entries to %s", dataList.size(), filePath));
    currentEntryCount += dataList.size();
  }

  private BufferedOutputStream openFileAsStream(final String searchTerm) throws IOException {
    if (!searchTerm.equals(currentSearchTerm)) {
      currentSearchTerm = searchTerm;

      if (outputStream != null) {
        closeFileStream();
      }

      filePath = buildNewFilePathForCurrentTime(fileName, fileExtension, searchTerm);
      final File twitterFile = new File(filePath);
      outputStream = new BufferedOutputStream(FileUtils.openOutputStream(twitterFile, true));
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
    return currentEntryCount;
  }

  private String buildNewFilePathForCurrentTime(final String fileName, final String fileExtension,
      final String searchTerm) {
    return (fileName + Clock.currentTime().getTimeInMillis() + "_" + searchTerm + fileExtension);
  }
}
