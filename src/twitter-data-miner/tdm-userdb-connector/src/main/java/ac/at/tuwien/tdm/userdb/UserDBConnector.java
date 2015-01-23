package ac.at.tuwien.tdm.userdb;

import ac.at.tuwien.tdm.commons.pojo.User;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.h2.tools.RunScript;

public class UserDBConnector {

  private Connection conn;
  private static final String PATH_TO_DB = "./userDB/users";
  private static final String PATH_TO_TABLE = "userTable.sql";
  private static final String INSERT_USER = "INSERT INTO  twitterUsers (userId, screenName, name, location, statusesCount, followersCount, language, favoritesCount, friendsCount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

  private static final UserDBConnector INSTANCE = new UserDBConnector();

  private UserDBConnector() {
    this.connect();
    this.createUserTable();
  }

  public static UserDBConnector getInstance() {
    return UserDBConnector.INSTANCE;
  }

  public void connect() {
    try {
      this.conn = DriverManager.getConnection("jdbc:h2:" + PATH_TO_DB, "sa", "");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void createUserTable() {
    try {
      RunScript.execute(conn,
          new InputStreamReader(this.getClass().getResourceAsStream("/" + UserDBConnector.PATH_TO_TABLE)));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void insertUser(User user) {
    if (this.getUser(user.getId()) == null) {
      this.insertUser(user.getId(), user.getScreenName(), user.getName(), user.getLocation(), user.getStatusesCount(),
          user.getFollowersCount(), user.getLanguage(), user.getFavoritesCount(), user.getFriendsCount());
    }

  }

  public void insertUser(Long userId, String screenName, String name, String location, int statusesCount,
      int followersCount, String language, int favoritesCount, int friendsCount) {
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

      p.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
  
  public int calcInfluence(Long userID, int followersWeight, int retweetsWeight, int favouritesWeight) {
	  User u = this.getUser(userID);
	  return u.getFollowersCount() * followersWeight + u.getRetweetsCount() * retweetsWeight + u.getFavoritesCount() * favouritesWeight;
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
      // TODO Auto-generated catch block
      e.printStackTrace();
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
      // TODO Auto-generated catch block
      e.printStackTrace();
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
      // TODO Auto-generated catch block
      e.printStackTrace();
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

      user = new User(userId, screenName, name, location, language, statusesCount, favoritesCount, followersCount,
          friendsCount, retweetsCount);
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return user;
  }

  public void disconnect() {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
