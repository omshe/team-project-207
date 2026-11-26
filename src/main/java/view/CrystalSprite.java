package view;

import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.Objects;

/** Draws a crystal or shard using fixed sheet origins, preserving the approved artwork. */
public final class CrystalSprite implements ProjectileSprite {
    private static final int FRAME_SIZE = 100;
    private static final int FRAME_COUNT = 4;
    private static final int TICKS_PER_FRAME = 5;
    private final SpriteSheet flight;
    private final SpriteSheet impact;

    /**
     * Loads a pair of transparent four-frame strips from the classpath.
     *
     * @param flightResource absolute resource path of the flight strip
     * @param impactResource absolute resource path of the impact strip
     */
    public CrystalSprite(String flightResource, String impactResource) {
        this(new SpriteSheet(flightResource, FRAME_SIZE, FRAME_SIZE),
                new SpriteSheet(impactResource, FRAME_SIZE, FRAME_SIZE));
    }

    /**
     * Receives loaded sheets, useful for off-screen rendering tests.
     *
     * @param flight non-null four-frame flight sheet
     * @param impact non-null four-frame impact sheet
     * @throws IllegalStateException if either sheet has a different frame count
     */
    public CrystalSprite(SpriteSheet flight, SpriteSheet impact) {
        this.flight = Objects.requireNonNull(flight);
        this.impact = Objects.requireNonNull(impact);
        if (flight.getFrameCount() != FRAME_COUNT || impact.getFrameCount() != FRAME_COUNT) {
            throw new IllegalStateException("Crystal strips must have four frames");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void drawFlight(Graphics2D graphics, ProjectileDisplayData projectile,
                           long animationTick) {
        long frame = animationTick / TICKS_PER_FRAME;
        draw(graphics, flight, frame, projectile.getCenterX(), projectile.getCenterY(),
                projectile.getAngleRadians(), 2);
    }

    /** {@inheritDoc} */
    @Override
    public void drawImpact(Graphics2D graphics, ProjectileImpactDisplayData event, int frameIndex) {
        Objects.checkIndex(frameIndex, FRAME_COUNT);
        draw(graphics, impact, frameIndex, event.getCenterX(), event.getCenterY(), 0, 3);
    }

    /** {@inheritDoc} */
    @Override
    public int getImpactFrameCount() {
        return FRAME_COUNT;
    }

    /** Renders around the fixed (50, 50) origin without changing the supplied graphics state. */
    private static void draw(Graphics2D graphics, SpriteSheet sheet, long frame,
                             double centerX, double centerY, double angle, double scale) {
        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            copy.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            copy.translate(centerX, centerY);
            copy.rotate(angle);
            copy.scale(scale, scale);
            copy.drawImage(sheet.getFrame(frame), -FRAME_SIZE / 2, -FRAME_SIZE / 2, null);
        } finally {
            copy.dispose();
        }
    }
}
