import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JComboBox;
import javax.swing.JTextField;
//import javax.swing.SwingUtilities;
//import javax.swing.table.DefaultTableModel;
//import java.sql.PreparedStatement;

import java.awt.BorderLayout;

import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.ResultSet;
import java.sql.SQLException;
//import java.sql.Statement;

public class DecentBuyFrame {
    DecentBuyOrderData DBDB_OrderData = new DecentBuyOrderData();
    
    public void dbFrame(Connection dbConn) {
        JFrame frame = new JFrame("DecentBuy Inventory Management");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.add(createTabbedPane(dbConn));
        frame.setVisible(true);
    }

    public JTabbedPane createTabbedPane(Connection dbConn) {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Inventory", createInventoryPanel(dbConn));
        tabbedPane.addTab("Pending Orders", createOrdersPanel(dbConn));
        tabbedPane.addTab("Canceled Orders", createCanceledOrdersPanel(dbConn));
        tabbedPane.addTab("Completed Orders", createCompletedOrdersPanel(dbConn));
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
        String[] searchOptions = {"Item Name", "Category"};
        JComboBox<String> searchDropdown = new JComboBox<>(searchOptions);
        JTextField searchTextField = new JTextField(15);
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(_ -> {
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
        refreshButton.addActionListener(_ -> {
            try {
                DBDB_OrderData.loadInventoryData(dbConn, table);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(refreshButton);
        return buttonPanel;
    }

    public JPanel createOrdersPanel(Connection dbConn) {
        JPanel ordersPanel = new JPanel(new BorderLayout());
        JTable ordersTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(ordersTable);
        ordersPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton refreshOrdersButton = new JButton("Refresh Orders");
        refreshOrdersButton.addActionListener(_ -> {
            try {
                DBDB_OrderData.loadPendingOrdersData(dbConn, ordersTable);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(refreshOrdersButton);

        JButton generateOrderButton = new JButton("Generate Order");
        generateOrderButton.addActionListener(_ -> {
            try {
                DBDB_OrderData.generateRandomOrder(dbConn);
                DBDB_OrderData.loadPendingOrdersData(dbConn, ordersTable);
                DBDB_OrderData.loadInventoryData(dbConn, new JTable()); // Assuming reloading inventory after order generation
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        buttonPanel.add(generateOrderButton);

        ordersPanel.add(buttonPanel, BorderLayout.SOUTH);
        return ordersPanel;
    }

    public JPanel createCanceledOrdersPanel(Connection dbConn) {
        JPanel canceledPanel = new JPanel(new BorderLayout());
        JTable canceledTable = new JTable();
        JScrollPane scrollPane = new JScrollPane(canceledTable);
        canceledPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshCanceledButton = new JButton("Refresh Canceled Orders");
        refreshCanceledButton.addActionListener(_ -> {
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
        refreshCompletedButton.addActionListener(_ -> {
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

}
