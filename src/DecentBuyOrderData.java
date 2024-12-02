import javax.swing.*;
import javax.swing.table.*;
//import com.mysql.cj.x.protobuf.MysqlxCrud.Order;

//import java.awt.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDate;

public class DecentBuyOrderData {
    LocalDate currentDate = LocalDate.now();

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
        String selectSQL = "SELECT * FROM Products";
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        // extract data from result set
        String[] columnNames = {"ID", "Name", "Category", "Brand", "Price", "Stock"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        while (rs.next()) {
            int ID = rs.getInt("idProducts");
            String itemName = rs.getString("productName");
            String category = rs.getString("productCategory");
            String brand = rs.getString("productBrand");
            //double price = rs.getDouble("ProductsPrice");
            String Price = String.format("$%.2f", rs.getDouble("productPrice"));

            int quantity = rs.getInt("productStock");
            Object[] row = {ID, itemName, category, brand, Price, quantity};
            model.addRow(row);
        }

        // set model to table
        table.setModel(model);
    }

    public void loadOrdersData(Connection dbConn, JTable table) throws SQLException {
        String selectSQL = "SELECT " +
                           "    Customer.idCustomer, Customer.customerFirst, Customer.customerLast, " +
                           "    DBOrder.idDBOrder, DBOrder.DBOrderDate, DBOrder.DBOrderQuantity, DBOrder.DBOrderTotalCost, " +
                           "    Products.idProducts, Products.productName, Products.productPrice " +
                           "FROM DBOrder " +
                           "JOIN Customer ON DBOrder.Customer_idCustomer = Customer.idCustomer " +
                           "JOIN Products ON DBOrder.Products_idProducts = Products.idProducts;";
    
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);
    
        // Define column headers
        String[] columnNames = {"Customer ID", "Customer First", "Customer Last", 
                                "Order ID", "Order Date", "Order Quantity", "Order Total Cost", 
                                "Product ID", "Product Name", "Product Price"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
    
        // Process the result set
        while (rs.next()) {
            int c_ID = rs.getInt("idCustomer");
            String c_First = rs.getString("customerFirst");
            String c_Last = rs.getString("customerLast");
    
            int o_Id = rs.getInt("idDBOrder");
            String o_Date = rs.getString("DBOrderDate");
            int o_Quantity = rs.getInt("DBOrderQuantity");
            double o_Cost = rs.getDouble("DBOrderTotalCost");
    
            int p_ID = rs.getInt("idProducts");
            String p_Name = rs.getString("productName");
            double p_Price = rs.getDouble("productPrice");
    
            // Add row to the table model
            Object[] row = {c_ID, c_First, c_Last, o_Id, o_Date, o_Quantity, o_Cost, p_ID, p_Name, p_Price};
            model.addRow(row);
        }
    
        // Set the model for the JTable
        table.setModel(model);
    }
    
    public void searchBarInventory(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException
    {
        Set<String> alloweCriteria = Set.of("idProducts", "productName", "productCategory",
                                            "productBrand", "productPrice", "productStock");
        if(!alloweCriteria.contains(searchCriterion)) {
            throw new IllegalArgumentException("Invalid Search Criteria");
        }
        String searchColumn = searchCriterion;  // Default search by item name

        // SQL query with parameterized search
        String searchSQL = "SELECT * FROM Products WHERE " + searchColumn + " LIKE ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(searchSQL)) {
            pstmt.setString(1, "%" + searchInput + "%");  // Use wildcards for partial matches
            try (ResultSet rs = pstmt.executeQuery()) {
                // Extract data and update the table model
                String[] columnNames = {"idProducts", "productName", "productCategory", "productBrand", "productPrice", "productStock"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                while (rs.next()) {
                    int ID = rs.getInt("idProducts");
                    String itemName = rs.getString("productName");
                    String category = rs.getString("productCategory");
                    String brand = rs.getString("productBrand");
                    //double price = rs.getDouble("ProductsPrice");
                    String Price = String.format("$%.2f", rs.getDouble("productPrice"));

                    String quantity = String.valueOf(rs.getInt("productStock"));
                    Object[] row = {ID, itemName, category, brand, Price, quantity};
                    model.addRow(row);
                }

                // set model to table
                table.setModel(model);
            }
        }
    }


    public void orderSearchBar(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException
    {
        // SQL query with parameterized search
        String searchSQL = "SELECT * FROM DBOrder where DBOrderStatus like %";
        try (PreparedStatement pstmt = dbConn.prepareStatement(searchSQL)) {
            pstmt.setString(1, "%" + searchInput + "%");  // Use wildcards for partial matches
            ResultSet rs = pstmt.executeQuery();

            // Extract data and update the table model
            String[] columnNames = {"Order ID", "Order Date", "Order Quantity", "Order Total", "Order Status", "Order Type"};
            DefaultTableModel model = new DefaultTableModel(columnNames, 0);

             while (rs.next()) {
            int orderId = rs.getInt("IdDBOrder");
            String OrderDate = rs.getString("DBOrderDate");
            String OrderQuantity = rs.getString("DBOrderQuantity");
            String TotalCost = rs.getString("DBOrderTotalCost");
            String status = rs.getString("DBOrderStatus");
            String type = rs.getString("DBOrderType");
            Object[] row = {orderId, OrderDate, OrderQuantity, TotalCost, status, type};
            model.addRow(row);
        }

        // set model to table
        table.setModel(model);
        }
    }



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