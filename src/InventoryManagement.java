import javax.swing.*;
//import javax.swing.table.*;
//import java.awt.*;
//import java.sql.*;

public class InventoryManagement {
    DatabaseConn dbconn = new DatabaseConn();
    //DecentBuyFrame DBDBFrame = new DecentBuyFrame();
    public static void main(String[] args)
    {
        try {
            SwingUtilities.invokeLater(() -> new DBLogin());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

 /*
        try (Connection conn = dbconn.getConnection()) {
            // Statement stmt = conn.createStatement()) {

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
                        + "('computers', 'Electronics', 10, 1000.00), "
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
            SwingUtilities.invokeLater(() -> new DBLogin());

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void createAndShowGUI(Connection dbConn) {
        JTable table = new JTable();
        DBDBFrame.dbFrame(dbConn);
        DBDBFrame.createCanceledOrdersPanel(dbConn);
        DBDBFrame.createInventoryPanel(dbConn);
        DBDBFrame.createInventoryButtonPanel(dbConn, table);
        DBDBFrame.createSearchPanel(dbConn, table);
        DBDBFrame.createCompletedOrdersPanel(dbConn);
    }
        
}
*/
