package view;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Checks the supplied fireball resource and off-screen sprite drawing. */
class FireballSpriteTest {
    private final FireballSprite sprite = new FireballSprite();
    private final ProjectileImpactDisplayData impact =
            new ProjectileImpactDisplayData("fireball", 100, 100);

    /** The production resource must be seven 100-pixel cells with three impact frames. */
    @Test
    void resourceHasExpectedLayout() {
        SpriteSheet sheet = new SpriteSheet(
                "/sprites/spells/fireball/fireball.png", 100, 100);

        assertEquals(7, sheet.getFrameCount());
        assertEquals(3, sprite.getImpactFrameCount());
    }

    /** Flight cycles through four frames and then repeats without entering impact frames. */
    @Test
    void flightLoopsAfterFourFrames() {
        BufferedImage first = flight(0, 0);

        assertArrayEquals(pixels(first), pixels(flight(4, 0)));
        assertArrayEquals(pixels(first), pixels(flight(20, 0)));
        assertFalse(Arrays.equals(pixels(first), pixels(flight(5, 0))));
        assertFalse(Arrays.equals(pixels(first), pixels(flight(15, 0))));
    }

    /** Rotation follows travel direction while the visible art stays centered. */
    @Test
    void flightRotatesAroundItsCenter() {
        Rectangle horizontal = bounds(flight(0, 0));
        Rectangle vertical = bounds(flight(0, Math.PI / 2));

        assertEquals(horizontal.width, vertical.height);
        assertEquals(horizontal.height, vertical.width);
        assertEquals(100, horizontal.getCenterX(), 1);
        assertEquals(100, horizontal.getCenterY(), 1);
        assertEquals(100, vertical.getCenterX(), 1);
        assertEquals(100, vertical.getCenterY(), 1);
    }

    /** All impact frames draw centered, and out-of-range indices cannot wrap into flight. */
    @Test
    void impactFramesAreDistinctAndDoNotWrap() {
        BufferedImage first = impactFrame(0);
        BufferedImage middle = impactFrame(1);
        BufferedImage last = impactFrame(2);

        assertFalse(Arrays.equals(pixels(first), pixels(middle)));
        assertFalse(Arrays.equals(pixels(middle), pixels(last)));

        Rectangle visible = bounds(last);
        assertEquals(100, visible.getCenterX(), 1);
        assertEquals(100, visible.getCenterY(), 1);

        assertThrows(IndexOutOfBoundsException.class, () -> impactFrame(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> impactFrame(3));
    }

    /** A sprite's transforms must not move later player, enemy, or text drawing. */
    @Test
    void drawingPreservesTheCallersGraphicsTransform() {
        Graphics2D graphics = canvas().createGraphics();
        try {
            graphics.translate(3, 4);
            AffineTransform before = graphics.getTransform();

            sprite.drawFlight(graphics, projectile(Math.PI / 4), 7);
            sprite.drawImpact(graphics, impact, 2);

            assertEquals(before, graphics.getTransform());
        } finally {
            graphics.dispose();
        }
    }

    /** A different sheet or missing dependency must fail clearly during construction. */
    @Test
    void invalidSheetsAreRejected() {
        assertThrows(NullPointerException.class, () -> new FireballSprite(null));
        assertThrows(IllegalStateException.class, () -> new FireballSprite(
                new SpriteSheet("/sprites/enemy/slime/slime-walk.png", 100, 100)));
    }

    /** Draws one flight frame into a transparent image. */
    private BufferedImage flight(long tick, double angle) {
        BufferedImage image = canvas();
        Graphics2D graphics = image.createGraphics();
        try {
            sprite.drawFlight(graphics, projectile(angle), tick);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    /** Draws one impact frame into a transparent image. */
    private BufferedImage impactFrame(int frame) {
        BufferedImage image = canvas();
        Graphics2D graphics = image.createGraphics();
        try {
            sprite.drawImpact(graphics, impact, frame);
        } finally {
            graphics.dispose();
        }
        return image;
    }

    /** Creates display geometry centered away from the canvas edges. */
    private static ProjectileDisplayData projectile(double angle) {
        return new ProjectileDisplayData("fireball", 100, 100, 24, 16, angle);
    }

    /** Creates an off-screen transparent drawing surface. */
    private static BufferedImage canvas() {
        return new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
    }

    /** Reads image pixels for exact animation-frame comparisons. */
    private static int[] pixels(BufferedImage image) {
        return image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0,
                image.getWidth());
    }

    /** Measures the non-transparent pixels and also checks that something was drawn. */
    private static Rectangle bounds(BufferedImage image) {
        Rectangle result = null;

        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    Rectangle pixel = new Rectangle(x, y, 1, 1);
                    if (result == null) {
                        result = pixel;
                    } else {
                        result = result.union(pixel);
                    }
                }
            }
        }

        assertNotNull(result);
        return result;
    }
}
