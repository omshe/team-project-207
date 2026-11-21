package view;

import javax.swing.JFrame;

/** The application window. Only holds the game panel. */
public class GameWindow extends JFrame {
    /**
     * Creates a fixed-size, centred window that closes the program when closed.
     *
     * @param panel the game panel to show
     */
    public GameWindow(GamePanel panel) {
        super("SpellBook");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        add(panel);
        pack();
        setLocationRelativeTo(null);
    }
}
