//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTabbedPane;
import javax.swing.JTable;
//import javax.swing.JComboBox;
//import javax.swing.JTextField;
import javax.swing.SwingUtilities;
//import javax.swing.table.DefaultTableModel;
//import java.sql.PreparedStatement;

//import java.awt.BorderLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InventoryManagement {
    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";  
    static final String PASS = "481jfortin1"; // plug in your password
    DecentBuyOrderData DBDB_OrderData = new DecentBuyOrderData();
    static DecentBuyFrame DBDBFrame = new DecentBuyFrame();
    
        public static void main(String[] args)
        {
            try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
                 Statement stmt = conn.createStatement()) {
    
                // create database
                String sql = "CREATE DATABASE IF NOT EXISTS mydatabase";
                stmt.executeUpdate(sql);
                System.out.println("Database created successfully...");
    
                Connection dbConn = DriverManager.getConnection(DB_URL + "mydatabase", USER, PASS);
    
                // create inventory table
                String createTableSQL = "CREATE TABLE IF NOT EXISTS Inventory "
                        + "(id INT NOT NULL AUTO_INCREMENT, "
                        + " item_name VARCHAR(255), "
                        + " category VARCHAR(255), "
                        + " quantity INT, "
                        + " price DECIMAL(10, 2), "
                        + " PRIMARY KEY (id))";
                dbConn.createStatement().executeUpdate(createTableSQL);
                System.out.println("Inventory table created successfully...");
    
                // create pending orders table
                String createPendingOrdersSQL = "CREATE TABLE IF NOT EXISTS PendingOrders "
                        + "(order_id INT NOT NULL AUTO_INCREMENT, "
                        + " first_name VARCHAR(45), "
                        + " last_name VARCHAR(45), "
                        + " item_name VARCHAR(45), "
                        + " quantity INT, "
                        + " price DECIMAL(10, 2), "
                        + " status VARCHAR(45) DEFAULT 'Pending', "
                        + " PRIMARY KEY (order_id))";
                dbConn.createStatement().executeUpdate(createPendingOrdersSQL);
                System.out.println("PendingOrders table created successfully...");
    
                // insert initial data if the table is empty
                String checkTableSQL = "SELECT COUNT(*) AS rowcount FROM Inventory";
                ResultSet rsCheck = dbConn.createStatement().executeQuery(checkTableSQL);
                rsCheck.next();
                int count = rsCheck.getInt("rowcount");
                if (count == 0) {
                    String insertSQL = "INSERT INTO Inventory (item_name, category, quantity, price) VALUES "
                            + "('computers', 'Electronics', 100, 1000.00), "
                            + "('iPads', 'Electronics', 15, 800.00), "
                            + "('monitors', 'Electronics', 20, 150.00), "
                            + "('laptops', 'Electronics', 8, 1200.00), "
                            + "('keyboards', 'Accessories', 30, 20.00), "
                            + "('mice', 'Accessories', 40, 15.00)";
                    dbConn.createStatement().executeUpdate(insertSQL);
                    System.out.println("Initial data inserted successfully...");
                } else {
                    System.out.println("Data already exists, skipping initial insertion...");
                }
    
                // GUI
                SwingUtilities.invokeLater(() -> createAndShowGUI(dbConn));
                
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                
                    private static void createAndShowGUI(Connection dbConn) {
            JTable table = new JTable();
            DBDBFrame.dbFrame(dbConn);
            DBDBFrame.createTabbedPane(dbConn);
            DBDBFrame.createCanceledOrdersPanel(dbConn);
            DBDBFrame.createInventoryPanel(dbConn);
            DBDBFrame.createInventoryButtonPanel(dbConn, table);
            DBDBFrame.createSearchPanel(dbConn, table);
            DBDBFrame.createCompletedOrdersPanel(dbConn);
    }
}
