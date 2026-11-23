package view;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Turns the enemy hitbox outlines on and off when F3 is pressed. A developer aid for checking
 * that collisions match what is drawn; it does not change the game.
 */
public class HitboxOverlayKey extends KeyAdapter {
    private final EnemyWaveLayer enemyWaveLayer;

    /**
     * Creates the key listener.
     *
     * @param enemyWaveLayer the layer whose hitbox overlay F3 toggles
     */
    public HitboxOverlayKey(EnemyWaveLayer enemyWaveLayer) {
        this.enemyWaveLayer = enemyWaveLayer;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_F3) {
            enemyWaveLayer.toggleHitboxOverlay();
        }
    }
}
