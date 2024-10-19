import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import java.awt.BorderLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InventoryManagement {
    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";  
    static final String PASS = " "; //plug in your pw

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // creates db
            String sql = "CREATE DATABASE IF NOT EXISTS mydatabase";
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");

            Connection dbConn = DriverManager.getConnection(DB_URL + "mydatabase", USER, PASS);

            // creates inventory table with category
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Inventory "
                    + "(id INT NOT NULL AUTO_INCREMENT, "
                    + " item_name VARCHAR(255), "
                    + " category VARCHAR(255), "
                    + " quantity INT, "
                    + " price DECIMAL(10, 2), "
                    + " PRIMARY KEY (id))";
            dbConn.createStatement().executeUpdate(createTableSQL);
            System.out.println("Inventory table created successfully...");

            // creates pending orders table
            String createPendingOrdersSQL = "CREATE TABLE IF NOT EXISTS PendingOrders "
                    + "(order_id INT NOT NULL AUTO_INCREMENT, "
                    + " item_name VARCHAR(255), "
                    + " quantity INT, "
                    + " status VARCHAR(255) DEFAULT 'Pending', "
                    + " PRIMARY KEY (order_id))";
            dbConn.createStatement().executeUpdate(createPendingOrdersSQL);
            System.out.println("PendingOrders table created successfully...");

            // insert initial data if not already occupied
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
            SwingUtilities.invokeLater(() -> createAndShowGUI(dbConn));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createAndShowGUI(Connection dbConn) {
        JFrame frame = new JFrame("Inventory Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        // creating a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // inventory Management Tab
        JPanel inventoryPanel = new JPanel(new BorderLayout());

        // table for inventory data
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);

        // refresh inventory feature
        JButton refreshButton = new JButton("Refresh Inventory");
        refreshButton.addActionListener(e -> {
            try {
                loadInventoryData(dbConn, table);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);

        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        // load initial data
        try {
            loadInventoryData(dbConn, table);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // add inventory management panel to tab
        tabbedPane.addTab("Inventory", inventoryPanel);

        // pending Orders Tab
        JPanel ordersPanel = new JPanel(new BorderLayout());

        JTable ordersTable = new JTable();
        JScrollPane ordersScrollPane = new JScrollPane(ordersTable);
        ordersPanel.add(ordersScrollPane, BorderLayout.CENTER);

        JButton refreshOrdersButton = new JButton("Refresh Orders");
        refreshOrdersButton.addActionListener(e -> {
            try {
                loadPendingOrdersData(dbConn, ordersTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // panel for buttons
        JPanel ordersButtonPanel = new JPanel();
        ordersButtonPanel.add(refreshOrdersButton);

        ordersPanel.add(ordersButtonPanel, BorderLayout.SOUTH);

        // load initial orders data
        try {
            loadPendingOrdersData(dbConn, ordersTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // add orders management panel to tab
        tabbedPane.addTab("Pending Orders", ordersPanel);

        // Add tabbed pane to frame
        frame.add(tabbedPane);

        // frame visibility
        frame.setVisible(true);
    }

    private static void loadInventoryData(Connection dbConn, JTable table) throws SQLException {
        String selectSQL = "SELECT id, item_name, category, quantity, price FROM Inventory";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        // extract data from result set
        String[] columnNames = {"ID", "Item Name", "Category", "Quantity", "Price"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            int id = rs.getInt("id");
            String itemName = rs.getString("item_name");
            String category = rs.getString("category");
            int quantity = rs.getInt("quantity");
            double price = rs.getDouble("price");
            String formattedPrice = String.format("$%.2f", price);
            Object[] row = {id, itemName, category, quantity, formattedPrice};
            model.addRow(row);
        }

        // set model to table
        table.setModel(model);
    }

    private static void loadPendingOrdersData(Connection dbConn, JTable table) throws SQLException {
        String selectSQL = "SELECT order_id, item_name, quantity, status FROM PendingOrders";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        // extract data from result set
        String[] columnNames = {"Order ID", "Item Name", "Quantity", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            int orderId = rs.getInt("order_id");
            String itemName = rs.getString("item_name");
            int quantity = rs.getInt("quantity");
            String status = rs.getString("status");
            Object[] row = {orderId, itemName, quantity, status};
            model.addRow(row);
        }

        // set model to table
        table.setModel(model);
    }
}
