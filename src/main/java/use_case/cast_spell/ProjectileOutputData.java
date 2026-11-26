package use_case.cast_spell;

/**
 * Immutable world-space values describing one projectile after a simulation tick.
 */
public final class ProjectileOutputData {
    private final String spellId;
    private final String projectileTypeId;
    private final double positionX;
    private final double positionY;
    private final double width;
    private final double height;
    private final double velocityX;
    private final double velocityY;

    /**
     * Creates a projectile snapshot.
     *
     * @param spellId the spell identifier
     * @param positionX the left edge in world pixels
     * @param positionY the top edge in world pixels
     * @param width the hitbox width in pixels
     * @param height the hitbox height in pixels
     * @param velocityX horizontal displacement per tick
     * @param velocityY vertical displacement per tick
     */
    public ProjectileOutputData(String spellId, double positionX, double positionY,
                                double width, double height,
                                double velocityX, double velocityY) {
        this(spellId, spellId, positionX, positionY, width, height, velocityX, velocityY);
    }

    /**
     * Creates values with separate spell identity and domain projectile type.
     *
     * @param spellId originating spell
     * @param projectileTypeId domain projectile type
     * @param positionX left edge
     * @param positionY top edge
     * @param width hitbox width
     * @param height hitbox height
     * @param velocityX fixed horizontal velocity
     * @param velocityY fixed vertical velocity
     */
    public ProjectileOutputData(String spellId, String projectileTypeId,
                                double positionX, double positionY,
                                double width, double height,
                                double velocityX, double velocityY) {
        this.spellId = spellId;
        this.projectileTypeId = projectileTypeId;
        this.positionX = positionX;
        this.positionY = positionY;
        this.width = width;
        this.height = height;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    /** Returns the domain projectile type used for renderer registration. */
    public String getProjectileTypeId() {
        return projectileTypeId;
    }

    /** Returns the spell identifier. */
    public String getSpellId() {
        return spellId;
    }

    /** Returns the left edge in world pixels. */
    public double getX() {
        return positionX;
    }

    /** Returns the top edge in world pixels. */
    public double getY() {
        return positionY;
    }

    /** Returns the hitbox width in pixels. */
    public double getWidth() {
        return width;
    }

    /** Returns the hitbox height in pixels. */
    public double getHeight() {
        return height;
    }

    /** Returns the horizontal displacement per tick. */
    public double getVelocityX() {
        return velocityX;
    }

    /** Returns the vertical displacement per tick. */
    public double getVelocityY() {
        return velocityY;
    }
}