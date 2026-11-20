package use_case.cast_spell;

/**
 * Immutable world-space values describing one projectile after a simulation tick.
 */
public final class ProjectileOutputData {
    private final String spellId;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final double velocityX;
    private final double velocityY;

    /**
     * Creates a projectile snapshot.
     *
     * @param spellId the spell identifier
     * @param x the left edge in world pixels
     * @param y the top edge in world pixels
     * @param width the hitbox width in pixels
     * @param height the hitbox height in pixels
     * @param velocityX horizontal displacement per tick
     * @param velocityY vertical displacement per tick
     */
    public ProjectileOutputData(String spellId, double x, double y,
                                double width, double height,
                                double velocityX, double velocityY) {
        this.spellId = spellId;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    /** @return the spell identifier */
    public String getSpellId() {
        return spellId;
    }

    /** @return the left edge in world pixels */
    public double getX() {
        return x;
    }

    /** @return the top edge in world pixels */
    public double getY() {
        return y;
    }

    /** @return the hitbox width in pixels */
    public double getWidth() {
        return width;
    }

    /** @return the hitbox height in pixels */
    public double getHeight() {
        return height;
    }

    /** @return horizontal displacement per tick */
    public double getVelocityX() {
        return velocityX;
    }

    /** @return vertical displacement per tick */
    public double getVelocityY() {
        return velocityY;
    }
}