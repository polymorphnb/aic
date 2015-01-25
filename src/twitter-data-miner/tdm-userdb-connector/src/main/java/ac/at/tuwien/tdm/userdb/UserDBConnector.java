package ac.at.tuwien.tdm.userdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.h2.tools.RunScript;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.results.InfluenceResult;

public class UserDBConnector {
  
  private static final Logger LOGGER = LoggerFactory.getLogger(UserDBConnector.class);

  private Connection conn;
  private static final String DB_NAME = "users";
  public static final String PATH_TO_DB_DEFAULT = "./userDB/";
  public static final String PATH_TO_TABLE_DEFAULT = "userTable.sql";
  private String pathToDB = UserDBConnector.PATH_TO_DB_DEFAULT;
  private String pathToTable = UserDBConnector.PATH_TO_TABLE_DEFAULT;
  private static final String INSERT_USER = "INSERT INTO  twitterUsers (userId, screenName, name, location, statusesCount, followersCount, language, favoritesCount, friendsCount, retweetsCount, collectedTweetsCount, favoritedCount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String PATH_USERDB_KEY = "userdb.path";
  public static final String PATH_USERTABLE_KEY = "userdb.table";
  //private static final UserDBConnector INSTANCE = new UserDBConnector();

  public UserDBConnector(String path, String pathToTable, boolean createTable) {
    this.pathToTable = pathToTable;
    this.pathToDB = path;
    this.connect();
    if(createTable == true) {
      this.createUserTable();
    }
  }
  
  public UserDBConnector(String path, boolean createTable) {
		  this.pathToDB = path;
	    this.connect();
	    if(createTable == true) {
	      this.createUserTable();
      }
	  }
  
  public UserDBConnector(boolean createTable) {
	    this.connect();
	    if(createTable == true) {
	      this.createUserTable();
	    }
	  }

  //public static UserDBConnector getInstance() {
  //  return UserDBConnector.INSTANCE;
  //}

  public void connect() {
    if(this.pathToDB.endsWith("/") == false) {
      this.pathToDB += "/";
    }
    final String fullDBPath = this.pathToDB + UserDBConnector.DB_NAME;
    try {
      Class.forName("org.h2.Driver");
      this.conn = DriverManager.getConnection("jdbc:h2:" + fullDBPath, "sa", "");
    } catch (Exception e) {
      LOGGER.error(String.format("Could not connect to database '%s': " + e.getMessage(), fullDBPath));
    }
  }

  public void createUserTableTest() {
    try {
      RunScript.execute(conn,
      new InputStreamReader(this.getClass().getResourceAsStream("/tmp/userTable.sql")));
    } catch (SQLException e) {
      LOGGER.error(String.format("Could not connect to database '%s': " + e.getMessage(), "/tmp/userTable.sql"));
    }
  }
  
  public void createUserTable() {;
    try {
      RunScript.execute(conn, new InputStreamReader(this.getClass().getResourceAsStream("/" + pathToTable)));
      return;
    } catch (SQLException e) {
      LOGGER.info(String.format("Could not find UserTable '%s' as resource, trying filesystem", "/" + pathToTable));
    }
    
	  try {
        RunScript.execute(conn, new InputStreamReader(new FileInputStream(new File(pathToTable))));
	    } catch (SQLException e) {
	      LOGGER.error(String.format("Error creating UserTable '%s'", pathToTable));
      } catch (FileNotFoundException e) {
        LOGGER.error(String.format("Error creating UserTable '%s'. Could not find table file.", pathToTable));
      }

	  }

  public void insertUser(User user) {
    if (this.getUser(user.getId()) == null) {
      this.insertUser(user.getId(), user.getScreenName(), user.getName(), user.getLocation(), user.getStatusesCount(),
          user.getFollowersCount(), user.getLanguage(), user.getFavoritesCount(), user.getFriendsCount(), user.getRetweetsCount());
    }

  }

