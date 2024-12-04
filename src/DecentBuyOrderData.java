import javax.swing.*;
import javax.swing.table.*;
//import com.mysql.cj.x.protobuf.MysqlxCrud.Order;

//import java.awt.*;
import java.sql.*;
import java.util.*;
import java.time.LocalDate;

public class DecentBuyOrderData {
    LocalDate currentDate = LocalDate.now();

    public void addProduct(Connection dbConn, String name, String category, String brand, double price, int stock) throws SQLException {
        String insertSQL = "INSERT INTO Products (productName, productDesc, productPrice, productStock, productBrand, productCategory) VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = dbConn.prepareStatement(insertSQL)) {
            pstmt.setString(1, name);
            pstmt.setString(2, ""); // Assuming productDesc is optional or can be added later
            pstmt.setDouble(3, price);
            pstmt.setInt(4, stock);
            pstmt.setString(5, brand);
            pstmt.setString(6, category);

            int rowsInserted = pstmt.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("Product added successfully.");
            } else {
                System.out.println("Failed to add the product.");
            }
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
                           "    DBOrder.idDBOrder, CONCAT(Customer.customerFirst, ' ', Customer.customerLast) AS 'Customer Name', " +
                           "    DBOrder.DBOrderDate, DBOrder.DBOrderQuantity," +
                           "    Products.productName, Products.productPrice, DBOrder.DBOrderTotalCost, DBOrderStatus " +
                           "FROM DBOrder " +
                           "JOIN Customer ON DBOrder.Customer_idCustomer = Customer.idCustomer " +
                           "JOIN Products ON DBOrder.Products_idProducts = Products.idProducts;";
    
        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);
    
        // Define column headers
        String[] columnNames = {"Order ID", "Customer Name",
                                "Order Date", "Order Quantity",
                                "Product Name", "Product Price", "Order Total Cost", "Order Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);
    
        // Process the result set
        while (rs.next()) {
            int o_Id = rs.getInt("idDBOrder");
            String c_First = rs.getString("Customer Name");
    

            String o_Date = rs.getString("DBOrderDate");
            int o_Quantity = rs.getInt("DBOrderQuantity");


            String p_Name = rs.getString("productName");
            double p_Price = rs.getDouble("productPrice");
            double o_Cost = rs.getDouble("DBOrderTotalCost");
            String o_status = rs.getString("DBOrderStatus");

    
            // Add row to the table model
            Object[] row = {o_Id, c_First, o_Date, o_Quantity, p_Name, p_Price, o_Cost, o_status};
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
                "Customer First Name", "customerFirst",
                "Customer Last Name", "customerLast",
                "Order Date", "DBOrderDate",
                "Product Name", "productName",
                "Order Status", "DBOrderStatus"
        );

        // Check if the search criterion is valid
        if (!searchMapping.containsKey(searchCriterion)) {
            throw new IllegalArgumentException("Invalid Search Criterion: " + searchCriterion);
        }

        // Base query setup
        String baseQuery = """
        SELECT 
            DBOrder.idDBOrder AS 'Order ID',
            CONCAT(Customer.customerFirst, ' ', Customer.customerLast) AS 'Customer Name',
            DBOrder.DBOrderDate AS 'Order Date',  
            DBOrder.DBOrderQuantity AS 'Quantity',
            Products.productName AS 'Product Name',
            Products.productPrice AS 'Price',
            DBOrder.DBOrderTotalCost AS 'Total Cost',
            DBOrder.DBOrderStatus AS 'Order Status'
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
                String[] columnNames = {"Order ID", "Customer Name", "Order Date", "Quantity", "Product Name", "Price", "Total Cost", "Order Status"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                // Populate table rows
                while (rs.next()) {
                    Object[] row = new Object[columnNames.length];
                    row[0] = rs.getInt("Order ID");
                    row[2] = rs.getInt("Customer Name");
                    row[1] = rs.getString("Order Date");
                    row[3] = rs.getInt("Quantity");
                    row[4] = rs.getInt("Product Name");
                    row[5] = rs.getInt("Price");
                    row[6] = rs.getInt("Total Price");
                    row[7] = rs.getInt("Status");

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