package ac.at.tuwien.tdm.userdb;

import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.h2.tools.RunScript;

public class UserDBConnector {

  private Connection conn;
  private static final String PATH_TO_DB = "./userDB/users";
  private static final String PATH_TO_TABLE = "userTable.sql";

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
  }

}
