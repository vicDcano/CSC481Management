import javax.swing.*;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

import com.formdev.flatlaf.FlatDarculaLaf;

@SuppressWarnings("unused")

public class DecentBuyFrame extends JFrame { 
    private JTable ordersTable;
    private JTable inventoryTable;
    // Uncomment these if you decide to use them in the future
    // private JTable canceledOrdersTable;
    // private JTable completedOrdersTable;
    
    DecentBuyOrderData DBDB_OrderData = new DecentBuyOrderData();
    DatabaseConn Conn = new DatabaseConn();
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Define custom highlight colors
    private final Color primaryColor = new Color(0, 122, 204);   // Blue
    private final Color secondaryColor = new Color(98, 114, 164); // Magenta

    public DecentBuyFrame() throws SQLException {
        // Set FlatDarcula Look and Feel
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        Connection dbConn = Conn.getConnection();
        setTitle("DecentBuy Inventory Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // Center the frame

        add(createTabbedPane(dbConn));
        setVisible(true);
    }

    /**
     * Creates the main tabbed pane with existing tabs.
     */
    public JTabbedPane createTabbedPane(Connection dbConn) {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Customize tabbedPane appearance
        tabbedPane.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tabbedPane.setForeground(primaryColor);

        tabbedPane.addTab("Product Inventory", createInventoryPanel(dbConn));
        tabbedPane.addTab("Orders", createOrdersPanel(dbConn));
        // Uncomment these if you decide to use them in the future
        // tabbedPane.addTab("Cancelled Orders", createCanceledOrdersPanel(dbConn));
        // tabbedPane.addTab("Completed Orders", createCompletedOrdersPanel(dbConn));
        return tabbedPane;
    }

    /**
     * Creates the Product Inventory panel.
     */
    public JPanel createInventoryPanel(Connection dbConn) {
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        inventoryTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = createSearchPanel(dbConn, inventoryTable);
        inventoryPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = createInventoryButtonPanel(dbConn, inventoryTable);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        try {
            DBDB_OrderData.loadInventoryData(dbConn, inventoryTable);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load inventory data: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
        return inventoryPanel;
    }

    private final Map<String, String> searchCriteriaMap = new HashMap<>();

    {
        searchCriteriaMap.put("Product ID", "idProducts");
        searchCriteriaMap.put("Product Name", "productName");
        searchCriteriaMap.put("Product Category", "productCategory");
        searchCriteriaMap.put("Product Brand", "productBrand");
        searchCriteriaMap.put("Product Price", "productPrice");
        searchCriteriaMap.put("Product Stock", "productStock");
    }

    /**
     * Creates the search panel for Product Inventory.
     */
    public JPanel createSearchPanel(Connection dbConn, JTable table) {
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.DARK_GRAY);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] searchOptions = {"Product ID", "Product Name", "Product Category", "Product Brand", "Product Price", "Product Stock"};
        
        JComboBox<String> searchDropdown = new JComboBox<>(searchOptions);
        searchDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchDropdown.setForeground(Color.WHITE);
        searchDropdown.setBackground(Color.GRAY);

        JTextField searchTextField = new JTextField(15);
        searchTextField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton searchButton = new JButton("Search");
        styleButton(searchButton, primaryColor, secondaryColor);
       // String displayCriteria = (String) searchDropdown.getSelectedItem();
       // String columnCriteria = searchCriteriaMap.get(displayCriteria);


        searchButton.addActionListener(e -> {
            String displayCriteria = (String) searchDropdown.getSelectedItem();
            String columnCriteria = searchCriteriaMap.get(displayCriteria);
        
            if (columnCriteria == null) {
                JOptionPane.showMessageDialog(this, "Invalid Search Criteria Selected.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
        
            try {
                DBDB_OrderData.searchBarInventory(dbConn, table, columnCriteria, searchTextField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchPanel.add(searchDropdown);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        return searchPanel;
    }

    /**
     * Creates the Orders panel with existing buttons.
     */
    public JPanel createOrdersPanel(Connection dbConn) {
        JPanel ordersPanel = new JPanel(new BorderLayout());
        ordersTable = new JTable(); 
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        ordersPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = createOrderSearchPanel(dbConn, ordersTable);
        ordersPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refreshOrdersButton = new JButton("Refresh Orders");
        styleButton(refreshOrdersButton, primaryColor, secondaryColor);
        refreshOrdersButton.addActionListener(e -> {
            try {
                DBDB_OrderData.loadDBOrderData(dbConn, ordersTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Refresh failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(refreshOrdersButton);

        JButton addOrderButton = new JButton("Add Order");
        styleButton(addOrderButton, primaryColor, secondaryColor);
        addOrderButton.addActionListener(e -> openAddOrderDialog(dbConn));
        buttonPanel.add(addOrderButton);

        ordersPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load data initially
        try {
            DBDB_OrderData.loadDBOrderData(dbConn, ordersTable);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load orders data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return ordersPanel;
    }

    /**
     * Creates the search panel for Orders.
     */
    public JPanel createOrderSearchPanel(Connection dbConn, JTable table) {
        JPanel searchPanel = new JPanel();
        searchPanel.setBackground(Color.DARK_GRAY);
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        String[] searchOptions = {"Order ID", "Current", "Pending", "Cancelled", "Customer", "Supplier"};
        JComboBox<String> searchDropdown = new JComboBox<>(searchOptions);
        searchDropdown.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchDropdown.setForeground(Color.WHITE);
        searchDropdown.setBackground(Color.GRAY);

        JTextField searchTextField = new JTextField(15);
        searchTextField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JButton searchButton = new JButton("Search");
        styleButton(searchButton, primaryColor, secondaryColor);

        searchButton.addActionListener(e -> {
            try {
                DBDB_OrderData.searchBarInventory(dbConn, table, searchDropdown.getSelectedItem().toString(), searchTextField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Search failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        searchPanel.add(searchDropdown);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        return searchPanel;
    }

    /**
     * Creates the Inventory Button panel with a refresh button.
     */
    public JPanel createInventoryButtonPanel(Connection dbConn, JTable table) {
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton refreshButton = new JButton("Refresh Inventory");
        styleButton(refreshButton, primaryColor, secondaryColor);
        refreshButton.addActionListener(e -> {
            try {
                updateInventoryQuantities(dbConn);
                DBDB_OrderData.loadInventoryData(dbConn, table);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Refresh failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        buttonPanel.add(refreshButton);
        return buttonPanel;
    }

    /**
     * Updates inventory quantities based on pending orders.
     */
    private void updateInventoryQuantities(Connection dbConn) throws SQLException {
        String updateInventorySQL = 
                "UPDATE Products AS i " +
                "JOIN " +
                    "(SELECT Products_idProducts, SUM(DBOrderQuantity) AS total_ordered " +
                    " FROM DBOrder WHERE DBOrderStatus = 'Pending' " +
                    " GROUP BY Products_idProducts ) As o" +
                    " ON i.idProducts = o.Products_idProducts " +
                    " SET i.productStock = i.productStock - o.total_ordered " +
                    " WHERE i.productStock >= o.total_ordered";

        try (PreparedStatement pstmt = dbConn.prepareStatement(updateInventorySQL)) {
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println(rowsUpdated + " inventory items updated based on pending orders.");
        }

        String markedProcessedSQL = 
            "UPDATE DBOrder SET DBOrderStatus = 'Current' WHERE DBOrderStatus = 'Pending'";
        try (PreparedStatement pstmt = dbConn.prepareStatement(markedProcessedSQL)) {
            pstmt.executeUpdate();
        }
    }

    /**
     * Opens the dialog to add a new order.
     */
    private void openAddOrderDialog(Connection dbConn) {
        // Create a dialog window
        JDialog dialog = new JDialog(this, "Add New Order", true);
        dialog.setSize(450, 500);
        dialog.setLocationRelativeTo(this);

        // Create input fields
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        panel.setBackground(Color.DARK_GRAY);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel orderDateLabel = new JLabel("Order Date: (YYYY-MM-DD)");
        orderDateLabel.setForeground(Color.WHITE);
        JTextField orderDateField = new JTextField(20);
        orderDateField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel orderQuantityLabel = new JLabel("Order Quantity:");
        orderQuantityLabel.setForeground(Color.WHITE);
        JTextField orderQuantityField = new JTextField(20);
        orderQuantityField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel customerIdLabel = new JLabel("Customer ID:");
        customerIdLabel.setForeground(Color.WHITE);
        JTextField customerIdField = new JTextField(20);
        customerIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel customerFirstLabel = new JLabel("Customer First Name:");
        customerFirstLabel.setForeground(Color.WHITE);
        JTextField customerFirstField = new JTextField(20);
        customerFirstField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel customerLastLabel = new JLabel("Customer Last Name:");
        customerLastLabel.setForeground(Color.WHITE);
        JTextField customerLastField = new JTextField(20);
        customerLastField.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JLabel productLabel = new JLabel("Product: ");
        productLabel.setForeground(Color.WHITE);
        JComboBox<String> productComboBox = new JComboBox<>();
        productComboBox.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        try {
            Statement stmt = dbConn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT idProducts, productName FROM Products");
            while (rs.next()) {
                int productId = rs.getInt("idProducts");
                String productName = rs.getString("productName");
                productComboBox.addItem(productId + " - " + productName);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Failed to load products: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        // Add components to panel
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(orderDateLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(orderDateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(orderQuantityLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(orderQuantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(customerIdLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(customerIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(customerFirstLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(customerFirstField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(customerLastLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(customerLastField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(productLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(productComboBox, gbc);

        // Buttons
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");
        styleButton(submitButton, primaryColor, secondaryColor);
        styleButton(cancelButton, primaryColor, secondaryColor);

        // Action Listener for Submit button
        submitButton.addActionListener(e -> {
            String orderDate = orderDateField.getText();
            String orderQuantityText = orderQuantityField.getText();
            String customerIdText = customerIdField.getText();
            String customerFirst = customerFirstField.getText();
            String customerLast = customerLastField.getText();
            String selectedProduct = (String) productComboBox.getSelectedItem();

            if (orderDate.isEmpty() || orderQuantityText.isEmpty() ||
                customerIdText.isEmpty() || customerFirst.isEmpty() || customerLast.isEmpty() || selectedProduct == null) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                // Validate date format
                LocalDate.parse(orderDate, formatter);

                int orderQuantity = Integer.parseInt(orderQuantityText);
                int customerId = Integer.parseInt(customerIdText);
                int productId = Integer.parseInt(selectedProduct.split(" - ")[0]);

                // Retrieve product price
                double productPrice = getProductPrice(dbConn, productId);

                // Calculate total cost
                double totalCost = productPrice * orderQuantity;

                // Insert or update customer information
                insertOrUpdateCustomer(dbConn, customerId, customerFirst, customerLast);

                // Add order to database
                addOrderToDatabase(dbConn, orderDate, orderQuantity, totalCost, "Pending", "Customer", customerId, productId);

                // Refresh the orders and inventory tables
                DBDB_OrderData.loadDBOrderData(dbConn, ordersTable);
                DBDB_OrderData.loadInventoryData(dbConn, inventoryTable);

                JOptionPane.showMessageDialog(dialog, "Order added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Numeric fields must contain valid numbers!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (DateTimeParseException ex) {
                JOptionPane.showMessageDialog(dialog, "Invalid date format! Please use YYYY-MM-DD.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Failed to add order: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Action Listener for Cancel button
        cancelButton.addActionListener(e -> dialog.dispose());

        // Add buttons to panel
        gbc.gridx = 0;
        gbc.gridy = 6;
        panel.add(submitButton, gbc);

        gbc.gridx = 1;
        gbc.gridy = 6;
        panel.add(cancelButton, gbc);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.setVisible(true);
    }

    /**
     * Retrieves the price of a product by its ID.
     */
    private double getProductPrice(Connection dbConn, int productId) throws SQLException {
        String selectPriceSQL = "SELECT productPrice FROM Products WHERE idProducts = ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(selectPriceSQL)) {
            pstmt.setInt(1, productId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("productPrice");
            } else {
                throw new SQLException("Product not found!");
            }
        }
    }

    /**
     * Inserts a new customer or updates an existing one.
     */
    private void insertOrUpdateCustomer(Connection dbConn, int customerId, String firstName, String lastName) throws SQLException {
        String checkCustomerSQL = "SELECT * FROM Customer WHERE idCustomer = ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(checkCustomerSQL)) {
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Customer exists, update
                String updateCustomerSQL = "UPDATE Customer SET customerFirst = ?, customerLast = ? WHERE idCustomer = ?";
                try (PreparedStatement updateStmt = dbConn.prepareStatement(updateCustomerSQL)) {
                    updateStmt.setString(1, firstName);
                    updateStmt.setString(2, lastName);
                    updateStmt.setInt(3, customerId);
                    updateStmt.executeUpdate();
                }
            } else {
                // Customer does not exist, insert
                String insertCustomerSQL = "INSERT INTO Customer (idCustomer, customerFirst, customerLast) VALUES (?, ?, ?)";
                try (PreparedStatement insertStmt = dbConn.prepareStatement(insertCustomerSQL)) {
                    insertStmt.setInt(1, customerId);
                    insertStmt.setString(2, firstName);
                    insertStmt.setString(3, lastName);
                    insertStmt.executeUpdate();
                }
            }
        }
    }

    /**
     * Adds a new order to the database.
     */
    private void addOrderToDatabase(Connection dbConn, String date, int quantity, double totalCost, String status, String type, int customerId, int productId) throws SQLException {
        String insertOrderSQL = "INSERT INTO DBOrder (DBOrderDate, DBOrderQuantity, DBOrderTotalCost, DBOrderStatus, DBOrderType, Customer_idCustomer, Products_idProducts) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbConn.prepareStatement(insertOrderSQL)) {
            pstmt.setString(1, date);
            pstmt.setInt(2, quantity);
            pstmt.setDouble(3, totalCost);
            pstmt.setString(4, status);
            pstmt.setString(5, type);
            pstmt.setInt(6, customerId);
            pstmt.setInt(7, productId);
            pstmt.executeUpdate();
            System.out.println("Order added successfully.");
        }
    }

    // Uncomment the following methods if you decide to use them in the future
    /*
    public JPanel createCanceledOrdersPanel(Connection dbConn) {
        JPanel canceledPanel = new JPanel(new BorderLayout());
        canceledOrdersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(canceledOrdersTable);
        canceledPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshCanceledButton = new JButton("Refresh Canceled Orders");
        styleButton(refreshCanceledButton, primaryColor, secondaryColor);
        refreshCanceledButton.addActionListener(e -> {
            try {
                DBDB_OrderData.loadCanceledOrdersData(dbConn, canceledOrdersTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Refresh failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(refreshCanceledButton);
        canceledPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load data initially
        try {
            DBDB_OrderData.loadCanceledOrdersData(dbConn, canceledOrdersTable);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load canceled orders data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return canceledPanel;
    }

    public JPanel createCompletedOrdersPanel(Connection dbConn) {
        JPanel completedPanel = new JPanel(new BorderLayout());
        completedOrdersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(completedOrdersTable);
        completedPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshCompletedButton = new JButton("Refresh Completed Orders");
        styleButton(refreshCompletedButton, primaryColor, secondaryColor);
        refreshCompletedButton.addActionListener(e -> {
            try {
                DBDB_OrderData.loadCompletedOrdersData(dbConn, completedOrdersTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Refresh failed: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.DARK_GRAY);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        buttonPanel.add(refreshCompletedButton);
        completedPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Load data initially
        try {
            DBDB_OrderData.loadCompletedOrdersData(dbConn, completedOrdersTable);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Failed to load completed orders data: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }

        return completedPanel;
    }
    */

    /**
     * Styles buttons with primary and hover colors.
     */
    private void styleButton(JButton button, Color bgColor, Color hoverColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(bgColor));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverColor);
                button.setBorder(BorderFactory.createLineBorder(hoverColor));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bgColor);
                button.setBorder(BorderFactory.createLineBorder(bgColor));
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new DecentBuyFrame();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Failed to launch application: " + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
