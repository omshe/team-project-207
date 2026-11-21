package view;

import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

/** Draws the current GameState. Only reads; never changes the game. */
public class GamePanel extends JPanel {
    private final GameViewModel viewModel;
    private final PlayerSprite playerSprite;
    private final EnemyWaveLayer enemyWaveLayer;

    /**
     * Creates the panel.
     *
     * @param viewModel      the player's view model to read when painting
     * @param playerSprite   how to draw the player
     * @param enemyWaveLayer draws the mobs and wave heading
     * @param width          preferred width, in pixels
     * @param height         preferred height, in pixels
     */
    public GamePanel(GameViewModel viewModel, PlayerSprite playerSprite, EnemyWaveLayer enemyWaveLayer,
                     int width, int height) {
        this.viewModel = viewModel;
        this.playerSprite = playerSprite;
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
        // Keep pixel art sharp when it is scaled up.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        GameState state = viewModel.getState();

        enemyWaveLayer.paint(g2);   // mobs and wave heading (Generate Mob Waves)

        playerSprite.draw(g2, state);
    }
}
