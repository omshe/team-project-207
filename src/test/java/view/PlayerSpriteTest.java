package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.game.GameState;
import interface_adapter.game.PlayerAnimation;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/**
 * Checks where the wizard is drawn relative to the player's box. Draws into an off-screen
 * image, so no window is opened.
 */
class PlayerSpriteTest {
    private static final int BOX_X = 200;
    private static final int BOX_Y = 200;
    private static final int BOX_SIZE = 48;

    @Test
    void feetRestOnTheBottomOfTheBoxAndTheBodyIsCentred() {
        for (PlayerAnimation animation : PlayerAnimation.values()) {
            for (boolean facingLeft : new boolean[] {false, true}) {
                Rectangle drawn = opaqueBounds(draw(animation, facingLeft));
                String label = animation + " left=" + facingLeft;

                assertEquals(BOX_Y + BOX_SIZE, drawn.y + drawn.height, "feet " + label);
                double boxCenterX = BOX_X + BOX_SIZE / 2.0;
                double drawnCenterX = drawn.x + drawn.width / 2.0;
                assertEquals(boxCenterX, drawnCenterX, 1.0, "centre " + label);
            }
        }
    }

    @Test
    void artIsScaledUpThreeTimes() {
        Rectangle drawn = opaqueBounds(draw(PlayerAnimation.IDLE, false));

        // The wizard's body is 17 sheet pixels wide (x 43..59).
        assertEquals(17 * 3, drawn.width);
    }

    @Test
    void facingLeftMirrorsTheFrameInPlace() {
        BufferedImage right = draw(PlayerAnimation.IDLE, false);
        BufferedImage left = draw(PlayerAnimation.IDLE, true);
        Rectangle bounds = opaqueBounds(right);

        // Turning around does not shift the body ...
        assertEquals(bounds, opaqueBounds(left));
        // ... and every pixel lands on the opposite side of the body.
        boolean anyDifferent = false;
        for (int y = bounds.y; y < bounds.y + bounds.height; y++) {
            for (int offset = 0; offset < bounds.width; offset++) {
                int rightPixel = right.getRGB(bounds.x + offset, y);
                int mirroredLeftPixel = left.getRGB(bounds.x + bounds.width - 1 - offset, y);
                assertEquals(rightPixel, mirroredLeftPixel, "pixel row " + y + " offset " + offset);
                if (rightPixel != left.getRGB(bounds.x + offset, y)) {
                    anyDifferent = true;
                }
            }
        }
        assertTrue(anyDifferent, "the art is symmetric, so this test cannot see the flip");
    }

    private static BufferedImage draw(PlayerAnimation animation, boolean facingLeft) {
        BufferedImage canvas = new BufferedImage(500, 500, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = canvas.createGraphics();
        PlayerSprite.wizard().draw(graphics,
                new GameState(BOX_X, BOX_Y, BOX_SIZE, animation, facingLeft, 0));
        graphics.dispose();
        return canvas;
    }

    /** Returns the smallest box containing every non-transparent pixel. */
    private static Rectangle opaqueBounds(BufferedImage canvas) {
        Rectangle bounds = null;
        for (int y = 0; y < canvas.getHeight(); y++) {
            for (int x = 0; x < canvas.getWidth(); x++) {
                if ((canvas.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                Rectangle pixel = new Rectangle(x, y, 1, 1);
                if (bounds == null) {
                    bounds = pixel;
                } else {
                    bounds = bounds.union(pixel);
                }
            }
        }
        return bounds;
    }
}
