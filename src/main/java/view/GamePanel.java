package view;

import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.beans.PropertyChangeListener;
import javax.swing.JPanel;

/** Draws terrain, enemies, the wizard, projectiles and map errors from display state. */
public class GamePanel extends JPanel {
    private final GameViewModel viewModel;
    private final PlayerSprite playerSprite;
    private final MapLayer mapLayer;
    private final EnemyWaveLayer enemyWaveLayer;
    private final ProjectileLayer projectileLayer;
    private final PropertyChangeListener mapListener = event -> repaint();

    /**
     * Creates a panel with the terrain, player, enemy and spell visuals supplied by the app.
     *
     * @param viewModel non-null player display state
     * @param playerSprite non-null player animation renderer
     * @param mapLayer non-null terrain layer, drawn behind game objects
     * @param enemyWaveLayer non-null enemy and wave-heading layer
     * @param projectileLayer non-null flight, impact and casting-feedback layer
     * @param width preferred panel width in pixels
     * @param height preferred panel height in pixels
     */
    public GamePanel(GameViewModel viewModel, PlayerSprite playerSprite, MapLayer mapLayer,
                     EnemyWaveLayer enemyWaveLayer, ProjectileLayer projectileLayer,
                     int width, int height) {
        this.viewModel = viewModel;
        this.playerSprite = playerSprite;
        this.mapLayer = mapLayer;
        this.enemyWaveLayer = enemyWaveLayer;
        this.projectileLayer = projectileLayer;
        // Swing can coalesce these requests with the game loop's repaint requests.
        enemyWaveLayer.whenStateChanges(() -> repaint());
        projectileLayer.whenStateChanges(() -> repaint());
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
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // Keep pixel art sharp when it is scaled up.
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        mapLayer.draw(g2);
        GameState state = viewModel.getState();

        enemyWaveLayer.paint(g2);
        playerSprite.draw(g2, state);
        projectileLayer.paint(g2, getHeight());
        mapLayer.drawError(g2, getWidth());
    }
}
