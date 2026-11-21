package view;

import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Draws looping fireball flight and the three-frame circular impact sequence. */
public final class FireballSprite implements ProjectileSprite {
    private static final String RESOURCE = "/sprites/spells/fireball/fireball.png";
    private static final int FRAME_SIZE = 100;
    private static final int FLIGHT_FRAMES = 4;
    private static final int IMPACT_FRAMES = 3;
    private static final int TICKS_PER_FLIGHT_FRAME = 5;
    private static final double PIXEL_SCALE = 2;

    private final List<BufferedImage> frames;

    /**
     * Loads the seven-frame PNG from the classpath.
     *
     * @throws IllegalStateException if the sheet is missing or has an unexpected layout
     */
    public FireballSprite() {
        this(new SpriteSheet(RESOURCE, FRAME_SIZE, FRAME_SIZE));
    }

    /**
     * Creates a sprite from an already loaded seven-frame sheet.
     * Transparent padding is trimmed per frame; visible art is centered when drawn.
     * Visual scale is independent of the gameplay hitbox.
     *
     * @param sheet the supplied fireball sheet
     * @throws NullPointerException if sheet is null
     * @throws IllegalStateException if it has the wrong frame count or an empty frame
     */
    public FireballSprite(SpriteSheet sheet) {
        Objects.requireNonNull(sheet);
        if (sheet.getFrameCount() != FLIGHT_FRAMES + IMPACT_FRAMES) {
            throw new IllegalStateException("Fireball sheet must contain seven frames");
        }

        List<BufferedImage> trimmed = new ArrayList<>();
        for (int i = 0; i < sheet.getFrameCount(); i++) {
            trimmed.add(trim(sheet.getFrame(i)));
        }
        frames = List.copyOf(trimmed);
    }

    /** {@inheritDoc} */
    @Override
    public void drawFlight(Graphics2D graphics, ProjectileDisplayData projectile,
                           long animationTick) {
        int index = (int) ((animationTick / TICKS_PER_FLIGHT_FRAME) % FLIGHT_FRAMES);
        draw(graphics, frames.get(index), projectile.getCenterX(),
                projectile.getCenterY(), projectile.getAngleRadians());
    }

    /** {@inheritDoc} */
    @Override
    public void drawImpact(Graphics2D graphics, ProjectileImpactDisplayData impact,
                           int frameIndex) {
        Objects.checkIndex(frameIndex, IMPACT_FRAMES);
        draw(graphics, frames.get(FLIGHT_FRAMES + frameIndex),
                impact.getCenterX(), impact.getCenterY(), 0);
    }

    /** {@inheritDoc} */
    @Override
    public int getImpactFrameCount() {
        return IMPACT_FRAMES;
    }

    /** Draws centered pixel art using an isolated graphics context. */
    private static void draw(Graphics2D graphics, BufferedImage frame,
                             double centerX, double centerY, double angle) {
        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            copy.translate(centerX, centerY);
            copy.rotate(angle);
            copy.scale(PIXEL_SCALE, PIXEL_SCALE);
            copy.translate(-frame.getWidth() / 2.0, -frame.getHeight() / 2.0);
            copy.drawImage(frame, 0, 0, null);
        } finally {
            copy.dispose();
        }
    }

    /** Removes transparent margins without changing the source resource. */
    private static BufferedImage trim(BufferedImage frame) {
        Rectangle bounds = null;
        for (int y = 0; y < frame.getHeight(); y++) {
            for (int x = 0; x < frame.getWidth(); x++) {
                if ((frame.getRGB(x, y) >>> 24) != 0) {
                    Rectangle pixel = new Rectangle(x, y, 1, 1);
                    if (bounds == null) {
                        bounds = pixel;
                    } else {
                        bounds = bounds.union(pixel);
                    }
                }
            }
        }

        if (bounds == null) {
            throw new IllegalStateException("Fireball sheet contains an empty frame");
        }

        return frame.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}
