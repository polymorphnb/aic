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

import ac.at.tuwien.tdm.commons.pojo.User;
import ac.at.tuwien.tdm.results.InfluenceResult;

public class UserDBConnector {

  private Connection conn;
  public static final String PATH_TO_DB_DEFAULT = "./userDB/users";
  private String pathToDB = UserDBConnector.PATH_TO_DB_DEFAULT;
  private String pathToTable = "userTable.sql";
  private static final String INSERT_USER = "INSERT INTO  twitterUsers (userId, screenName, name, location, statusesCount, followersCount, language, favoritesCount, friendsCount, retweetsCount) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

  public static final String PATH_USERDB_KEY = "userdb.path";
  //private static final UserDBConnector INSTANCE = new UserDBConnector();

  public UserDBConnector(String path, String pathToTable) throws FileNotFoundException {
	this.pathToTable = pathToTable;
	this.pathToDB = path;
    this.connect();
//    this.createUserTable();
  }
  
  public UserDBConnector(String path) throws FileNotFoundException {
		this.pathToDB = path;
	    this.connect();
//	    this.createUserTable();
	  }
  
  public UserDBConnector() throws FileNotFoundException {
	    this.connect();
//	    this.createUserTable();
	  }

  //public static UserDBConnector getInstance() {
  //  return UserDBConnector.INSTANCE;
  //}

  public void connect() {
    try {
      this.conn = DriverManager.getConnection("jdbc:h2:" + pathToDB, "sa", "");
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public void createUserTableTest() {
    try {
      RunScript.execute(conn,
      new InputStreamReader(this.getClass().getResourceAsStream("/tmp/userTable.sql")));
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
  
  public void createUserTable() throws FileNotFoundException {
	    try {
	      RunScript.execute(conn, new InputStreamReader(new FileInputStream(new File(pathToTable))));
	    } catch (SQLException e) {
	      e.printStackTrace();
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

      p.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }
  
  public int calcInfluence(Long userID, int followersWeight, int retweetsWeight, int favouritesWeight) {
	  User u = this.getUser(userID);
	  return u.getFollowersCount() * followersWeight + u.getRetweetsCount() * retweetsWeight + u.getFavoritesCount() * favouritesWeight;
  }
  
  public List<InfluenceResult> calcInfluenceAll(int followersWeight, int retweetsWeight, int favouritesWeight, int maxResults) {
	  String sum = "followersCount * " + followersWeight + " + favoritesCount * " + favouritesWeight + "+ retweetsCount *" + retweetsWeight;
	  String query = "SELECT screenName, followersCount, favoritesCount, retweetsCount, " + sum +" as influence_score";
	  query = query + " from twitterUsers order by " + sum + " desc";
	  
	  LinkedList<InfluenceResult> ret = new LinkedList<InfluenceResult>();
	  
	  int tmp = 0;
	  try {
	      Statement stmt = this.conn.createStatement();
	      ResultSet rs = stmt.executeQuery(query);
	      while (rs.next() && tmp < maxResults) {
	    	  tmp += 1;
	    	  ret.addLast(new InfluenceResult(
	    			     rs.getString("screenName"),
	    				 rs.getInt("followersCount"),
	    				 rs.getInt("favoritesCount"),
	    				 rs.getInt("retweetsCount"),
	    				 rs.getInt("influence_score")
	    			  ));
	      }
	    } catch (SQLException e) {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
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
  
  public static void main(String[] args) throws FileNotFoundException {
	  UserDBConnector db = new UserDBConnector("/tmp/userdb.h2");
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
