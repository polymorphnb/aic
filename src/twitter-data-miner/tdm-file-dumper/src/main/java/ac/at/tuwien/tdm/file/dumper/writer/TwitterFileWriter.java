package ac.at.tuwien.tdm.file.dumper.writer;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * A simple file writer that persists a list of data as json (one entry per line). Appending content is thread safe.
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 */
public abstract class TwitterFileWriter<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFileWriter.class);

  private static final long timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();

  private static final Gson gson = new Gson();

  private final String filePath;

  private BufferedWriter fileWriter;

  protected TwitterFileWriter(final String fileName, final String fileExtension) {
    this.filePath = (fileName + timestamp + fileExtension);
  }

  public synchronized void appendToFile(final List<T> dataList) throws IOException {
    final BufferedWriter fileWriter = openFileAsStream();

    for (final T data : dataList) {
      fileWriter.append(gson.toJson(data));
      fileWriter.newLine();
    }
  }

  private BufferedWriter openFileAsStream() throws IOException {
    if (fileWriter == null) {
      final File tweetFile = new File(filePath);

      final File dir = tweetFile.getParentFile();

      if (!dir.exists()) {
        dir.mkdirs();
      }

      if (!tweetFile.exists()) {
        tweetFile.createNewFile();
      }

      fileWriter = new BufferedWriter(new FileWriter(filePath, true));
    }

    return fileWriter;
  }

  public void closeFileStream() {
    try {
      if (fileWriter != null) {
        fileWriter.close();
      }
    } catch (IOException e) {
      LOGGER.error(String.format("Couldn't close file writer of file '%s'", filePath), e);
    }
  }
}
