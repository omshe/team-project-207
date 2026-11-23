package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.enemy_wave.EnemyDisplayData;
import interface_adapter.enemy_wave.EnemyWaveState;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/** Tests the F3 hitbox overlay: off by default, outlines living enemies exactly. */
class HitboxOverlayTest {
    private static final int CYAN = Color.CYAN.getRGB();

    private EnemyWaveLayer layerShowing(EnemyDisplayData... enemies) {
        EnemyWaveViewModel viewModel = new EnemyWaveViewModel();
        viewModel.setState(new EnemyWaveState("Wave 1", "", List.of(enemies), 1));
        return new EnemyWaveLayer(viewModel, new SlimeSprite());
    }

    private static BufferedImage paint(EnemyWaveLayer layer) {
        BufferedImage canvas = new BufferedImage(300, 300, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        layer.paint(graphics);
        graphics.dispose();
        return canvas;
    }

    @Test
    void theOverlayStartsOffAndDrawsNoOutline() {
        EnemyWaveLayer layer = layerShowing(
                new EnemyDisplayData(100, 100, 56, 34, false, false, false, 0));
        assertFalse(layer.isHitboxOverlayShown());
        assertTrue(CYAN != paint(layer).getRGB(100, 100));
    }

    @Test
    void theOverlayOutlinesTheHitboxsOuterPixels() {
        EnemyWaveLayer layer = layerShowing(
                new EnemyDisplayData(100, 100, 56, 34, false, false, false, 0));
        layer.toggleHitboxOverlay();
        BufferedImage canvas = paint(layer);
        assertEquals(CYAN, canvas.getRGB(100, 100));
        assertEquals(CYAN, canvas.getRGB(155, 133));
        assertTrue(CYAN != canvas.getRGB(156, 133), "outline must not spill past the box");
        assertTrue(CYAN != canvas.getRGB(155, 134), "outline must not spill past the box");
    }

    @Test
    void defeatedEnemiesGetNoOutline() {
        EnemyWaveLayer layer = layerShowing(
                new EnemyDisplayData(100, 100, 56, 34, false, false, true, 3));
        layer.toggleHitboxOverlay();
        assertTrue(CYAN != paint(layer).getRGB(100, 100));
    }

    @Test
    void f3TogglesTheOverlayAndOtherKeysDoNot() {
        EnemyWaveLayer layer = layerShowing();
        HitboxOverlayKey key = new HitboxOverlayKey(layer);
        JPanel source = new JPanel();

        key.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_W, 'w'));
        assertFalse(layer.isHitboxOverlayShown());
        key.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_F3,
                KeyEvent.CHAR_UNDEFINED));
        assertTrue(layer.isHitboxOverlayShown());
        key.keyPressed(new KeyEvent(source, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_F3,
                KeyEvent.CHAR_UNDEFINED));
        assertFalse(layer.isHitboxOverlayShown());
    }
}
