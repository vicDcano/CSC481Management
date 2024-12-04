import java.sql.*;

public class DatabaseConn {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/decentbuy3";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "1234"; //changes this to your pass

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Error connecting to the database");
        }
    }
}
