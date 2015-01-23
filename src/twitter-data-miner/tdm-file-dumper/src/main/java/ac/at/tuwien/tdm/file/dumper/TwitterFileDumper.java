package ac.at.tuwien.tdm.file.dumper;

import ac.at.tuwien.tdm.commons.Clock;
import ac.at.tuwien.tdm.commons.GsonInstance;
import ac.at.tuwien.tdm.commons.Maybe;
import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.file.dumper.pipeline.FindTweetsJob;
import ac.at.tuwien.tdm.file.dumper.pipeline.Pipeline;
import ac.at.tuwien.tdm.file.dumper.writer.SearchTweetFileWriter;
import ac.at.tuwien.tdm.file.dumper.writer.TweetsForUserFileWriter;
import ac.at.tuwien.tdm.file.dumper.writer.UserFileWriter;
import ac.at.tuwien.tdm.twitter.connector.TwitterConnectorConstants;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnector;
import ac.at.tuwien.tdm.twitter.connector.api.TwitterConnectorFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Collects tweets and users related to topics defined by topics.txt
 * 
 * @author Irnes Okic (irnes.okic@student.tuwien.ac.at)
 * 
 */
public final class TwitterFileDumper {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFileDumper.class);

  private static ExecutorService executorService;

  private final Properties config;

  private TwitterFileDumper(final int amountWorkerThreads, final Properties config) {
    executorService = Executors.newFixedThreadPool(amountWorkerThreads);
    this.config = config;
  }

  public static void main(final String[] args) {

    OperationMode mode = null;
    Properties config = null;

    // Config is not needed anymore
//    String userFolder = null;
//    try {
//      config = readConfig();
//      userFolder = config.getProperty(FileDumperConstants.USER_FILE_FOLDER_KEY);
//    } catch (final Exception e) {
//      LOGGER.error("No configuratin File found, using default");
//    }
    
    mode = OperationMode.getByChoice(FileDumperConstants.GET_TWEETS_FOR_SEARCHTERMS_MODE);

    final long startUpTime = Clock.currentTime().getTimeInMillis();

    TwitterFileDumper fileDumper = null;

    try {
      final int amountWorkerThreads = FileDumperConstants.AMOUNT_OF_WORKER_THREADS;
      LOGGER.info(String.format("Using %d worker thread(s)", amountWorkerThreads));

      fileDumper = new TwitterFileDumper(amountWorkerThreads, config);
      fileDumper.execute(mode);

    } catch (final Exception e) {
      LOGGER.error("Unexpected exception", e);
    } finally {
      executorService.shutdownNow();

      if (fileDumper != null) {
        fileDumper.cleanUpResources();
      }
    }

    final long durationInMilliSeconds = (Clock.currentTime().getTimeInMillis() - startUpTime);

    switch (mode) {
      case LOOK_UP_SEARCH_TERMS:
        final long amountOfFoundTweets = SearchTweetFileWriter.getInstance().getTotalAmountOfEntries();
        final long amountOfFoundUsers = UserFileWriter.getInstance().getTotalAmountOfEntries();

        LOGGER.info(String.format("Found %d tweets and %d users in %s", amountOfFoundTweets, amountOfFoundUsers,
            formatDuration(durationInMilliSeconds, false)));
        break;
      default:
        LOGGER.info(String.format("Executed in %s", formatDuration(durationInMilliSeconds, false)));
        break;
    }
  }

  @SuppressWarnings("unused")
  private static Properties readConfig() throws IOException {
    final Properties config = new Properties();
    config.load(ClassLoader.getSystemResourceAsStream(FileDumperConstants.CONFIG_PROPERTIES_FILE_NAME));

    return config;
  }

  @SuppressWarnings("unused")
  private static OperationMode readUserDecision(final String folderPath) {

    // System.out.println("Used folder: " + folderPath);
    System.out.println("Choose operation mode:");

    System.out.println(" 11) Search tweets for given search terms");

    System.out.println(" 31) Look up tweets - ALL files - ONLY author users");
    System.out.println(" 32) Look up tweets - WHITELISTED files - ONLY author users");

    System.out.println(" 41) Look up tweets - ALL files - author users AND friends");
    System.out.println(" 42) Look up tweets - WHITELISTED files - author users AND friends");

    System.out.println(" 51) Look up tweets - ALL files - ALL users");
    System.out.println(" 52) Look up tweets - WHITELISTED files - ALL users");

    System.out.println(" 91) Analyze users - ALL files - ALL users");
    System.out.println(" 92) Analyze users - WHITELISTED files - ALL users");

    OperationMode mode = null;
    final Scanner scanner = new Scanner(System.in);

    while (scanner.hasNextLine()) {

      final String line = scanner.nextLine();

      try {
        final Integer choice = Integer.valueOf(line);
        mode = OperationMode.getByChoice(choice);

        if (mode == null) {
          System.out.println("Invalid input");
          continue;
        } else {
          break;
        }
      } catch (final NumberFormatException e) {
        System.out.println("Invalid input");
        continue;
      }
    }

    scanner.close();

    System.out.println("");
    return mode;
  }

  private void execute(final OperationMode mode) throws InterruptedException {
    if (mode.isFindTweetsMode()) {
      findTweets(mode);
    } else if (mode.isAnalyzingMode()) {
      analyzeUsers(mode);
    } else {
      lookUpSearchTerms();
    }
  }

  private void lookUpSearchTerms() throws InterruptedException {
    final TwitterConnector connector = TwitterConnectorFactory.createTwitterConnector();

    final List<TweetSearchTerm> searchTerms = readSearchTermsFromDefaultFile();

    if (searchTerms.isEmpty()) {
      LOGGER.info("No search terms found");
      return;
    }

    final CountDownLatch latch = new CountDownLatch(searchTerms.size());

    for (final TweetSearchTerm searchTerm : searchTerms) {
      final Pipeline pipeline = Pipeline.newInstance(connector, latch, searchTerm);
      executorService.submit(pipeline);
    }

    latch.await();
    connector.shutdownService();
  }

  private void findTweets(final OperationMode mode) throws InterruptedException {
    final TwitterConnector connector = TwitterConnectorFactory.createTwitterConnector();

    final Collection<File> files = retrieveUserFilesToLookUp(mode);

    if (files.isEmpty()) {
      return;
    }

    final CountDownLatch latch = new CountDownLatch(files.size());

    for (final File userFile : files) {
      final Runnable runnable = new FindTweetsJob(connector, latch, userFile, mode.isIncludeFriendsMode(),
          mode.isIncludeFollowersMode());
      executorService.submit(runnable);
    }

    latch.await();
    connector.shutdownService();
  }

  private void analyzeUsers(final OperationMode mode) {
    final Collection<File> files = retrieveUserFilesToLookUp(mode);

    if (files.isEmpty()) {
      return;
    }

    System.out.println("");
    final List<UserCountResult> countResults = new ArrayList<UserCountResult>(Integer.highestOneBit(files.size()) * 2);

    for (final File userFile : files) {
      final Maybe<UserCountResult> mbResult = analyzeUserFile(userFile);

      if (mbResult.isKnown()) {
        final UserCountResult result = mbResult.value();
        printCountResult(userFile.getName(), result);
        countResults.add(result);
      }
    }

    final UserCountResult totalResult = calculateTotalAmountOfUsers(countResults);
    printCountResult("All files", totalResult);
  }

  private Maybe<UserCountResult> analyzeUserFile(final File userFile) {
    Maybe<UserCountResult> mbResult = Maybe.unknown(UserCountResult.class);
    final Collection<Long> authorUserIds = new HashSet<Long>();
    Collection<Long> friendsUserIds = new HashSet<Long>();
    Collection<Long> followersUserIds = new HashSet<Long>();

    InputStream in = null;

    try {
      in = FileUtils.openInputStream(userFile);
      final LineIterator it = IOUtils.lineIterator(in, FileDumperConstants.ENCODING);
      final Gson gson = GsonInstance.get();

      while (it.hasNext()) {
        final User user = gson.fromJson(it.next(), User.class);

        authorUserIds.add(user.getId());

        for (final Long friendId : user.getFriendsUserIds()) {
          friendsUserIds.add(friendId);
        }

        for (final Long followerId : user.getFollowerUserIds()) {
          followersUserIds.add(followerId);
        }
      }

      friendsUserIds = CollectionUtils.subtract(friendsUserIds, authorUserIds);
      followersUserIds = CollectionUtils.subtract(CollectionUtils.subtract(followersUserIds, friendsUserIds),
          authorUserIds);

      mbResult = Maybe.definitely(new UserCountResult(authorUserIds.size(), friendsUserIds.size(), followersUserIds
          .size()));

    } catch (final IOException e) {
      LOGGER.error(String.format("Couldn't analzye file \"%s\"", userFile.getName()), e);
    } finally {
      if (in != null) {
        try {
          in.close();
        } catch (final IOException e) {
          LOGGER.error(String.format("Couldn't close stream of file \"%s\"", userFile.getName()), e);
        }
      }
    }

    return mbResult;
  }

  private void printCountResult(final String resultDescription, final UserCountResult result) {
    System.out.println("Result for: " + resultDescription);
    System.out.println(String.format(
        "Assumption: %d credentials, 300 requests / time window, max. %d tweets per user, time window of %d minutes",
        FileDumperConstants.ASSUMPTION_AMOUNT_CREDENTIALS,
        TwitterConnectorConstants.DEFAULT_MAX_TWEETS_PER_USER_SEARCH,
        FileDumperConstants.ASSUMPTION_TIME_WINDOW_IN_MINUTES));

    final String durationAuthorUsersOnly = (formatDuration(calculateDurationOfRequests(result.getAmountAuthorUsers()), true));
    final String durationAuthorUsersAndFriends = (formatDuration(calculateDurationOfRequests(result
        .getAmountAuthorUsers() + result.getAmountFriends()), true));
    final String durationAllUsers = (formatDuration(calculateDurationOfRequests(result.getTotalAmountOfUsers()), true));

    System.out.println(String.format("Author users only { amount: %s, max. time: %s }", result.getAmountAuthorUsers(),
        durationAuthorUsersOnly));
    System.out.println(String.format("Author users and friends { amount: %s, max. time: %s }",
        (result.getAmountAuthorUsers() + result.getAmountFriends()), durationAuthorUsersAndFriends));
    System.out.println(String.format("All users { amount: %s, max. time: %s }", result.getTotalAmountOfUsers(),
        durationAllUsers));
    System.out.println("");
  }

  private UserCountResult calculateTotalAmountOfUsers(final List<UserCountResult> userCounts) {
    long amountOfAuthorUsers = 0;
    long amountOfFriends = 0;
    long amountOfFollowers = 0;

    for (final UserCountResult userCount : userCounts) {
      amountOfAuthorUsers += userCount.getAmountAuthorUsers();
      amountOfFriends += userCount.getAmountFriends();
      amountOfFollowers += userCount.getAmountFollowers();
    }

    return new UserCountResult(amountOfAuthorUsers, amountOfFriends, amountOfFollowers);
  }

  private long calculateDurationOfRequests(final long amountOfUsers) {
    final int requestsPerWindow = (300 * FileDumperConstants.ASSUMPTION_AMOUNT_CREDENTIALS);
    final int maxRequestsPerUser = (TwitterConnectorConstants.DEFAULT_MAX_TWEETS_PER_USER_SEARCH / 200);

    final int amountOfNeededTimeWindows = (int) ((amountOfUsers * maxRequestsPerUser) / requestsPerWindow);

    return (amountOfNeededTimeWindows * FileDumperConstants.ASSUMPTION_TIME_WINDOW_IN_MINUTES * FileDumperConstants.MINUTE_IN_MILLISECONDS);
  }

  private static String formatDuration(final long durationInMilliseconds, final boolean addMinRequestValueIfZero) {
    final int days = (int) (durationInMilliseconds / FileDumperConstants.DAY_IN_MILLISECONDS);
    final int hours = (int) ((durationInMilliseconds - (days * FileDumperConstants.DAY_IN_MILLISECONDS)) / FileDumperConstants.HOUR_IN_MILLISECONDS);
    int minutes = (int) ((durationInMilliseconds - (days * FileDumperConstants.DAY_IN_MILLISECONDS) - (hours * FileDumperConstants.HOUR_IN_MILLISECONDS)) / FileDumperConstants.MINUTE_IN_MILLISECONDS);
    int seconds = (int) ((durationInMilliseconds - (days * FileDumperConstants.DAY_IN_MILLISECONDS)
        - (hours * FileDumperConstants.HOUR_IN_MILLISECONDS) - (minutes * FileDumperConstants.MINUTE_IN_MILLISECONDS)) / FileDumperConstants.SECOND_IN_MILLISECONDS);

    if (days == 0 && hours == 0 && minutes == 0 && seconds == 0) {
      if(addMinRequestValueIfZero){
        minutes = FileDumperConstants.ASSUMPTION_TIME_WINDOW_IN_MINUTES;
      }else{
        seconds = 1;
      }
    }

    final StringBuilder builder = new StringBuilder();
    addToBuilder(builder, "days", days, false);
    addToBuilder(builder, "hours", hours, false);
    addToBuilder(builder, "minutes", minutes, false);
    addToBuilder(builder, "seconds", seconds, true);

    return builder.toString();
  }

  private static void addToBuilder(final StringBuilder builder, final String valueName, final int value,
      final boolean lastEntry) {
    builder.append(value);
    builder.append(" ");
    builder.append(valueName);

    if (!lastEntry) {
      builder.append(", ");
    }
  }

  private Collection<File> retrieveUserFilesToLookUp(final OperationMode mode) {
    final String folderPath = config.getProperty(FileDumperConstants.USER_FILE_FOLDER_KEY).trim();

    if (folderPath.isEmpty()) {
      throw new IllegalArgumentException("No folder set in config.properties");
    }

    final File folder = new File(folderPath);

    if (!folder.isDirectory()) {
      throw new IllegalArgumentException(String.format("Path (%s) does not denote a folder", folderPath));
    }

    IOFileFilter fileFilter = null;

    if (mode.isWhiteListingMode()) {
      final List<String> whiteListedFiles = retrieveWhiteListedFiles();

      if (whiteListedFiles.isEmpty()) {
        throw new IllegalArgumentException("No whitelisted files found");
      }

      final List<IOFileFilter> filters = new ArrayList<IOFileFilter>();

      for (final String whiteListedFile : whiteListedFiles) {
        filters.add(FileFilterUtils.nameFileFilter(whiteListedFile));
      }

      fileFilter = FileFilterUtils.or(filters.toArray(new IOFileFilter[filters.size()]));
    } else {
      fileFilter = FileFilterUtils.prefixFileFilter(FileDumperConstants.USER_FILE_NAME_PREFIX);
    }

    fileFilter = FileFilterUtils.and(fileFilter, FileFilterUtils.fileFileFilter());

    final Collection<File> files = FileUtils.listFiles(folder, fileFilter, null);
    LOGGER.info(String.format("Found %d user files matching the criteria", files.size()));

    return files;
  }

  private List<String> retrieveWhiteListedFiles() {
    final InputStream resource = ClassLoader.getSystemResourceAsStream(FileDumperConstants.WHITELISTED_FILES_FILE_NAME);

    try {
      return IOUtils.readLines(resource, FileDumperConstants.ENCODING);
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }

  private List<TweetSearchTerm> readSearchTermsFromDefaultFile() {
    final List<TweetSearchTerm> searchTerms = new ArrayList<TweetSearchTerm>();
    InputStream resource = ClassLoader.getSystemResourceAsStream(FileDumperConstants.SEARCH_TERMS_FILE_NAME);
    try {
      if(resource == null) {
        resource = new FileInputStream("./" + FileDumperConstants.SEARCH_TERMS_FILE_NAME);
      }
    } catch (FileNotFoundException e) {
        LOGGER.error(
          String.format("Could not find searchterm file '%s'", FileDumperConstants.SEARCH_TERMS_FILE_NAME), e);
        throw new RuntimeException(e);
    }

    try {
      final List<String> readLines = IOUtils.readLines(resource, FileDumperConstants.ENCODING);

      for (final String readLine : readLines) {
        final String[] values = readLine.split(";");

        if (values.length != 2) {
          throw new IllegalArgumentException("Topics file is corrupt. Read line does not contain 2 values.");
        }

        final String searchTerm = values[0].trim();
        final boolean onlyHashTags = Boolean.parseBoolean(values[1].trim());

        searchTerms.add(new TweetSearchTerm(searchTerm, onlyHashTags));
      }
    } catch (final Exception e) {
      throw new RuntimeException(e);
    } finally {
      try {
        resource.close();
      } catch (IOException e) {
        LOGGER.error(
            String.format("File stream for file '%s' couldn't be closed", FileDumperConstants.SEARCH_TERMS_FILE_NAME),
            e);
      }
    }

    return searchTerms;
  }

  private void cleanUpResources() {
    SearchTweetFileWriter.getInstance().closeFileStream();
    UserFileWriter.getInstance().closeFileStream();
    TweetsForUserFileWriter.getInstance().closeFileStream();
  }
}
