package ac.at.tuwien.tdm.file.dumper;

import ac.at.tuwien.tdm.twitter.connector.api.User;

final class UserFileWriter extends TwitterFileWriter<User> {

  private static final UserFileWriter INSTANCE = new UserFileWriter();

  private UserFileWriter() {
    super(Constants.USER_FILE_NAME, Constants.TEXT_FILE_EXTENSION);
  }

  public static UserFileWriter getInstance() {
    return INSTANCE;
  }
}
