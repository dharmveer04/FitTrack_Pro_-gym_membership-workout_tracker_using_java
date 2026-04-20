package gymtracker;

import gymtracker.gui.MainFrame;

import javax.swing.*;

/**
 * Application entry point.
 * All Swing components are created on the Event Dispatch Thread (EDT).
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) { /* fall back to default L&F */ }
            new MainFrame();
        });
    }
}
