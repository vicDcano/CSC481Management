import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import java.awt.*;
import java.sql.*;

public class Add_Order_or_Product extends JFrame {
    DecentBuyFrame orderStock = new DecentBuyFrame();
    DecentBuyFrame customerOrder = new DecentBuyFrame();
    DatabaseConn connection= new DatabaseConn();

    public Add_Order_or_Product() throws SQLException {
        Connection dbConn = connection.getConnection();
        JTable table = new JTable();
        
        setTitle("Reordering Page");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Welcome label
        JLabel welcomeLabel = new JLabel("Hello", JLabel.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        add(welcomeLabel, BorderLayout.NORTH);

        // Main system features (example buttons)
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 10, 10));

        JButton productButton = new JButton("Product");
        JButton SupplierButton = new JButton("Supplier");
        JButton logoutButton = new JButton("Logout");
        JButton exitButton = new JButton("Exit");

        buttonPanel.add(productButton);
        buttonPanel.add(SupplierButton);
        buttonPanel.add(logoutButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.CENTER);

        SupplierButton.addActionListener(e -> {
            orderStock.openAddOrderDialog(dbConn, table);
        });

        productButton.addActionListener(e -> {
            
        });



        // Logout button action
        logoutButton.addActionListener(e -> {
            dispose(); // Close the main system window
            new DBLogin(); // Return to the login page
        });

        // Exit button action
        exitButton.addActionListener(e -> System.exit(0));

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }
}

