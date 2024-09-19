import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicBorders;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import javax.swing.JButton;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;

public class appliance_inventory_database_swing extends JFrame {

    private JPanel contentPane;
    private JTable table;
    private JTextField textField;

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    appliance_inventory_database_swing frame = new appliance_inventory_database_swing();
                    frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }

    public appliance_inventory_database_swing() {
        setFont(new Font("Dialog", Font.ITALIC, 12));
        setTitle("Appliance Store Inventory");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(250, 100, 700, 361);
        contentPane = new JPanel();
        contentPane.setBackground(new Color(0, 238, 127));
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setBounds(10, 70, 650, 217);
        contentPane.add(scrollPane);
        table = new JTable();
        table.setModel(new DefaultTableModel(
                new Object[][]{
                },
                new String[]{
                        "Product name", "Product Weight", "Product Dimensions", "Product Color", "Product Warranty", "Location"
                }
        ));
        scrollPane.setViewportView(table);
        JButton btnNewButton = new JButton("Search");
        btnNewButton.setFont(new Font("Dialog", Font.ITALIC,12));
        btnNewButton.setBackground(new Color(255, 255, 255));

        btnNewButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e) {

                String user = textField.getText().trim();
                System.out.println("User input: " + user);
                String searchQuery = "SELECT " +
                        "product.ProductName, " +
                        "product.ProductWeight, " +
                        "product.ProductDimensions, " +
                        "product.ProductColor, " +
                        "product.ProductWaranty, " +
                        "product.location " +
                        "FROM " +
                        "appliance_store_inventory.product " +
                        "WHERE " +
                        "LOWER(ProductColor) LIKE ?"; // update the user ? here; // update the user ? here

                try {
                    //Class.forName("com.mysql.jdbc.Driver");
                    Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/appliance_store_inventory", "root", "1234");
                    PreparedStatement pst = con.prepareStatement(searchQuery);

                    pst.setString(1, user.toLowerCase() + "%");   // add this line

                    //System.out.println("Am I the problem");

                    ResultSet rs = pst.executeQuery();

                    DefaultTableModel tb1Model = (DefaultTableModel) table.getModel();
                    if (tb1Model.getRowCount() > 0)
                    {
                        //System.out.println("CHecking IF statement");
                        tb1Model.setRowCount(0);
                    }

                    while (rs.next())
                    {
                        //System.out.println("CHECKING WHILE LOOP NOW");
                        String name = rs.getString("ProductName");
                        String weight = rs.getString("ProductWeight");
                        String dimension = rs.getString("ProductDimensions");
                        String color = rs.getString("ProductColor");
                        String warranty = rs.getString("ProductWaranty");
                        String location = rs.getString("location");
                        String tbData[] = {name, weight, dimension, color, warranty, location};
                        tb1Model.addRow(tbData);

                    }
                } catch (Exception ex)
                {
                    ex.printStackTrace();
                }
            }
        });

        textField = new JTextField();
        textField.setBounds(50, 30, 300, 25);
        contentPane.add(textField);
        btnNewButton.setBounds(380, 30, 80, 14);
        contentPane.add(btnNewButton);
    }
}