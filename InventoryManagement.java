import javax.swing.*;
import com.formdev.flatlaf.FlatDarculaLaf;

public class InventoryManagement {
    DatabaseConn dbconn = new DatabaseConn();
    public static void main(String[] args)
    {
        try {
            UIManager.setLookAndFeel(new FlatDarculaLaf());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        try {
            SwingUtilities.invokeLater(() -> new DBLogin());
        } catch (Exception e) {
            e.printStackTrace();
        }

        }

    }
