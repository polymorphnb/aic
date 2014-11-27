package ac.at.tuwien.tdm.userdb;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.h2.store.fs.FileUtils;
import org.h2.tools.RunScript;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ac.at.tuwien.tdm.twitter.connector.api.User;

public class UserDBConnector {

  private Connection conn;
  private static final String PATH_TO_DB = "./userDB/users";
  private static final String PATH_TO_TABLE = "userTable.sql";
  private static final String INSERT_USER = "INSERT INTO  twitterUsers (screenName, name, location, statusesCount, followersCount, language, friendsCount, friendsUserIds, FollowerUserIds) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

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
  public void insertUser(Long id, String screenName, String name, String location, int statusesCount, int followers, String language, int friendsCount, String friendsUserIds, String followerUserIds) {
    try {
      PreparedStatement p = this.conn.prepareStatement(INSERT_USER);
//      p.setLong(1, id);
      p.setString(1, screenName);
      p.setString(2, name);
      p.setString(3, location);
      p.setInt(4, statusesCount);
      p.setInt(5, followers);
      p.setString(6, language);
      p.setInt(7, friendsCount);
      p.setString(8, friendsUserIds.replace("[", "").replace("]", ""));
      p.setString(9, followerUserIds.replace("[", "").replace("]", ""));

      p.execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }

  }

  public void getUsers() {
    String query = "select * from twitterUsers";
    try {
      Statement stmt = this.conn.createStatement();

      ResultSet rs = stmt.executeQuery(query);
      while (rs.next()) {
    	  
    	  Long id = rs.getLong("id");
    	  String screenName = rs.getString("screenName");
    	  String name = rs.getString("name");
    	  String location = rs.getString("location");
    	  String language = rs.getString("language");
    	  Integer statusesCount = rs.getInt("statusesCount");
    	  Integer followersCount = rs.getInt("followersCount");
    	  Integer friendsCount = rs.getInt("friendsCount");
    	  Integer favoritesCount = rs.getInt("favoritesCount");
    	  String followerUserIds = rs.getString("followerUserIds");
    	  String friendsUserIds = rs.getString("friendsUserIds");
    	  
    	  List<Long> followerList = new ArrayList<Long>();
    	  List<Long> friendsList = new ArrayList<Long>();
    	  
    	  String[] splitFollower = followerUserIds.split(",");
    	  String[] splitFriends = friendsUserIds.split(",");
    	  
    	  for (String string : splitFriends) {
    		  if(!string.trim().isEmpty()){
    			  friendsList.add(Long.parseLong(string));
    		  }
    	  }
    	  for (String string : splitFollower) {
    		  if(!string.trim().isEmpty()){
    			  followerList.add(Long.parseLong(string));
    		  }
      	  }
    	  
    	  User user = new User(id, screenName, name, location, language, statusesCount, favoritesCount, followersCount, friendsCount);
    	  user.addFollowerUserIds(followerList);
    	  user.addFriendsUserIds(friendsList);
    	  
        System.out.println(user);
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  public void disconnect() {
    try {
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) throws IOException, JSONException {
    UserDBConnector connector = new UserDBConnector();
    connector.connect();
    connector.createUserTable();
    
    Long userId = 1L;
    
    File folder = new File("./src/main/resources/user");
    for (File file : folder.listFiles()) {
		if(file.isFile()){
			String readFileToString = org.apache.commons.io.FileUtils.readFileToString(file);
			String[] users = readFileToString.split("]}");
			for (int i = 0; i < users.length-1; i++) { //users.length()
				  JSONObject user = new JSONObject(users[i]+"]}");
				  connector.insertUser(userId, user.getString("screenName"), user.getString("name"), user.getString("location"), Integer.parseInt(user.getString("statusesCount")), Integer.parseInt(user.getString("followersCount")), user.getString("language"), user.getInt("friendsCount"), user.getString("friendsUserIds"), user.getString("followerUserIds"));
				  userId++;
				}
		}
	}
    
    //connector.insertUser(1L, "Test", "test", "bla", 0, 0);
    connector.getUsers();
  }

}
