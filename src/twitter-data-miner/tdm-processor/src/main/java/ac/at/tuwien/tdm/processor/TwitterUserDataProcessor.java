package ac.at.tuwien.tdm.processor;

import ac.at.tuwien.tdm.processor.reader.ConfigConstants;
import ac.at.tuwien.tdm.twitter.connector.api.User;

import java.io.File;
import java.util.Iterator;
import java.util.List;

public class TwitterUserDataProcessor extends TwitterDataProcessor {
  
  public TwitterUserDataProcessor() {
    
  }
  
  public void process() {
    
    Iterator<?> it = reader.getFiles(ConfigConstants.USER_FOLDER);
    while(it.hasNext()) {
      File file = (File)it.next();
      final List<String> readLines = reader.getDataForFile(file);
      for (final String readLine : readLines) {
        
        User user = gson.fromJson(readLine, User.class);
        
        // TODO: call graphpart, userdb-part....
      }
    }
  }
  

}
