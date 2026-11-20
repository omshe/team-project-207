package interface_adapter.cast_spell;

import java.util.Objects;

/** Immutable geometry and orientation used to draw one flying projectile. */
public final class ProjectileDisplayData {
    private final String spellId;
    private final double centerX;
    private final double centerY;
    private final double width;
    private final double height;
    private final double angleRadians;

    /**
     * Creates display values without choosing a sprite or animation frame.
     *
     * @param spellId the spell identifier used to select its visuals
     * @param centerX horizontal hitbox center in world pixels
     * @param centerY vertical hitbox center in world pixels
     * @param width hitbox width in pixels, independent of sprite padding
     * @param height hitbox height in pixels, independent of sprite padding
     * @param angleRadians rotation from rightward, clockwise on a downward-positive screen
     */
    public ProjectileDisplayData(String spellId, double centerX, double centerY,
                                 double width, double height, double angleRadians) {
        this.spellId = Objects.requireNonNull(spellId);
        this.centerX = centerX;
        this.centerY = centerY;
        this.width = width;
        this.height = height;
        this.angleRadians = angleRadians;
    }

    /** @return the spell identifier */
    public String getSpellId() {
        return spellId;
    }

    /** @return horizontal hitbox center in world pixels */
    public double getCenterX() {
        return centerX;
    }

    /** @return vertical hitbox center in world pixels */
    public double getCenterY() {
        return centerY;
    }

    /** @return hitbox width in pixels */
    public double getWidth() {
        return width;
    }

    /** @return hitbox height in pixels */
    public double getHeight() {
        return height;
    }

    /** @return rotation in radians from the rightward direction */
    public double getAngleRadians() {
        return angleRadians;
    }
}
