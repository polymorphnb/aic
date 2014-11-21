package ac.at.tuwien.tdm.file.dumper.writer;

import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

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

  private static final long timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();

  private final String filePath;

  private BufferedOutputStream outputStream;

  protected TwitterFileWriter(final String fileName, final String fileExtension) {
    this.filePath = (fileName + timestamp + fileExtension);
  }

  public synchronized void appendToFile(final List<T> dataList) throws IOException {
    final BufferedOutputStream outputStream = openFileAsStream();

    IOUtils.writeLines(dataList, FileDumperConstants.LINE_ENDING, outputStream, FileDumperConstants.ENCODING);
    outputStream.flush();
  }

  private BufferedOutputStream openFileAsStream() throws IOException {
    if (outputStream == null) {
      final File tweetFile = new File(filePath);
      outputStream = new BufferedOutputStream(FileUtils.openOutputStream(tweetFile, true));
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
}