  public void insertUser(Long userId, String screenName, String name, String location, int statusesCount,
      int followersCount, String language, int favoritesCount, int friendsCount, int retweetsCount) {
    try {
      PreparedStatement p = this.conn.prepareStatement(INSERT_USER);
      p.setLong(1, userId);
      p.setString(2, screenName);
      p.setString(3, name);
      p.setString(4, location);
      p.setInt(5, statusesCount);
      p.setInt(6, followersCount);
      p.setString(7, language);
      p.setInt(8, favoritesCount);
      p.setInt(9, friendsCount);
      p.setInt(10, retweetsCount);
      p.setInt(11, 0);
      p.setInt(12, 0);
      
      p.execute();
    } catch (SQLException e) {
      LOGGER.error("Could not insert user into UserDB: " + e.getMessage());
    }

  }
  
  public int calcInfluence(Long userID, int followersWeight, int retweetsWeight, int favouritesWeight) {
	  User u = this.getUser(userID);
	  return u.getFollowersCount() * followersWeight + u.getRetweetsCount() * retweetsWeight + u.getFavoritedCount() * favouritesWeight;
  }
  
  public List<InfluenceResult> calcInfluenceAll(int followersWeight, int retweetsWeight, int favouritesWeight, int maxResults) {
	  String sum = "followersCount * " + followersWeight + " + favoritedCount * " + favouritesWeight + " + retweetsCount * " + retweetsWeight;
	  String query = "SELECT userId, screenName, followersCount, favoritedCount, retweetsCount, " + sum +" as influence_score";
	  query = query + " from twitterUsers order by " + sum + " desc";
	  
	  LinkedList<InfluenceResult> ret = new LinkedList<InfluenceResult>();
	  
	  int tmp = 0;
	  try {
	      Statement stmt = this.conn.createStatement();
	      ResultSet rs = stmt.executeQuery(query);
	      while (rs.next() && tmp < maxResults) {
	    	  tmp += 1;
	    	  ret.addLast(new InfluenceResult(
	    			  	 rs.getString("userId"),
	    			     rs.getString("screenName"),
	    				 rs.getInt("followersCount"),
	    				 rs.getInt("favoritedCount"),
	    				 rs.getInt("retweetsCount"),
	    				 rs.getInt("influence_score")
	    			  ));
	      }
	    } catch (SQLException e) {
	      LOGGER.error("Could not execute Query: " + e.getMessage());
	    }
	   return ret;
  }

