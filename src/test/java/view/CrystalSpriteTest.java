package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import org.junit.jupiter.api.Test;

/** Checks approved sheets, playback bounds and isolated off-screen rendering. */
class CrystalSpriteTest {
    private static final String ROOT = "/sprites/spells/crystal_burst/";

    @Test
    void approvedCoreAndShardStripsRenderEveryFrameAndKeepGraphicsState() {
        for (String kind : new String[] {"core", "shard"}) {
            String impactName = kind + "-impact";
            if (kind.equals("core")) {
                impactName = "core-shatter";
            }
            CrystalSprite sprite = new CrystalSprite(ROOT + "crystal-" + kind + "-flight.png",
                    ROOT + "crystal-" + impactName + ".png");
            assertEquals(4, sprite.getImpactFrameCount());
            for (int frame = 0; frame < 4; frame++) {
                BufferedImage image = new BufferedImage(260, 260, BufferedImage.TYPE_INT_ARGB);
                Graphics2D graphics = image.createGraphics();
                try {
                    AffineTransform before = graphics.getTransform();
                    sprite.drawFlight(graphics, new ProjectileDisplayData("crystal_burst",
                            "crystal_" + kind, 130, 130, 8, 8, Math.PI / 2), frame * 5);
                    assertEquals(before, graphics.getTransform());
                    assertTrue(visiblePixels(image) > 10);
                    sprite.drawImpact(graphics, new ProjectileImpactDisplayData(
                            "crystal_burst", "crystal_" + kind, 130, 130), frame);
                    assertEquals(before, graphics.getTransform());
                    assertThrows(IndexOutOfBoundsException.class,
                            () -> sprite.drawImpact(graphics,
                                    new ProjectileImpactDisplayData("crystal_burst", 130, 130), 4));
                } finally {
                    graphics.dispose();
                }
            }
        }
    }

    private static int visiblePixels(BufferedImage image) {
        int count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) != 0) {
                    count++;
                }
            }
        }
        return count;
    }
}
