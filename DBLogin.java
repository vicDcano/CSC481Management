import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;


public class DBLogin extends JFrame {

    // Database connection details
    DatabaseConn dbConn = new DatabaseConn();

    private final Color backgroundColor = new Color(40, 42, 54); // dracula bg
    private final Color primaryColor = new Color(0, 122, 204);   // Blue
    
    public DBLogin() {
        setTitle("DecentBuy Inventory Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(backgroundColor);

        // Panel for the form
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        formPanel.setBackground(backgroundColor);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Username Label and Field
        JLabel usernameLabel = new JLabel("Username:");
        JTextField usernameField = new JTextField(15);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        formPanel.add(usernameField, gbc);

        // Password Label and Field
        JLabel passwordLabel = new JLabel("Password:");
        JPasswordField passwordField = new JPasswordField(15);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        formPanel.add(passwordField, gbc);

        // Login Button
        JButton loginButton = new JButton("Login");
        loginButton.setBackground(primaryColor);
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        formPanel.add(loginButton, gbc);

        // Add hover effect
        loginButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(primaryColor.darker());
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginButton.setBackground(primaryColor);
            }
        });

        // Status Label
        JLabel statusLabel = new JLabel("", JLabel.CENTER);
        statusLabel.setForeground(Color.RED);
        add(statusLabel, BorderLayout.SOUTH);

        add(formPanel, BorderLayout.CENTER);


        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());
                if (authenticateUser(username, password)) {
                    statusLabel.setForeground(Color.GREEN);
                    statusLabel.setText("Login Successful!");

                    // Close the login page
                    dispose();
                    
                    // Open the main system window
                    try {
                        new DecentBuyFrame();
                    } catch (Exception er) {
                        er.printStackTrace();
                    }
                } else {
                    statusLabel.setForeground(Color.RED);
                    statusLabel.setText("Invalid username or password.");
                }
            }
        });

        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }



    private boolean authenticateUser(String username, String password) {
        try (Connection connection = dbConn.getConnection()) {
            String query = "SELECT * FROM User WHERE username = ? AND userpassword = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next(); // Returns true if a user is found
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Incorrect login: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }


  //if you want to run this file to make any changes
  // you can remove this comment to run the main

    public static void main(String[] args) {
        SwingUtilities.invokeLater(DBLogin::new);
    }
        


}