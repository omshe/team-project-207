package view;

import interface_adapter.enemy_wave.EnemyDisplayData;
import interface_adapter.enemy_wave.EnemyWaveState;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * Draws the mob-wave part of the screen: every enemy, the wave heading, and any status
 * message. {@code GamePanel} calls it while painting. It only reads the
 * {@link EnemyWaveViewModel}; it never changes the game.
 */
public class EnemyWaveLayer {
    private static final int TEXT_MARGIN = 12;
    private static final Font HEADING_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 16);
    private static final Font MESSAGE_FONT = new Font(Font.SANS_SERIF, Font.PLAIN, 13);

    private final EnemyWaveViewModel viewModel;
    private final SlimeSprite slimeSprite;

    /**
     * Creates the layer.
     *
     * @param viewModel   the mob-wave view model to read
     * @param slimeSprite how to draw each enemy
     */
    public EnemyWaveLayer(EnemyWaveViewModel viewModel, SlimeSprite slimeSprite) {
        this.viewModel = viewModel;
        this.slimeSprite = slimeSprite;
    }

    /**
     * Subscribes to the view model so the given action runs whenever a new snapshot arrives.
     *
     * @param action what to do on a change, normally a repaint request; it runs on the
     *               game-loop thread, so it must be thread-safe ({@code repaint()} is)
     */
    public void whenStateChanges(Runnable action) {
        viewModel.addPropertyChangeListener(event -> action.run());
    }

    /**
     * Paints the latest snapshot.
     *
     * @param graphics where to draw
     */
    public void paint(Graphics2D graphics) {
        // Read the volatile reference once so the whole frame uses one consistent snapshot.
        EnemyWaveState state = viewModel.getState();

        for (EnemyDisplayData enemy : state.getEnemies()) {
            slimeSprite.draw(graphics, enemy, state.getAnimationTick());
        }

        graphics.setColor(Color.WHITE);
        graphics.setFont(HEADING_FONT);
        int headingBaseline = TEXT_MARGIN + graphics.getFontMetrics().getAscent();
        graphics.drawString(state.getWaveLabel(), TEXT_MARGIN, headingBaseline);

        graphics.setColor(Color.ORANGE);
        graphics.setFont(MESSAGE_FONT);
        int messageBaseline = headingBaseline + graphics.getFontMetrics().getHeight() + 4;
        graphics.drawString(state.getStatusMessage(), TEXT_MARGIN, messageBaseline);
    }
}
