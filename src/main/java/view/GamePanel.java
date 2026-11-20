package view;

import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

/** Draws the current GameState. Only reads; never changes the game. */
public class GamePanel extends JPanel {
    private final GameViewModel viewModel;
    private final EnemyWaveLayer enemyWaveLayer;

    public GamePanel(GameViewModel viewModel, EnemyWaveLayer enemyWaveLayer,
                     int width, int height) {
        this.viewModel = viewModel;
        this.enemyWaveLayer = enemyWaveLayer;
        // Repaint when the mob view model changes. Swing merges this with GameLoop's own
        // repaint request for the same tick, so the panel is still painted once per frame.
        enemyWaveLayer.whenStateChanges(() -> repaint());
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   // clears the screen with the background colour
        Graphics2D g2 = (Graphics2D) g;
        GameState state = viewModel.getState();

        enemyWaveLayer.paint(g2);   // mobs and wave heading (Generate Mob Waves)

        g2.setColor(Color.WHITE);
        g2.fillRect(state.getPlayerX(), state.getPlayerY(), state.getPlayerSize(), state.getPlayerSize());
    }
}
