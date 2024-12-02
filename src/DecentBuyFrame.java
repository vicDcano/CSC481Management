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
        String[] searchOptions = {"idProducts", "ProductsName", "ProductsCategory", "ProductsBrand", "ProductsPrice", "ProductsStock"};
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
                "UPDATE Products AS i " +
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
            "UPDATE PendingOrders SET processed = TRUE WHERE processed = FALSE";
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
        refreshOrdersButton.addActionListener(e -> {
            try {
                DBDB_OrderData.loadPendingOrdersData(dbConn, ordersTable);
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
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(null);
        dialog.setModal(true);

        // Create input fields
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JTextField idDBOField = new JTextField(20);
        JTextField DBO_Quantity = new JTextField(20);
        JTextField DBO_Cost = new JTextField(20);
        JTextField DBO_Type = new JTextField(20);
        JTextField DBO_Date = new JTextField(20);


        panel.add(new JLabel("Order ID:"));
        panel.add(idDBOField);
        panel.add(new JLabel("Date: (YYYY-MM-DD)"));
        panel.add(DBO_Date);
        panel.add(new JLabel("Quantity:"));
        panel.add(DBO_Quantity);
        panel.add(new JLabel("Type:"));
        panel.add(DBO_Type);
  

        JButton submitButton = new JButton("Submit");
        JButton cancelButton = new JButton("Cancel");

        // Add action listener to Submit button
        submitButton.addActionListener(e -> {
            String id = idDBOField.getText();
            String formattedDate = DBO_Date.getText();
            String stock = DBO_Quantity.getText();
            String cost = DBO_Cost.getText();
            String type = DBO_Type.getText();

            if (id.isEmpty() || stock.isEmpty() || cost.isEmpty() || type.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "All fields must be filled!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int quantity = Integer.parseInt(stock);
                //addOrderToDatabase(dbConn, id, date, stock, cost, type);
                DBDB_OrderData.loadPendingOrdersData(dbConn, ordersTable); // Refresh the table
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Quantity must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Failed to add order. Check logs for details.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> dialog.dispose());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);

        dialog.add(panel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
/*
    private void addOrderToDatabase(Connection dbConn, String id, String date, int stock, String cost, String type) throws SQLException {
        String selectSQL = "SELECT price, quantity FROM Products WHERE ProductsName = ?";
        double price = 0.0;
        int availableQuantity = 0;

        try (PreparedStatement selectStmt = dbConn.prepareStatement(selectSQL)) {
            selectStmt.setString(1, itemName);
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                price = rs.getDouble("price");
                availableQuantity = rs.getInt("quantity");

                if (availableQuantity < quantity) {
                    throw new SQLException("Insufficient inventory for item: " + itemName);
                }
            } else {
                throw new SQLException("Item not found in inventory: " + itemName);
            }
        }

        double totalPrice = price * quantity;
        String insertOrderSQL = "INSERT INTO DBOrders (idDBOrders, DBOrderDate, DBOrderQuantity, "  
                              + "DBOrderTotal, DBOrderStatus, DBOrderType) " 
                              + "VALUES (?,?,?,?,'Pending',? , FALSE)";
        try (PreparedStatement insertStmt = dbConn.prepareStatement(insertOrderSQL)) {
            insertStmt.setString(1, firstName);
            insertStmt.setString(2, lastName);
            insertStmt.setString(3, itemName);
            insertStmt.setInt(4, quantity);
            insertStmt.setDouble(5, totalPrice);
            insertStmt.executeUpdate();
            System.out.println("Order added successfully.");
        }
    }
 */

 //delete the comment bellow to access the cancelled and coomplete orders
 /*  
    public JPanel createCanceledOrdersPanel(Connection dbConn) {
        JPanel canceledPanel = new JPanel(new BorderLayout());
        JTable canceledTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(canceledTable);
        canceledPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshCanceledButton = new JButton("Refresh Canceled Orders");
        refreshCanceledButton.addActionListener(e -> {
            try {
                DBDB_OrderData.loadCanceledOrdersData(dbConn, canceledTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshCanceledButton);
        canceledPanel.add(buttonPanel, BorderLayout.SOUTH);

        return canceledPanel;
    }

    public JPanel createCompletedOrdersPanel(Connection dbConn) {
        JPanel completedPanel = new JPanel(new BorderLayout());
        JTable completedTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(completedTable);
        completedPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshCompletedButton = new JButton("Refresh Completed Orders");
        refreshCompletedButton.addActionListener(e -> {
            try {
                DBDB_OrderData.loadCompletedOrdersData(dbConn, completedTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(refreshCompletedButton);
        completedPanel.add(buttonPanel, BorderLayout.SOUTH);

        return completedPanel;
    }

    */

    public static void main(String[] args) {
        try {
            new DecentBuyFrame();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}