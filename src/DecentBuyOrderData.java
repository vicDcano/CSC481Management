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
        String selectSQL = """
        SELECT 
            DBOrder.idDBOrder, 
            IFNULL(CONCAT(Customer.customerFirst, ' ', Customer.customerLast), Supplier.supplierName) AS 'Name',
            DBOrder.DBOrderDate, 
            DBOrder.DBOrderQuantity,
            Products.productName, 
            Products.productPrice, 
            DBOrder.DBOrderTotalCost, 
            DBOrder.DBOrderStatus 
        FROM DBOrder 
        LEFT JOIN Customer ON DBOrder.Customer_idCustomer = Customer.idCustomer 
        LEFT JOIN Supplier ON DBOrder.Supplier_idSupplier = Supplier.idSupplier 
        LEFT JOIN Products ON DBOrder.Products_idProducts = Products.idProducts;
    """;

        ResultSet rs = dbConn.createStatement().executeQuery(selectSQL);

        // Define column headers
        String[] columnNames = {"Order ID", "Name", "Order Date", "Order Quantity", "Product Name", "Product Price", "Order Total Cost", "Order Status"};
        DefaultTableModel model = new DefaultTableModel(columnNames, 0);

        // Process the result set
        while (rs.next()) {
            int orderId = rs.getInt("idDBOrder");
            String name = rs.getString("Name");
            String orderDate = rs.getString("DBOrderDate");
            int orderQuantity = rs.getInt("DBOrderQuantity");
            String productName = rs.getString("productName");
            double productPrice = rs.getDouble("productPrice");
            double orderCost = rs.getDouble("DBOrderTotalCost");
            String orderStatus = rs.getString("DBOrderStatus");

            // Add row to the table model
            Object[] row = {orderId, name, orderDate, orderQuantity, productName, productPrice, orderCost, orderStatus};
            model.addRow(row);
        }

        // Set the model for the JTable
        table.setModel(model);
    }


    public void searchBarInventory(Connection dbConn, JTable table, String searchCriterion, String searchInput) throws SQLException {
        // Allowed search criteria
        Set<String> allowedCriteria = Set.of("idProducts", "productName", "productCategory",
                                            "productBrand", "productPrice", "productStock");

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
        Map<String, String> searchMapping = Map.of(
                "Order ID", "idDBOrder",
                "Customer First Name", "customerFirst",
                "Customer Last Name", "customerLast",
                "Order Date", "DBOrderDate",
                "Product Name", "productName",
                "Order Status", "DBOrderStatus",
                "Supplier", "supplierName"  // New entry for supplier
        );

        // Check if the search criterion is valid
        if (!searchMapping.containsKey(searchCriterion)) {
            throw new IllegalArgumentException("Invalid Search Criterion: " + searchCriterion);
        }

        // Base query setup
        String baseQuery = """
        SELECT 
            DBOrder.idDBOrder AS 'Order ID',
            CONCAT(Customer.customerFirst, ' ', Customer.customerLast) AS 'Name',
            DBOrder.DBOrderDate AS 'Order Date',  
            DBOrder.DBOrderQuantity AS 'Order Quantity',
            Products.productName AS 'Product Name',
            Products.productPrice AS 'Product Price',
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
                String[] columnNames = {"Order ID", "Name", "Order Date", "Order Quantity", "Product Name", " Product Price", "Total Cost", "Order Status"};
                DefaultTableModel model = new DefaultTableModel(columnNames, 0);

                // Populate table rows
                while (rs.next()) {
                    Object[] row = new Object[columnNames.length];
                    row[0] = rs.getInt("Order ID");
                    row[1] = rs.getString("Name");
                    row[2] = rs.getString("Order Date");
                    row[3] = rs.getInt("Order Quantity");
                    row[4] = rs.getString("Product Name");
                    row[5] = rs.getDouble("Product Price");
                    row[6] = rs.getDouble("Total Cost");
                    row[7] = rs.getString("Order Status");

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

    public void saveEditedOrder(Connection dbConn, int orderId, String firstName, String lastName, String orderDate,
                                int quantity, String productName, double productPrice, double totalCost, String orderStatus) throws SQLException {
        // SQL queries
        String updateCustomerSQL = "UPDATE Customer " +
                "SET customerFirst = ?, customerLast = ? " +
                "WHERE idCustomer = (SELECT Customer_idCustomer FROM DBOrder WHERE idDBOrder = ?)";

        String updateOrderSQL = "UPDATE DBOrder SET " +
                "DBOrderDate = ?, DBOrderQuantity = ?, DBOrderTotalCost = ?, DBOrderStatus = ? " +
                "WHERE idDBOrder = ?";

        String updateProductSQL = "UPDATE Products " +
                "SET productName = ?, productPrice = ? " +
                "WHERE idProducts = (SELECT Products_idProducts FROM DBOrder WHERE idDBOrder = ?)";

        try {
            // Begin transaction
            dbConn.setAutoCommit(false);

            // Update customer details
            try (PreparedStatement psCustomer = dbConn.prepareStatement(updateCustomerSQL)) {
                psCustomer.setString(1, firstName);  // Update first name
                psCustomer.setString(2, lastName);   // Update last name
                psCustomer.setInt(3, orderId);       // Update only the customer linked to the specific order
                psCustomer.executeUpdate();
            }

            // Update order details
            try (PreparedStatement psOrder = dbConn.prepareStatement(updateOrderSQL)) {
                psOrder.setString(1, orderDate);
                psOrder.setInt(2, quantity);
                psOrder.setDouble(3, totalCost);
                psOrder.setString(4, orderStatus);
                psOrder.setInt(5, orderId);          // Update the specific order
                psOrder.executeUpdate();
            }

            // Update product details
            try (PreparedStatement psProduct = dbConn.prepareStatement(updateProductSQL)) {
                psProduct.setString(1, productName);
                psProduct.setDouble(2, productPrice);
                psProduct.setInt(3, orderId);        // Update the product associated with the specific order
                psProduct.executeUpdate();
            }

            // Commit transaction
            dbConn.commit();
        } catch (SQLException ex) {
            dbConn.rollback(); // Rollback if an error occurs
            throw ex;
        } finally {
            dbConn.setAutoCommit(true);
        }
    }


}