package ac.at.tuwien.tdm.userdb;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.h2.tools.RunScript;

public class UserDBConnector {

  private Connection conn;
  private static final String PATH_TO_DB = "./userDB/users";
  private static final String PATH_TO_TABLE = "userTable.sql";
  private static final String INSERT_USER = "INSERT INTO twitterUsers VALUES (?, ?, ?, ?, ?, ?)";

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

  public void insertUser(Long id, String screenName, String name, String location, int statusesCount, int followers) {
    try {
      PreparedStatement p = this.conn.prepareStatement(INSERT_USER);
      p.setLong(1, id);
      p.setString(2, screenName);
      p.setString(3, name);
      p.setString(4, location);
      p.setInt(5, statusesCount);
      p.setInt(6, followers);

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
        System.out.println(rs);
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

  public static void main(String[] args) {
    UserDBConnector connector = new UserDBConnector();
    connector.connect();
    connector.createUserTable();
    connector.insertUser(1L, "Test", "test", "bla", 0, 0);
    connector.getUsers();
  }

}
