import javax.swing.*;

public class InventoryManagement {
    DatabaseConn dbconn = new DatabaseConn();
    public void main(String[] args)
    {
        try {
            SwingUtilities.invokeLater(() -> new DBLogin());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}