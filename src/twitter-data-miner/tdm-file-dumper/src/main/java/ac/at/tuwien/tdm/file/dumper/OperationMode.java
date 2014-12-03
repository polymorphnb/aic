package ac.at.tuwien.tdm.file.dumper;

import java.util.EnumSet;
import java.util.Set;

public enum OperationMode {
  LOOK_UP_SEARCH_TERMS(11),

  FIND_TWEETS_ALL_FILES_AUTHOR_USERS_ONLY(31), FIND_TWEETS_WHITELISTED_FILES_AUTHOR_USERS_ONLY(32),

  FIND_TWEETS_ALL_FILES_AUTHOR_USERS_AND_FRIENDS(41), FIND_TWEETS_WHITELISTED_FILES_AUTHOR_USERS_AND_FRIENDS(42),

  FIND_TWEETS_ALL_FILES_ALL_USERS(51), FIND_TWEETS_WHITELISTED_FILES_ALL_USERS(52),

  ANALYZE_ALL_FILES_ALL_USERS(91), ANALYZE_WHITELISTED_FILES_ALL_USERS(92);

  private static final Set<OperationMode> ANALYZING_MODES = EnumSet.of(ANALYZE_ALL_FILES_ALL_USERS,
      ANALYZE_WHITELISTED_FILES_ALL_USERS);

  private static final Set<OperationMode> FIND_TWEETS_MODES = EnumSet.of(FIND_TWEETS_ALL_FILES_AUTHOR_USERS_ONLY,
      FIND_TWEETS_WHITELISTED_FILES_AUTHOR_USERS_ONLY, FIND_TWEETS_ALL_FILES_AUTHOR_USERS_AND_FRIENDS,
      FIND_TWEETS_WHITELISTED_FILES_AUTHOR_USERS_AND_FRIENDS, FIND_TWEETS_ALL_FILES_ALL_USERS,
      FIND_TWEETS_WHITELISTED_FILES_ALL_USERS);

  private static final Set<OperationMode> WHITELISTING_MODES = EnumSet.of(
      FIND_TWEETS_WHITELISTED_FILES_AUTHOR_USERS_ONLY, FIND_TWEETS_WHITELISTED_FILES_AUTHOR_USERS_AND_FRIENDS,
      FIND_TWEETS_WHITELISTED_FILES_ALL_USERS, ANALYZE_WHITELISTED_FILES_ALL_USERS);

  private static final Set<OperationMode> INCLUDE_FRIENDS_MODES = EnumSet.of(
      FIND_TWEETS_ALL_FILES_AUTHOR_USERS_AND_FRIENDS, FIND_TWEETS_WHITELISTED_FILES_AUTHOR_USERS_AND_FRIENDS,
      FIND_TWEETS_ALL_FILES_ALL_USERS, FIND_TWEETS_WHITELISTED_FILES_ALL_USERS, ANALYZE_ALL_FILES_ALL_USERS,
      ANALYZE_WHITELISTED_FILES_ALL_USERS);

  private static final Set<OperationMode> INCLUDE_FOLLOWERS_MODES = EnumSet.of(FIND_TWEETS_ALL_FILES_ALL_USERS,
      FIND_TWEETS_WHITELISTED_FILES_ALL_USERS, ANALYZE_ALL_FILES_ALL_USERS, ANALYZE_WHITELISTED_FILES_ALL_USERS);

  private final int choice;

  private OperationMode(final int choice) {
    this.choice = choice;
  }

  public static OperationMode getByChoice(final int choice) {
    for (final OperationMode mode : OperationMode.values()) {
      if (mode.choice == choice) {
        return mode;
      }
    }

    return null;
  }

  public boolean isAnalyzingMode() {
    return ANALYZING_MODES.contains(this);
  }

  public boolean isFindTweetsMode() {
    return FIND_TWEETS_MODES.contains(this);
  }

  public boolean isWhiteListingMode() {
    return WHITELISTING_MODES.contains(this);
  }

  public boolean isIncludeFriendsMode() {
    return INCLUDE_FRIENDS_MODES.contains(this);
  }

  public boolean isIncludeFollowersMode() {
    return INCLUDE_FOLLOWERS_MODES.contains(this);
  }

}
