//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JScrollPane;
//import javax.swing.JTabbedPane;
import javax.swing.JTable;
//import javax.swing.JComboBox;
//import javax.swing.JTextField;
//import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;
import java.sql.PreparedStatement;

//import java.awt.BorderLayout;

import java.sql.Connection;
//import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;


public class DecentBuyOrderData {

    public void generateRandomOrder(Connection dbConn) throws SQLException {
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

    public void loadInventoryData(Connection dbConn, JTable table) throws SQLException {
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

    public void loadPendingOrdersData(Connection dbConn, JTable table) throws SQLException {
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

    public void loadCanceledOrdersData(Connection dbConn, JTable table) throws SQLException
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

    public void loadCompletedOrdersData(Connection dbConn, JTable table) throws SQLException
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

    public void searchBarInventory(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException
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

   // private static void searchBarCustomerOrder(Connection dbConn, JTable table) throws SQLException
//    {

  //  }


    public void savePendingOrder(Connection dbConn, JTable ordersTable) throws SQLException {
        DefaultTableModel model = (DefaultTableModel) ordersTable.getModel();

        for (int row = 0; row < model.getRowCount(); row++) {
            int orderId = (int) model.getValueAt(row, 0); // Assuming order_id is the first column
            String firstName = (String) model.getValueAt(row, 1);
            String lastName = (String) model.getValueAt(row, 2);
            String itemName = (String) model.getValueAt(row, 3);
            int quantity = (int) model.getValueAt(row, 4);
            double price = Double.parseDouble(model.getValueAt(row, 5).toString().replace("$", ""));
            String status = (String) model.getValueAt(row, 6);

            // Check if the order exists and should be updated
            String checkSQL = "SELECT COUNT(*) FROM PendingOrders WHERE order_id = ?";
            try (PreparedStatement checkStmt = dbConn.prepareStatement(checkSQL)) {
                checkStmt.setInt(1, orderId);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                int exists = rs.getInt(1);

                if (exists > 0) { // Order exists, so update it
                    String updateSQL = "UPDATE PendingOrders SET first_name = ?, last_name = ?, item_name = ?, quantity = ?, price = ?, status = ? WHERE order_id = ?";
                    try (PreparedStatement pstmt = dbConn.prepareStatement(updateSQL)) {
                        pstmt.setString(1, firstName);
                        pstmt.setString(2, lastName);
                        pstmt.setString(3, itemName);
                        pstmt.setInt(4, quantity);
                        pstmt.setDouble(5, price);
                        pstmt.setString(6, status);
                        pstmt.setInt(7, orderId);
                        pstmt.executeUpdate();
                    }
                } else { // Order does not exist, insert it as new
                    String insertSQL = "INSERT INTO PendingOrders (first_name, last_name, item_name, quantity, price, status) VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement pstmt = dbConn.prepareStatement(insertSQL)) {
                        pstmt.setString(1, firstName);
                        pstmt.setString(2, lastName);
                        pstmt.setString(3, itemName);
                        pstmt.setInt(4, quantity);
                        pstmt.setDouble(5, price);
                        pstmt.setString(6, status);
                        pstmt.executeUpdate();
                    }
                }
            }
        }
    }




}
