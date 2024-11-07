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
import java.awt.GridLayout;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class InventoryManagement {
    static final String DB_URL = "jdbc:mysql://localhost:3306/";
    static final String USER = "root";
    static final String PASS = "481jfortin1";

    public static void main(String[] args) {
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASS);
             Statement stmt = conn.createStatement()) {

            // Create database
            String sql = "CREATE DATABASE IF NOT EXISTS mydatabase";
            stmt.executeUpdate(sql);
            System.out.println("Database created successfully...");

            Connection dbConn = DriverManager.getConnection(DB_URL + "mydatabase", USER, PASS);

            // Create inventory table
            String createTableSQL = "CREATE TABLE IF NOT EXISTS Inventory "
                    + "(id INT NOT NULL AUTO_INCREMENT, "
                    + " item_name VARCHAR(255), "
                    + " category VARCHAR(255), "
                    + " quantity INT, "
                    + " price DECIMAL(10, 2), "
                    + " PRIMARY KEY (id))";
            dbConn.createStatement().executeUpdate(createTableSQL);
            System.out.println("Inventory table created successfully...");

            // Create pending orders table
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

            // GUI setup
            SwingUtilities.invokeLater(() -> createAndShowGUI(dbConn));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void createAndShowGUI(Connection dbConn) {
        JFrame frame = new JFrame("Inventory Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);

        JTabbedPane tabbedPane = new JTabbedPane();

        // Inventory Management Tab
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = new JPanel();
        String[] searchOptions = {"Item Name", "Category"};
        JComboBox<String> searchDropdown = new JComboBox<>(searchOptions);
        JTextField searchTextField = new JTextField(15);
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> {
            try {
                searchBarInventory(dbConn, table, searchDropdown.getSelectedItem().toString(), searchTextField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

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

        // Pending Orders Tab
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

        // Add Order Button to open the Add Order form
        JButton addOrderButton = new JButton("Add Order");
        addOrderButton.addActionListener(e -> openAddOrderTab(dbConn, tabbedPane, ordersTable));

        JPanel ordersButtonPanel = new JPanel();
        ordersButtonPanel.add(refreshOrdersButton);
        ordersButtonPanel.add(addOrderButton);
        ordersPanel.add(ordersButtonPanel, BorderLayout.SOUTH);

        try {
            loadPendingOrdersData(dbConn, ordersTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        tabbedPane.addTab("Pending Orders", ordersPanel);

        // Canceled Orders Tab
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

        // Completed Orders Tab
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

        frame.add(tabbedPane);
        frame.setVisible(true);
    }

    private static void openAddOrderTab(Connection dbConn, JTabbedPane tabbedPane, JTable ordersTable) {
        JPanel addOrderPanel = new JPanel(new GridLayout(6, 2, 5, 5));

        JTextField firstNameField = new JTextField();
        JTextField lastNameField = new JTextField();
        JTextField itemNameField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField priceField = new JTextField();

        addOrderPanel.add(new javax.swing.JLabel("First Name:"));
        addOrderPanel.add(firstNameField);
        addOrderPanel.add(new javax.swing.JLabel("Last Name:"));
        addOrderPanel.add(lastNameField);
        addOrderPanel.add(new javax.swing.JLabel("Item Name:"));
        addOrderPanel.add(itemNameField);
        addOrderPanel.add(new javax.swing.JLabel("Quantity:"));
        addOrderPanel.add(quantityField);
        addOrderPanel.add(new javax.swing.JLabel("Price:"));
        addOrderPanel.add(priceField);

        JButton submitOrderButton = new JButton("Submit Order");
        submitOrderButton.addActionListener(e -> {
            String firstName = firstNameField.getText();
            String lastName = lastNameField.getText();
            String itemName = itemNameField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());

            try {
                addOrderToDatabase(dbConn, firstName, lastName, itemName, quantity, price);
                loadPendingOrdersData(dbConn, ordersTable);
                tabbedPane.setSelectedIndex(tabbedPane.indexOfTab("Pending Orders"));
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        addOrderPanel.add(submitOrderButton);
        tabbedPane.addTab("Add Order", addOrderPanel);
        tabbedPane.setSelectedComponent(addOrderPanel);
    }

    private static void addOrderToDatabase(Connection dbConn, String firstName, String lastName, String itemName, int quantity, double price) throws SQLException {
        String insertSQL = "INSERT INTO PendingOrders (first_name, last_name, item_name, quantity, price) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.prepareStatement(insertSQL)) {
            pstmt.setString(1, firstName);
            pstmt.setString(2, lastName);
            pstmt.setString(3, itemName);
            pstmt.setInt(4, quantity);
            pstmt.setDouble(5, price);
            pstmt.executeUpdate();
            System.out.println("Order added successfully.");
        }
    }

    private static void loadInventoryData(Connection dbConn, JTable table) throws SQLException {
        String selectSQL = "SELECT id, item_name, category, quantity, price FROM Inventory";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

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

        table.setModel(model);
    }

    private static void loadPendingOrdersData(Connection dbConn, JTable table) throws SQLException {
        String selectSQL = "SELECT order_id, first_name, last_name, item_name, quantity, price, status FROM PendingOrders";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

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

        table.setModel(model);
    }

    private static void loadCanceledOrdersData(Connection dbConn, JTable table) throws SQLException {
        String selectSQL = "SELECT order_id, first_name, last_name, item_name, quantity, price, status FROM CanceledOrders";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

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

        table.setModel(model);
    }

    private static void loadCompletedOrdersData(Connection dbConn, JTable table) throws SQLException {
        String selectSQL = "SELECT order_id, first_name, last_name, item_name, quantity, price, status FROM CompletedOrders";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

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

        table.setModel(model);
    }

    private static void searchBarInventory(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException {
        String searchColumn = searchCriterion.equals("Category") ? "category" : "item_name";
        String searchSQL = "SELECT id, item_name, category, quantity, price FROM Inventory WHERE " + searchColumn + " LIKE ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(searchSQL)) {
            pstmt.setString(1, "%" + searchInput + "%");
            ResultSet rs = pstmt.executeQuery();

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

            table.setModel(model);
        }
    }
}