  public User getUser(Long userID) {
    String query = "SELECT * from twitterUsers where userId = " + userID;
    User user = null;
    try {
      Statement stmt = this.conn.createStatement();

      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
        user = this.getUserFromResultSet(rs);
      }
    } catch (SQLException e) {
      LOGGER.error(String.format("Could not retrieve User '%s': " + e.getMessage(), userID));
    }
    return user;
  }

  public List<User> getUsers() {
    String query = "select * from twitterUsers";
    List<User> users = new ArrayList<User>();
    try {
      Statement stmt = this.conn.createStatement();

      ResultSet rs = stmt.executeQuery(query);
      
      while (rs.next()) {
        User user = this.getUserFromResultSet(rs);
        users.add(user);
      }
    } catch (SQLException e) {
      LOGGER.error("Couldn't not process users: " + e.getMessage());
    }
    return users;
  }
  
  public void dropTableTwitterUsers() {
    String query = "DROP TABLE twitterUsers;";
    Statement stmt;
    try {
      stmt = this.conn.createStatement();
      stmt.executeUpdate(query);
    } catch (SQLException e) {
      LOGGER.error("Couldn't not drop userTable twitterUsers");
    }
  }
  
  public void updateRetweetCountForUser(Long userID, int retweetCount) {
    String updateString = "update twitterUsers set retweetsCount = retweetsCount + ? where userID = ?";
    try {
      PreparedStatement updateRetweet = this.conn.prepareStatement(updateString);
      updateRetweet.setInt(1, retweetCount);
      updateRetweet.setLong(2, userID);
      updateRetweet.execute();
      this.conn.commit();
    } catch (SQLException ex) {
      LOGGER.error(String.format("Couldn't update retweet count for user '%s'", userID));
    }
  }
  
  public void updateTweetCountForUser(Long userID) {
    String updateString = "update twitterUsers set collectedTweetsCount = collectedTweetsCount + 1 where userID = ?";
    try {
      PreparedStatement updateTweetCount = this.conn.prepareStatement(updateString);
      updateTweetCount.setLong(1, userID);
      updateTweetCount.execute();
      this.conn.commit();
    } catch (SQLException ex) {
      LOGGER.error(String.format("Couldn't update retweet count for user '%s'", userID));
    }
  }
  
  public void updateFavoritedCountForUser(Long userID, int favoritedCount) {
    String updateString = "update twitterUsers set favoritedCount = favoritedCount + ? where userID = ?";
    try {
      PreparedStatement updateFavoritedCount = this.conn.prepareStatement(updateString);
      updateFavoritedCount.setInt(1, favoritedCount);
      updateFavoritedCount.setLong(2, userID);
      updateFavoritedCount.execute();
      this.conn.commit();
    } catch (SQLException ex) {
      LOGGER.error(String.format("Couldn't update retweet count for user '%s'", userID));
    }
  }
  
  public void updateFavoritedCountTweetCountRetweetCountForUser(Long userID, int retweetCount, int favoritedCount) {
    String updateString = "update twitterUsers set favoritedCount = favoritedCount + ?, collectedTweetsCount = collectedTweetsCount + 1, retweetsCount = retweetsCount + ? where userID = ?";
    try {
      PreparedStatement updateUser = this.conn.prepareStatement(updateString);
      updateUser.setInt(1, favoritedCount);
      updateUser.setInt(2, retweetCount);
      updateUser.setLong(3, userID);
      updateUser.execute();
      this.conn.commit();
    } catch (SQLException ex) {
      LOGGER.error(String.format("Couldn't update retweet count for user '%s'", userID));
    }
  }

  private User getUserFromResultSet(ResultSet rs) {
    User user = null;
    try {
      Long userId = rs.getLong("userId");

      String screenName = rs.getString("screenName");
      String name = rs.getString("name");
      String location = rs.getString("location");
      String language = rs.getString("language");
      Integer statusesCount = rs.getInt("statusesCount");
      Integer followersCount = rs.getInt("followersCount");
      Integer friendsCount = rs.getInt("friendsCount");
      Integer favoritesCount = rs.getInt("favoritesCount");
      Integer retweetsCount = rs.getInt("retweetsCount");
      Integer collectedTweetsCount = rs.getInt("collectedTweetsCount");
      Integer favoritedCount = rs.getInt("favoritedCount");

      user = new User(userId, screenName, name, location, language, statusesCount, favoritesCount, followersCount,
          friendsCount, retweetsCount, collectedTweetsCount, favoritedCount);
    } catch (SQLException e) {
      LOGGER.error("Couldn't get User from ResultSet: " + e.getMessage());
    }
    return user;
  }

  public void disconnect() {
    try {
      conn.close();
    } catch (SQLException e) {
      LOGGER.error("Could not disconnect from database: " + e.getMessage());
    }
  }
  
  public static void main(String[] args) throws FileNotFoundException {
	  UserDBConnector db = new UserDBConnector("/tmp/userdb", true);
	  db.connect();
	  db.dropTableTwitterUsers();
	  db.createUserTable();
	  db.insertUser(new Long(1), "1", "", "", 1, 1, "", 1, 1, 1);
	  db.insertUser(new Long(2), "2", "", "", 2, 2, "", 2, 2, 2);
	  db.insertUser(new Long(3), "3", "", "", 3, 3, "", 3, 3, 3);
	  List<InfluenceResult> ret = db.calcInfluenceAll(2, 2, 2,3);
	  System.out.println(ret.get(0).getScreenName());
	  db.disconnect();
  }
}
