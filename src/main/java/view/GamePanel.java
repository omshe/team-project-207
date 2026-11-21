package view;

import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/** Draws terrain, enemies, the player and map errors using only display state. */
public class GamePanel extends JPanel {
    private final GameViewModel viewModel;
    private final MapLayer mapLayer;
    private final EnemyWaveLayer enemyWaveLayer;
    private final PropertyChangeListener mapListener = event -> repaint();

    /**
     * Creates a panel with both terrain and enemy-wave display layers.
     *
     * @param viewModel non-null player display state
     * @param width preferred panel width in pixels
     * @param height preferred panel height in pixels
     * @param mapLayer non-null terrain layer, drawn before enemies and the player
     * @param enemyWaveLayer non-null enemy and wave-heading layer
     */
    public GamePanel(GameViewModel viewModel, int width, int height,
                     MapLayer mapLayer, EnemyWaveLayer enemyWaveLayer) {
        this.viewModel = viewModel;
        this.mapLayer = mapLayer;
        this.enemyWaveLayer = enemyWaveLayer;
        // Swing can coalesce these requests with the game loop's repaint requests.
        enemyWaveLayer.whenStateChanges(() -> repaint());
        setPreferredSize(new Dimension(width, height));
        setBackground(Color.BLACK);
        setDoubleBuffered(true);
        setFocusable(true);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        mapLayer.addPropertyChangeListener(mapListener);
    }

    @Override
    public void removeNotify() {
        mapLayer.removePropertyChangeListener(mapListener);
        super.removeNotify();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);   // clears the screen with the background colour
        Graphics2D g2 = (Graphics2D) g;
        mapLayer.draw(g2);
        GameState state = viewModel.getState();

        enemyWaveLayer.paint(g2);   // mobs and wave heading (Generate Mob Waves)

        g2.setColor(Color.WHITE);
        g2.fillRect(state.getPlayerX(), state.getPlayerY(),
                state.getPlayerSize(), state.getPlayerSize());
        mapLayer.drawError(g2, getWidth());
    }
}
