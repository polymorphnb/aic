package ac.at.tuwien.tdm.processor.reader;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class TwitterFileReader {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFileReader.class);
  
  private static final TwitterFileReader INSTANCE = new TwitterFileReader();
  
  private LineIterator fileIt;

  private TwitterFileReader() {
  }

  public static TwitterFileReader getInstance() {
    return INSTANCE;
  }
  
  public Iterator<?> getFiles(String folder) {
    Iterator<?> it = FileUtils.iterateFiles(new File(folder), new String[] {ConfigConstants.TEXT_FILE_EXTENSION}, false);
    return it;
  }
  
  public List<String> getDataForFile(File file) {
    // TODO: read files per fileiterator? FileUtils.lineIterator(file, "UTF-8");
    // this is in case the files are getting to large
    List<String> readLines = null;
    try {
      readLines = FileUtils.readLines(file, ConfigConstants.ENCODING);
    } catch (IOException e) {
      LOGGER.error(e.getMessage());
    }
    return readLines;
  }
  
  public void loadFileContent(File file) {
    try {
      fileIt = FileUtils.lineIterator(file, "UTF-8");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  public String getNextLine() {
    if(fileIt.hasNext()) {
      return fileIt.next();
    }
    return null;
  }
  
  public void closeLineIterator() {
    fileIt.close();
  }

}
