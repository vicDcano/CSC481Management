import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.sql.PreparedStatement;

import java.awt.BorderLayout;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InventoryManagement {
    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";  
    static final String PASS = "1234567"; // plug in your password

    public static void main(String[] args) {
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

        // create a tabbed pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // inventory Management Tab
        JPanel inventoryPanel = new JPanel(new BorderLayout());

        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel();
        String[] searchOptions = {"Item Name", "Category"};  // Dropdown options
        JComboBox<String> searchDropdown = new JComboBox<>(searchOptions);  // Dropdown for search criteria
        JTextField searchTextField = new JTextField(15);  // Text field for search input
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> {
            try {
                searchBarInventory(dbConn, table, searchDropdown.getSelectedItem().toString(), searchTextField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        // Add components to search panel
        searchPanel.add(searchDropdown);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        inventoryPanel.add(searchPanel, BorderLayout.NORTH);

        JButton refreshButton = new JButton("Refresh Inventory");
        refreshButton.addActionListener(e -> {
            try {
                loadInventoryData(dbConn, table);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshButton);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        try {
            loadInventoryData(dbConn, table);
        } catch (SQLException e) {
            e.printStackTrace();
        }

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

        JButton generateOrderButton = new JButton("Generate Order");
        generateOrderButton.addActionListener(e -> {
            try {
                generateRandomOrder(dbConn);
                loadPendingOrdersData(dbConn, ordersTable);
                loadInventoryData(dbConn, table);  // refresh the inventory after order generation
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        JPanel ordersButtonPanel = new JPanel();
        ordersButtonPanel.add(refreshOrdersButton);
        ordersButtonPanel.add(generateOrderButton);
        ordersPanel.add(ordersButtonPanel, BorderLayout.SOUTH);

        try {
            loadPendingOrdersData(dbConn, ordersTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tabbedPane.addTab("Pending Orders", ordersPanel);

        // canceled Orders Tab
        JPanel canceledPanel = new JPanel(new BorderLayout());

        JTable canceledTable = new JTable();
        JScrollPane canceledScrollPane = new JScrollPane(canceledTable);
        canceledPanel.add(canceledScrollPane, BorderLayout.CENTER);

        JButton refreshCanceledButton = new JButton("Refresh Canceled Orders");
        refreshCanceledButton.addActionListener(e -> {
            try {
                loadCanceledOrdersData(dbConn, canceledTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        JPanel canceledButtonPanel = new JPanel();
        canceledButtonPanel.add(refreshCanceledButton);
        canceledPanel.add(canceledButtonPanel, BorderLayout.SOUTH);

        try {
            loadCanceledOrdersData(dbConn, canceledTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tabbedPane.addTab("Canceled Orders", canceledPanel);

        // completed Orders Tab
        JPanel completedPanel = new JPanel(new BorderLayout());

        JTable completedTable = new JTable();
        JScrollPane completedScrollPane = new JScrollPane(completedTable);
        completedPanel.add(completedScrollPane, BorderLayout.CENTER);

        JButton refreshCompletedButton = new JButton("Refresh Completed Orders");
        refreshCompletedButton.addActionListener(e -> {
            try {
                loadCompletedOrdersData(dbConn, completedTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        JPanel completedButtonPanel = new JPanel();
        completedButtonPanel.add(refreshCompletedButton);
        completedPanel.add(completedButtonPanel, BorderLayout.SOUTH);

        try {
            loadCompletedOrdersData(dbConn, completedTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tabbedPane.addTab("Completed Orders", completedPanel);

        // add tabbed pane to frame
        frame.add(tabbedPane);

        // display frame
        frame.setVisible(true);
    }

    private static void generateRandomOrder(Connection dbConn) throws SQLException {
        // select a random item from the inventory where quantity > 0
        String selectSQL = "SELECT id, item_name, quantity FROM Inventory WHERE quantity > 0 ORDER BY RAND() LIMIT 1";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        if (rs.next()) {
            int itemId = rs.getInt("id");
            String itemName = rs.getString("item_name");
            // reduce the quantity by 1
            String updateSQL = "UPDATE Inventory SET quantity = quantity - 1 WHERE id = " + itemId;
            dbConn.createStatement().executeUpdate(updateSQL);

            // insert the generated order into PendingOrders
            String insertOrderSQL = "INSERT INTO PendingOrders (item_name, quantity) VALUES ('" + itemName + "', 1)";
            dbConn.createStatement().executeUpdate(insertOrderSQL);

            System.out.println("Order for " + itemName + " has been generated.");
        } else {
            System.out.println("No items available in inventory to generate an order.");
        }
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
        String selectSQL = "SELECT order_id, first_name, last_name, item_name, quantity, price, status FROM PendingOrders";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        // extract data from result set
        String[] columnNames = {"Order ID", "First Name", "Last Name", "Item Name", "Quantity", "Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            int orderId = rs.getInt("order_id");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String itemName = rs.getString("item_name");
            int quantity = rs.getInt("quantity");
            double price = rs.getDouble("price");
            String formattedPrice = String.format("$%.2f", price);
            String status = rs.getString("status");
            Object[] row = {orderId,firstName, lastName, itemName, quantity, formattedPrice, status};
            model.addRow(row);
        }

        // set model to table
        table.setModel(model);
    }

    private static void loadCanceledOrdersData(Connection dbConn, JTable table) throws SQLException
    {
        String selectSQL = "SELECT order_id, first_name, last_name, item_name, quantity, price, status FROM CanceledOrders";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        // extract data from result set
        String[] columnNames = {"Order ID", "First Name", "Last Name", "Item Name", "Quantity", "Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            int orderId = rs.getInt("order_id");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String itemName = rs.getString("item_name");
            int quantity = rs.getInt("quantity");
            double price = rs.getDouble("price");
            String formattedPrice = String.format("$%.2f", price);
            String status = rs.getString("status");
            Object[] row = {orderId, firstName, lastName, itemName, quantity, formattedPrice, status};
            model.addRow(row);
        }

        // set model to table
        table.setModel(model);
    }

    private static void loadCompletedOrdersData(Connection dbConn, JTable table) throws SQLException
    {
        String selectSQL = "SELECT order_id, first_name, last_name, item_name, quantity, price, status FROM CompletedOrders";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        // extract data from result set
        String[] columnNames = {"Order ID", "First Name", "Last Name", "Item Name", "Quantity", "Price", "Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            int orderId = rs.getInt("order_id");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            String itemName = rs.getString("item_name");
            int quantity = rs.getInt("quantity");
            double price = rs.getDouble("price");
            String formattedPrice = String.format("$%.2f", price);
            String status = rs.getString("status");
            Object[] row = {orderId, firstName, lastName, itemName, quantity, formattedPrice, status};
            model.addRow(row);
        }

        // set model to table
        table.setModel(model);
    }

    private static void searchBarInventory(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException
    {
        String searchColumn = "item_name";  // Default search by item name

        // Map the search criterion to the correct column
        if (searchCriterion.equals("Category")) {
            searchColumn = "category";
        }

        // SQL query with parameterized search
        String searchSQL = "SELECT id, item_name, category, quantity, price FROM Inventory WHERE " + searchColumn + " LIKE ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(searchSQL)) {
            pstmt.setString(1, "%" + searchInput + "%");  // Use wildcards for partial matches
            ResultSet rs = pstmt.executeQuery();

            // Extract data and update the table model
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

            table.setModel(model);  // Update table with search results
        }
    }

    private static void searchBarCustomerOrder(Connection dbConn, JTable table) throws SQLException
    {

    }
}
