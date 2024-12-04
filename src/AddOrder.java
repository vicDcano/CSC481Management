import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
// import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class AddOrder extends JFrame {
    private Connection dbConn;
    private JTable ordersTable;
    private DecentBuyOrderData orderData;

    private final Color primaryColor = new Color(0, 122, 204);   // Blue
    private final Color secondaryColor = new Color(98, 114, 164); // Magenta

    public AddOrder(Connection dbConn, JTable ordersTable) throws SQLException {
        this.dbConn = dbConn;
        this.ordersTable = ordersTable;
        this.orderData = new DecentBuyOrderData();

        setTitle("Reordering Page");
        setSize(400, 200); // Adjusted size for three buttons
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // Close only this window
        setLayout(new BorderLayout());

        // Welcome label
        JLabel welcomeLabel = new JLabel("Odering Options", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomeLabel, BorderLayout.NORTH);

        // Main system features (only three buttons)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3, 10, 10)); // Single row with three buttons
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton customerButton = new JButton("Customer");
        JButton supplierButton = new JButton("Supplier");
        JButton exitButton = new JButton("Exit");

        // Style buttons
        styleButton(customerButton);
        styleButton(supplierButton);
        styleButton(exitButton);

        buttonPanel.add(customerButton);
        buttonPanel.add(supplierButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.CENTER);

        // Action Listeners
        customerButton.addActionListener(e -> {
            try {
                openCustomerOrderDialog();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error opening Customer Order Dialog.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        supplierButton.addActionListener(e -> {
            try {
                openSupplierOrderDialog();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error opening Supplier Order Dialog.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        exitButton.addActionListener(e -> dispose()); // Close only the AddOrder window

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    // Method to style buttons uniformly
    private void styleButton(JButton button) {
        button.setBackground(primaryColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(primaryColor));

        // Hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(secondaryColor);
                button.setBorder(BorderFactory.createLineBorder(secondaryColor));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(primaryColor);
                button.setBorder(BorderFactory.createLineBorder(primaryColor));
            }
        });
    }

    /**
     * Opens the Customer Order Dialog.
     */
    private void openCustomerOrderDialog() throws SQLException {
        JDialog dialog = new JDialog(this, "Add Customer Order", true);
        dialog.setSize(450, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Customer Selection
        JLabel customerLabel = new JLabel("Select Customer:");
        JComboBox<String> customerComboBox = new JComboBox<>();
        populateCustomerComboBox(customerComboBox);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(customerLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(customerComboBox, gbc);

        // Product Selection
        JLabel productLabel = new JLabel("Select Product:");
        JComboBox<String> productComboBox = new JComboBox<>();
        populateProductComboBox(productComboBox);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(productLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(productComboBox, gbc);

        // Quantity
        JLabel quantityLabel = new JLabel("Quantity:");
        JTextField quantityField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(quantityLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(quantityField, gbc);

        // Order Date
        JLabel dateLabel = new JLabel("Order Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(LocalDate.now().toString());

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(dateLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(dateField, gbc);

        // Status and Type (Hidden Fields)
        JLabel statusLabel = new JLabel("Status:");
        JLabel statusValue = new JLabel("Pending"); // Default status

        JLabel typeLabel = new JLabel("Type:");
        JLabel typeValue = new JLabel("Customer"); // Type is Customer

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(statusValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(typeValue, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");
        styleButton(submitButton);
        styleButton(cancelButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        // Action Listeners
        submitButton.addActionListener(e -> {
            String selectedCustomer = (String) customerComboBox.getSelectedItem();
            String selectedProduct = (String) productComboBox.getSelectedItem();
            String quantityText = quantityField.getText().trim();
            String dateText = dateField.getText().trim();
            String status = statusValue.getText();
            String type = typeValue.getText();

            // Validation
            if (selectedCustomer == null || selectedProduct == null || quantityText.isEmpty() || dateText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity must be a positive integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Date Format
            if (!dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(dialog, "Date must be in YYYY-MM-DD format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Extract first and last name
            String[] nameParts = selectedCustomer.split(" ");
            if (nameParts.length < 2) {
                JOptionPane.showMessageDialog(dialog, "Selected customer name is invalid.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            String firstName = nameParts[0];
            String lastName = nameParts[1];

            // Fetch customer ID
            int customerId = -1;
            String fetchCustomerSQL = "SELECT idCustomer FROM Customer WHERE customerFirst = ? AND customerLast = ?";
            try (PreparedStatement pstmt = dbConn.prepareStatement(fetchCustomerSQL)) {
                pstmt.setString(1, firstName);
                pstmt.setString(2, lastName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    customerId = rs.getInt("idCustomer");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Customer not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error while fetching customer ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Fetch product ID and price
            int productId = -1;
            double productPrice = 0.0;
            String fetchProductSQL = "SELECT idProducts, productPrice, productStock FROM Products WHERE productName = ?";
            try (PreparedStatement pstmt = dbConn.prepareStatement(fetchProductSQL)) {
                pstmt.setString(1, selectedProduct);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    productId = rs.getInt("idProducts");
                    productPrice = rs.getDouble("productPrice");
                    int currentStock = rs.getInt("productStock");

                    if (currentStock < quantity) {
                        JOptionPane.showMessageDialog(dialog, "Insufficient stock for the selected product.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Product not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error while fetching product details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double totalCost = productPrice * quantity;

            // Insert into DBOrder table
            String insertOrderSQL = "INSERT INTO DBOrder (DBOrderDate, DBOrderQuantity, DBOrderTotalCost, Customer_idCustomer, Products_idProducts, DBOrderStatus, DBOrderType) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = dbConn.prepareStatement(insertOrderSQL)) {
                pstmt.setString(1, dateText);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, totalCost);
                pstmt.setInt(4, customerId);
                pstmt.setInt(5, productId);
                pstmt.setString(6, status);
                pstmt.setString(7, type);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(dialog, "Customer order added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Update the product stock
                    String updateStockSQL = "UPDATE Products SET productStock = productStock - ? WHERE idProducts = ?";
                    try (PreparedStatement updatePstmt = dbConn.prepareStatement(updateStockSQL)) {
                        updatePstmt.setInt(1, quantity);
                        updatePstmt.setInt(2, productId);
                        updatePstmt.executeUpdate();
                    }

                    // Refresh the orders table in the main window
                    orderData.loadOrdersData(dbConn, ordersTable);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add the order.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Opens the Supplier Order Dialog.
     */
    private void openSupplierOrderDialog() throws SQLException {
        JDialog dialog = new JDialog(this, "Add Supplier Order", true);
        dialog.setSize(450, 400);
        dialog.setLayout(new BorderLayout());
        dialog.setLocationRelativeTo(this);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Supplier Selection
        JLabel supplierLabel = new JLabel("Select Supplier:");
        JComboBox<String> supplierComboBox = new JComboBox<>();
        populateSupplierComboBox(supplierComboBox);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(supplierLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        panel.add(supplierComboBox, gbc);

        // Product Selection
        JLabel productLabel = new JLabel("Select Product:");
        JComboBox<String> productComboBox = new JComboBox<>();
        populateProductComboBox(productComboBox);

        gbc.gridx = 0;
        gbc.gridy = 1;
        panel.add(productLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        panel.add(productComboBox, gbc);

        // Quantity
        JLabel quantityLabel = new JLabel("Quantity:");
        JTextField quantityField = new JTextField();

        gbc.gridx = 0;
        gbc.gridy = 2;
        panel.add(quantityLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        panel.add(quantityField, gbc);

        // Order Date
        JLabel dateLabel = new JLabel("Order Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField(LocalDate.now().toString());

        gbc.gridx = 0;
        gbc.gridy = 3;
        panel.add(dateLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        panel.add(dateField, gbc);

        // Status and Type (Hidden Fields)
        JLabel statusLabel = new JLabel("Status:");
        JLabel statusValue = new JLabel("Pending"); // Default status

        JLabel typeLabel = new JLabel("Type:");
        JLabel typeValue = new JLabel("Supplier"); // Type is Supplier

        gbc.gridx = 0;
        gbc.gridy = 4;
        panel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(statusValue, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        panel.add(typeLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        panel.add(typeValue, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel();
        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");
        styleButton(submitButton);
        styleButton(cancelButton);
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        // Action Listeners
        submitButton.addActionListener(e -> {
            String selectedSupplier = (String) supplierComboBox.getSelectedItem();
            String selectedProduct = (String) productComboBox.getSelectedItem();
            String quantityText = quantityField.getText().trim();
            String dateText = dateField.getText().trim();
            String status = statusValue.getText();
            String type = typeValue.getText();

            // Validation
            if (selectedSupplier == null || selectedProduct == null || quantityText.isEmpty() || dateText.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled!", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int quantity;
            try {
                quantity = Integer.parseInt(quantityText);
                if (quantity <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity must be a positive integer.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Validate Date Format
            if (!dateText.matches("\\d{4}-\\d{2}-\\d{2}")) {
                JOptionPane.showMessageDialog(dialog, "Date must be in YYYY-MM-DD format.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Fetch supplier ID
            int supplierId = -1;
            String fetchSupplierSQL = "SELECT idSupplier FROM Supplier WHERE SupplierName = ?";
            try (PreparedStatement pstmt = dbConn.prepareStatement(fetchSupplierSQL)) {
                pstmt.setString(1, selectedSupplier);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    supplierId = rs.getInt("idSupplier");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Supplier not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error while fetching supplier ID.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Fetch product ID and price
            int productId = -1;
            double productPrice = 0.0;
            String fetchProductSQL = "SELECT idProducts, productPrice, productStock FROM Products WHERE productName = ?";
            try (PreparedStatement pstmt = dbConn.prepareStatement(fetchProductSQL)) {
                pstmt.setString(1, selectedProduct);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    productId = rs.getInt("idProducts");
                    productPrice = rs.getDouble("productPrice");
                    int currentStock = rs.getInt("productStock");

                    if (currentStock < quantity) {
                        JOptionPane.showMessageDialog(dialog, "Insufficient stock for the selected product.", "Stock Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } else {
                    JOptionPane.showMessageDialog(dialog, "Product not found in the database.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error while fetching product details.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            double totalCost = productPrice * quantity;

            // Insert into DBOrder table
            String insertOrderSQL = "INSERT INTO DBOrder (DBOrderDate, DBOrderQuantity, DBOrderTotalCost, Supplier_idSupplier, Products_idProducts, DBOrderStatus, DBOrderType) " +
                                    "VALUES (?, ?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = dbConn.prepareStatement(insertOrderSQL)) {
                pstmt.setString(1, dateText);
                pstmt.setInt(2, quantity);
                pstmt.setDouble(3, totalCost);
                pstmt.setInt(4, supplierId);
                pstmt.setInt(5, productId);
                pstmt.setString(6, status);
                pstmt.setString(7, type);

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(dialog, "Supplier order added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                    // Update the product stock
                    String updateStockSQL = "UPDATE Products SET productStock = productStock - ? WHERE idProducts = ?";
                    try (PreparedStatement updatePstmt = dbConn.prepareStatement(updateStockSQL)) {
                        updatePstmt.setInt(1, quantity);
                        updatePstmt.setInt(2, productId);
                        updatePstmt.executeUpdate();
                    }

                    // Refresh the orders table in the main window
                    orderData.loadOrdersData(dbConn, ordersTable);
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Failed to add the order.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Database error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    /**
     * Populates the Customer JComboBox with customer names from the database.
     */
    private void populateCustomerComboBox(JComboBox<String> customerComboBox) {
        String customerSQL = "SELECT customerFirst, customerLast FROM Customer";
        try (Statement stmt = dbConn.createStatement();
             ResultSet rs = stmt.executeQuery(customerSQL)) {
            while (rs.next()) {
                String name = rs.getString("customerFirst") + " " + rs.getString("customerLast");
                customerComboBox.addItem(name);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching customers from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Populates the Supplier JComboBox with supplier names from the database.
     */
    private void populateSupplierComboBox(JComboBox<String> supplierComboBox) {
        String supplierSQL = "SELECT SupplierName FROM Supplier";
        try (Statement stmt = dbConn.createStatement();
             ResultSet rs = stmt.executeQuery(supplierSQL)) {
            while (rs.next()) {
                supplierComboBox.addItem(rs.getString("SupplierName"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching suppliers from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Populates the Product JComboBox with product names from the database.
     */
    private void populateProductComboBox(JComboBox<String> productComboBox) {
        String productSQL = "SELECT productName FROM Products";
        try (Statement stmt = dbConn.createStatement();
             ResultSet rs = stmt.executeQuery(productSQL)) {
            while (rs.next()) {
                productComboBox.addItem(rs.getString("productName"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error fetching products from database.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
