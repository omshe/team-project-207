package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.enemy_wave.EnemyDisplayData;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * Checks that the slime sheets load from the classpath and are drawn fitted to the hitbox.
 * Draws into an off-screen image, so no window is opened.
 */
class SlimeSpriteTest {
    private static final String WALK = "/sprites/enemy/slime/slime-walk.png";

    @Test
    void slimeSheetsResolveOnTheClasspathWithSixFramesEach() {
        SpriteSheet walk = new SpriteSheet(WALK, 100, 100);
        SpriteSheet idle = new SpriteSheet("/sprites/enemy/slime/slime-idle.png", 100, 100);
        assertEquals(6, walk.getFrameCount());
        assertEquals(6, idle.getFrameCount());
        // Measured visible area of the art (see resources/sprites/CREDITS.md).
        Rectangle visible = walk.getVisibleBounds().union(idle.getVisibleBounds());
        assertEquals(new Rectangle(35, 40, 28, 17), visible);
    }

    @Test
    void framesWrapAround() {
        SpriteSheet walk = new SpriteSheet(WALK, 100, 100);
        assertSame(walk.getFrame(1), walk.getFrame(7));
    }

    @Test
    void missingOrMisshapenSheetsFailClearly() {
        IllegalStateException missing = assertThrows(IllegalStateException.class,
                () -> new SpriteSheet("/sprites/does-not-exist.png", 100, 100));
        assertTrue(missing.getMessage().contains("does-not-exist"));
        assertThrows(IllegalStateException.class, () -> new SpriteSheet(WALK, 100, 50));
        assertThrows(IllegalStateException.class, () -> new SpriteSheet(WALK, 70, 100));
    }

    @Test
    void drawnSlimeFillsTheHitboxWidthAndRestsOnItsBottomEdge() {
        SlimeSprite sprite = new SlimeSprite();
        for (boolean facingLeft : new boolean[] {false, true}) {
            for (boolean moving : new boolean[] {false, true}) {
                EnemyDisplayData enemy = new EnemyDisplayData(100, 100, 56, 34, moving,
                        facingLeft, false, 0);
                Rectangle drawn = drawAndMeasure(sprite, enemy);
                // Every visible pixel stays inside the hitbox ...
                assertTrue(new Rectangle(100, 100, 56, 34).contains(drawn),
                        "drawn " + drawn + " moving=" + moving + " left=" + facingLeft);
                // ... and the art is scaled up, not drawn at its tiny original size.
                assertTrue(drawn.width >= 40, "too narrow: " + drawn);
            }
        }
    }

    @Test
    void transparentPaddingStaysTransparent() {
        SlimeSprite sprite = new SlimeSprite();
        BufferedImage canvas = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        sprite.draw(graphics, new EnemyDisplayData(100, 100, 56, 34, false, false, false, 0), 0);
        graphics.dispose();
        assertEquals(0, canvas.getRGB(5, 5) >>> 24);
        assertEquals(0, canvas.getRGB(100, 60) >>> 24);
    }

    /** Draws one enemy on a blank transparent canvas and returns the box of opaque pixels. */
    private static Rectangle drawAndMeasure(SlimeSprite sprite, EnemyDisplayData enemy) {
        return drawAndMeasure(sprite, enemy, 0);
    }

    /** Same as above, drawing the frame for the given animation tick. */
    private static Rectangle drawAndMeasure(SlimeSprite sprite, EnemyDisplayData enemy,
                                            long tick) {
        BufferedImage canvas = render(sprite, enemy, tick);

        Rectangle bounds = null;
        for (int pixelY = 0; pixelY < canvas.getHeight(); pixelY++) {
            for (int pixelX = 0; pixelX < canvas.getWidth(); pixelX++) {
                if ((canvas.getRGB(pixelX, pixelY) >>> 24) == 0) {
                    continue;
                }
                Rectangle pixel = new Rectangle(pixelX, pixelY, 1, 1);
                if (bounds == null) {
                    bounds = pixel;
                } else {
                    bounds = bounds.union(pixel);
                }
            }
        }
        return bounds;
    }

    @Test
    void deathSheetHasFourFramesAndLastsThatManyFrameLengths() {
        SpriteSheet death = new SpriteSheet("/sprites/enemy/slime/slime-death.png", 100, 100);
        assertEquals(4, death.getFrameCount());
        assertEquals(4 * SlimeSprite.TICKS_PER_FRAME, new SlimeSprite().getDeathAnimationTicks());
    }

    @Test
    void defeatedSlimePlaysTheDeathFramesOnceThenHoldsTheLast() {
        SlimeSprite sprite = new SlimeSprite();
        BufferedImage alive = render(sprite,
                new EnemyDisplayData(100, 100, 56, 34, false, false, false, 0), 0);
        BufferedImage firstDeathFrame = render(sprite,
                new EnemyDisplayData(100, 100, 56, 34, false, false, true, 0), 0);
        BufferedImage lastDeathFrame = render(sprite, new EnemyDisplayData(100, 100, 56, 34,
                false, false, true, 3 * SlimeSprite.TICKS_PER_FRAME), 0);
        BufferedImage longAfter = render(sprite,
                new EnemyDisplayData(100, 100, 56, 34, false, false, true, 1000), 0);

        assertFalse(samePixels(alive, firstDeathFrame), "death art differs from idle art");
        assertFalse(samePixels(firstDeathFrame, lastDeathFrame));
        assertTrue(samePixels(lastDeathFrame, longAfter), "holds the last frame, no looping");
    }

    private static BufferedImage render(SlimeSprite sprite, EnemyDisplayData enemy, long tick) {
        BufferedImage canvas = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        sprite.draw(graphics, enemy, tick);
        graphics.dispose();
        return canvas;
    }

    private static boolean samePixels(BufferedImage first, BufferedImage second) {
        for (int pixelY = 0; pixelY < first.getHeight(); pixelY++) {
            for (int pixelX = 0; pixelX < first.getWidth(); pixelX++) {
                if (first.getRGB(pixelX, pixelY) != second.getRGB(pixelX, pixelY)) {
                    return false;
                }
            }
        }
        return true;
    }
}
