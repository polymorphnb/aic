package ac.at.tuwien.tdm.file.dumper.writer;

import ac.at.tuwien.tdm.file.dumper.FileDumperConstants;
import ac.at.tuwien.tdm.twitter.connector.api.User;

/**
 * A simple file writer for users (one user per line as json)
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class UserFileWriter extends TwitterFileWriter<User> {

  private static final UserFileWriter INSTANCE = new UserFileWriter();

  private UserFileWriter() {
    super(FileDumperConstants.USER_FILE_NAME, FileDumperConstants.TEXT_FILE_EXTENSION);
  }

  public static UserFileWriter getInstance() {
    return INSTANCE;
  }
}
