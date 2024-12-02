import javax.swing.*;
import javax.swing.table.*;
import java.awt.BorderLayout;
import java.sql.*;
import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.*;



@SuppressWarnings("unused")
public class DecentBuyFrame extends JFrame{
    DecentBuyOrderData DBDB_OrderData = new DecentBuyOrderData();
    DatabaseConn Conn = new DatabaseConn();
    
    LocalDate currentDate = LocalDate.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DecentBuyFrame() throws SQLException {
        Connection dbConn = Conn.getConnection();
        setTitle("DecentBuy Inventory Management");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        add(createTabbedPane(dbConn));
        setVisible(true);
    }

    public JTabbedPane createTabbedPane(Connection dbConn) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Product Inventory", createInventoryPanel(dbConn));
        tabbedPane.addTab("Orders", createOrdersPanel(dbConn));
        //tabbedPane.addTab("Cancelled Orders", createCompletedOrdersPanel(dbConn));
        //tabbedPane.addTab("Current Orders", createCanceledOrdersPanel(dbConn));
        return tabbedPane;
    }

    public JPanel createInventoryPanel(Connection dbConn) {
        JPanel inventoryPanel = new JPanel(new BorderLayout());
        JTable table = new JTable();
        JScrollPane scrollPane = new JScrollPane(table);
        inventoryPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel searchPanel = createSearchPanel(dbConn, table);
        inventoryPanel.add(searchPanel, BorderLayout.NORTH);

        JPanel buttonPanel = createInventoryButtonPanel(dbConn, table);
        inventoryPanel.add(buttonPanel, BorderLayout.SOUTH);

        try {
            DBDB_OrderData.loadInventoryData(dbConn, table);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return inventoryPanel;
    }

    public JPanel createSearchPanel(Connection dbConn, JTable table) {
        JPanel searchPanel = new JPanel();
        String[] searchOptions = {"idProducts", "productName", "productCategory", "productBrand", "productPrice", "productStock"};
        JComboBox<String> searchDropdown = new JComboBox<>(searchOptions);
        JTextField searchTextField = new JTextField(15);
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> {

            try {
                DBDB_OrderData.searchBarInventory(dbConn, table, searchDropdown.getSelectedItem().toString(), searchTextField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        searchPanel.add(searchDropdown);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        return searchPanel;
    }

    public JPanel createOrderSearchPanel(Connection dbConn, JTable table) {
        JPanel searchPanel = new JPanel();
        String[] searchOptions = {"idDBOrder", "Current", "Pending", "Cancelled", "Customer", "Supplier"};
        JComboBox<String> searchDropdown = new JComboBox<>(searchOptions);
        JTextField searchTextField = new JTextField(15);
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> {

            try {
                DBDB_OrderData.searchBarInventory(dbConn, table, searchDropdown.getSelectedItem().toString(), searchTextField.getText());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        searchPanel.add(searchDropdown);
        searchPanel.add(searchTextField);
        searchPanel.add(searchButton);
        return searchPanel;
    }

    public JPanel createInventoryButtonPanel(Connection dbConn, JTable table) {
        JPanel buttonPanel = new JPanel();
        JButton refreshButton = new JButton("Refresh Inventory");
        refreshButton.addActionListener(e -> {
            try {
                updateInventoryQuantities(dbConn);
                DBDB_OrderData.loadInventoryData(dbConn, table);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(refreshButton);
        return buttonPanel;
    }

    private void updateInventoryQuantities(Connection dbConn) throws SQLException {
        String updateInventorySQL = 
                "UPDATE Products AS inventory " +
                "JOIN " +
                    "(SELECT ProductsName, SUM(ProductsStock) AS total_ordered " +
                    " FROM PendingOrders WHERE processed = FALSE" +
                    " GROUP BY ProductsName ) As o" +
                    " ON i.ProductsName = o.ProductsName " +
                    " SET i.ProductsStock = i.ProductsStock - o.total_ordered " +
                    " WHERE i.ProductsStock >= o.total_ordered";

        try (PreparedStatement pstmt = dbConn.prepareStatement(updateInventorySQL)) {
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println(rowsUpdated + " inventory items updated based on pending orders.");
        }

        String markedProcessedSQL = 
            "UPDATE DBOrder SET processed = TRUE WHERE processed = FALSE";
        try (PreparedStatement pstmt = dbConn.prepareStatement(markedProcessedSQL)) {
            pstmt.executeUpdate();
        }
    }

    public JPanel createOrdersPanel(Connection dbConn) {
        JPanel ordersPanel = new JPanel(new BorderLayout());
        JTable ordersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        ordersPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshOrdersButton = new JButton("Refresh Orders");

        try {
            DBDB_OrderData.loadOrdersData(dbConn, ordersTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        refreshOrdersButton.addActionListener(e -> {
            try {
                DBDB_OrderData.loadOrdersData(dbConn, ordersTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(refreshOrdersButton);

        JButton addOrderButton = new JButton("Add Order");
       addOrderButton.addActionListener(e -> openAddOrderDialog(dbConn, ordersTable)); {
        buttonPanel.add(addOrderButton);

        ordersPanel.add(buttonPanel, BorderLayout.SOUTH);
        return ordersPanel;

        }
    }

    private void openAddOrderDialog(Connection dbConn, JTable ordersTable) {
    // Create a dialog window
    JDialog dialog = new JDialog();
    dialog.setTitle("Add New Order");
    dialog.setSize(400, 400);
    dialog.setLocationRelativeTo(null);
    dialog.setModal(true);

    // Create input fields
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

    JTextField idDBOField = new JTextField(20);
    JTextField DBO_Quantity = new JTextField(20);
    JTextField DBO_Type = new JTextField(20);
    JTextField DBO_Date = new JTextField(20);

    JComboBox<String> itemComboBox = new JComboBox<>(); // Populates products
    try {
        Statement stmt = dbConn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT productName FROM Products");
        while (rs.next()) {
            itemComboBox.addItem(rs.getString("productName"));
        }
    } catch (SQLException ex) {
        ex.printStackTrace();
    }

    // Add fields to the panel
    panel.add(new JLabel("Order ID:"));
    panel.add(idDBOField);
    panel.add(new JLabel("Date (YYYY-MM-DD):"));
    panel.add(DBO_Date);
    panel.add(new JLabel("Quantity:"));
    panel.add(DBO_Quantity);
    panel.add(new JLabel("Type (Customer or Supplier):"));
    panel.add(DBO_Type);
    panel.add(new JLabel("Product:"));
    panel.add(itemComboBox);

    // Buttons
    JButton submitButton = new JButton("Submit");
    JButton cancelButton = new JButton("Cancel");

    // Submit button action
    submitButton.addActionListener(e -> {
        String id = idDBOField.getText();
        String formattedDate = DBO_Date.getText();
        String quantityText = DBO_Quantity.getText();
        String type = DBO_Type.getText();
        String productName = (String) itemComboBox.getSelectedItem();

        if (id.isEmpty() || formattedDate.isEmpty() || quantityText.isEmpty() || type.isEmpty() || productName == null) {
            JOptionPane.showMessageDialog(dialog, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            int orderId = Integer.parseInt(id);
            int quantity = Integer.parseInt(quantityText);
            double price = 0.0;

            // Fetch product price
            String selectPriceSQL = "SELECT ProductsPrice FROM Products WHERE productName = ?";
            try (PreparedStatement pstmt = dbConn.prepareStatement(selectPriceSQL)) {
                pstmt.setString(1, productName);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    price = rs.getDouble("ProductsPrice");
                } else {
                    JOptionPane.showMessageDialog(dialog, "Product not found!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Calculate total cost
            double totalCost = price * quantity;

            // Insert the new order
            addOrderToDatabase(dbConn, orderId, formattedDate, quantity, totalCost, "Pending", type);

            // Refresh the orders table
            DBDB_OrderData.loadOrdersData(dbConn, ordersTable);
            dialog.dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(dialog, "Order ID and Quantity must be numbers!", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(dialog, "Failed to add order. Check logs for details.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    });

    // Cancel button action
    cancelButton.addActionListener(e -> dialog.dispose());

    // Add buttons to a panel
    JPanel buttonPanel = new JPanel();
    buttonPanel.add(submitButton);
    buttonPanel.add(cancelButton);

    // Add components to the dialog
    dialog.add(panel, BorderLayout.CENTER);
    dialog.add(buttonPanel, BorderLayout.SOUTH);

    dialog.setVisible(true);
}

    // successfully adds orders, but new orders are not being added to the database, josh look into this 
    private void addOrderToDatabase(Connection dbConn, int id, String date, int quantity, double totalCost, 
                                 String supplierName, String productName, String customerFirst, String customerLast, 
                                 String status, String type) throws SQLException {
    // SQL query to insert a new order with subqueries for foreign keys
    String insertSQL = "INSERT INTO decentbuy3.DBOrder " + 
                       "(idDBOrder, DBOrderDate, DBOrderQuantity, DBOrderTotalCost, " +
                       "Supplier_idSupplier, Products_idProducts, Customer_idCustomer, " +
                       "DBOrderStatus, DBOrderType) " +
                       "VALUES (?, ?, ?, ?, " +
                       "(SELECT idSupplier FROM decentbuy3.Supplier WHERE SupplierName = ? LIMIT 1), " +
                       "(SELECT idProducts FROM decentbuy3.Products WHERE productName = ? LIMIT 1), " +
                       "(SELECT idCustomer FROM decentbuy3.Customer WHERE customerFirst = ? AND customerLast = ? LIMIT 1), " +
                       "?, ?)";

    try (PreparedStatement pstmt = dbConn.prepareStatement(insertSQL)) {
        // Set placeholders for the main table values
        pstmt.setInt(1, id);
        pstmt.setString(2, date);
        pstmt.setInt(3, quantity);
        pstmt.setDouble(4, totalCost);

        // Set placeholders for the subqueries
        pstmt.setString(5, supplierName); // SupplierName for Supplier_idSupplier
        pstmt.setString(6, productName); // productName for Products_idProducts
        pstmt.setString(7, customerFirst); // customerFirst for Customer_idCustomer
        pstmt.setString(8, customerLast); // customerLast for Customer_idCustomer

        // Set placeholders for order status and type
        pstmt.setString(9, status);
        pstmt.setString(10, type);

        // Execute the query
        int rowsInserted = pstmt.executeUpdate();
        if (rowsInserted > 0) {
            System.out.println("Order added successfully.");
        } else {
            System.out.println("Failed to add the order. Check foreign key references.");
        }
    }
}

        
    public static void main(String[] args) {
        try {
            new DecentBuyFrame();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}