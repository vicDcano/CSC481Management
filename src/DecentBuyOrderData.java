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
                           "    DBOrder.idDBOrder, DBOrder.DBOrderDate, DBOrder.DBOrderQuantity, DBOrder.DBOrderTotalCost, DBOrderStatus," +
                           "    Products.idProducts, Products.productName, Products.productPrice " +
                           "FROM DBOrder " +
                           "JOIN Customer ON DBOrder.Customer_idCustomer = Customer.idCustomer " +
                           "JOIN Products ON DBOrder.Products_idProducts = Products.idProducts;";
    
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);
    
        // Define column headers
        String[] columnNames = {"Customer ID", "Customer First", "Customer Last", 
                                "Order ID", "Order Date", "Order Quantity", "Order Total Cost",
                                "Product ID", "Product Name", "Product Price", "Order Status"};
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
            String o_status = rs.getString("DBOrderStatus");
    
            int p_ID = rs.getInt("idProducts");
            String p_Name = rs.getString("productName");
            double p_Price = rs.getDouble("productPrice");

    
            // Add row to the table model
            Object[] row = {c_ID, c_First, c_Last, o_Id, o_Date, o_Quantity, o_Cost, o_status, p_ID, p_Name, p_Price};
            model.addRow(row);
        }
    
        // Set the model for the JTable
        table.setModel(model);
    }

    public void searchBarInventory(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException {
        // Allowed search criteria
        Set<String> allowedCriteria = Set.of("idProducts", "productName", "productCategory", "productBrand", "productPrice", "productStock");

        // Debugging: Print the selected search criterion
        System.out.println("Selected search criterion: " + searchCriterion);

        if (!allowedCriteria.contains(searchCriterion)) {
            throw new IllegalArgumentException("Invalid Search Criterion: " + searchCriterion);
        }

        String searchColumn = searchCriterion;  // Use the passed criterion directly

        String searchSQL = "SELECT * FROM Products WHERE " + searchColumn + " LIKE ?";
        try (PreparedStatement pstmt = dbConn.prepareStatement(searchSQL)) {
            pstmt.setString(1, "%" + searchInput + "%");  // Use wildcards for partial matches
            try (ResultSet rs = pstmt.executeQuery()) {
                String[] columnNames = {"Product ID", "Product Name", "Category", "Brand", "Price", "Quantity"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                while (rs.next()) {
                    int ID = rs.getInt("idProducts");
                    String itemName = rs.getString("productName");
                    String category = rs.getString("productCategory");
                    String brand = rs.getString("productBrand");
                    String price = String.format("$%.2f", rs.getDouble("productPrice"));
                    String quantity = String.valueOf(rs.getInt("productStock"));

                    Object[] row = {ID, itemName, category, brand, price, quantity};
                    model.addRow(row);
                }

                table.setModel(model);
            }
        }
    }

    public void searchBarOrder(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException {
        // Map user-friendly search criteria to database columns
        Map<String, String> searchMapping = Map.of(
                "Order ID", "idDBOrder",
                "Order Date", "DBOrderDate",
                "Customer First Name", "customerFirst",
                "Customer Last Name", "customerLast",
                "Order Status", "DBOrderStatus",
                "Product Name", "productName"
        );

        // Check if the search criterion is valid
        if (!searchMapping.containsKey(searchCriterion)) {
            throw new IllegalArgumentException("Invalid Search Criterion: " + searchCriterion);
        }

        // Base query setup
        String baseQuery = """
        SELECT 
            DBOrder.idDBOrder AS 'Order ID', 
            DBOrder.DBOrderDate AS 'Order Date', 
            DBOrder.DBOrderStatus AS 'Order Status',
            Customer.idCustomer AS 'Customer ID',
            Customer.customerFirst AS 'Customer First',
            Customer.customerLast AS 'Customer Last',
            Products.productName AS 'Product Name'
        FROM DBOrder
        LEFT JOIN Customer ON DBOrder.Customer_idCustomer = Customer.idCustomer
        LEFT JOIN Products ON DBOrder.Products_idProducts = Products.idProducts
    """;

        // Identify the database column to search
        String searchColumn = searchMapping.get(searchCriterion);

        // Add WHERE clause for the search
        String searchQuery = baseQuery + " WHERE " + searchColumn + " LIKE ?";

        try (PreparedStatement pstmt = dbConn.prepareStatement(searchQuery)) {
            pstmt.setString(1, "%" + searchInput + "%");

            try (ResultSet rs = pstmt.executeQuery()) {
                // Define the columns to display
                String[] columnNames = {"Order ID", "Order Date", "Order Status", "Customer ID", "Customer First", "Customer Last", "Product Name"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                // Populate table rows
                while (rs.next()) {
                    Object[] row = new Object[columnNames.length];
                    row[0] = rs.getInt("Order ID");
                    row[1] = rs.getString("Order Date");
                    row[2] = rs.getString("Order Status");
                    row[3] = rs.getInt("Customer ID");
                    row[4] = rs.getString("Customer First");
                    row[5] = rs.getString("Customer Last");
                    row[6] = rs.getString("Product Name");
                    model.addRow(row);
                }

                // Update table with the new model
                table.setModel(model);
            }
        }
    }


    public void updateProductInDatabase(Connection dbConn, Object id, String name, String category,
                                        String brand, double price, int stock) throws SQLException {
        String updateSQL = "UPDATE Products SET productName = ?, productCategory = ?, productBrand = ?, " +
                "productPrice = ?, productStock = ? WHERE idProducts = ?";

        try (PreparedStatement pstmt = dbConn.prepareStatement(updateSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, category);
            pstmt.setString(3, brand);
            pstmt.setDouble(4, price);
            pstmt.setInt(5, stock);
            pstmt.setObject(6, id);

            int rowsUpdated = pstmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Product updated successfully.");
            } else {
                System.out.println("No product was updated.");
            }
        }
    }

}