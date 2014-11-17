package ac.at.tuwien.tdm.file.dumper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import com.google.gson.Gson;

abstract class TwitterFileWriter<T> {

  private static final long timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTimeInMillis();

  private static final Gson gson = new Gson();

  private final String filePath;

  private BufferedWriter fileWriter;

  protected TwitterFileWriter(final String fileName, final String fileExtension) {
    this.filePath = (fileName + timestamp + fileExtension);
  }

  public void appendToFile(final List<T> dataList) throws IOException {
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

    }
  }
}
