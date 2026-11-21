package view;

import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import java.awt.Graphics2D;

/** Describes how a spell's flight and impact visuals are drawn. */
public interface ProjectileSprite {

    /**
     * Draws one flight frame without changing gameplay state or the caller's graphics settings.
     *
     * @param graphics the drawing surface
     * @param projectile the projectile geometry and orientation
     * @param animationTick the non-negative simulation tick used for the flight loop
     */
    void drawFlight(Graphics2D graphics, ProjectileDisplayData projectile, long animationTick);

    /**
     * Draws one impact frame without changing the caller's graphics settings.
     *
     * @param graphics the drawing surface
     * @param impact the fixed impact location
     * @param frameIndex a zero-based index smaller than the impact frame count
     * @throws IndexOutOfBoundsException if frameIndex is outside that range
     */
    void drawImpact(Graphics2D graphics, ProjectileImpactDisplayData impact, int frameIndex);

    /** @return the positive number of frames in one impact animation */
    int getImpactFrameCount();
}