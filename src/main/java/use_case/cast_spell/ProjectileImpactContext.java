package use_case.cast_spell;

/** Immutable collision geometry; deliberately has no enemy or storage references. */
public final class ProjectileImpactContext {
    private final double centerX;
    private final double centerY;
    private final double velocityX;
    private final double velocityY;

    /**
     * Captures a collision and the projectile's incoming fixed velocity.
     *
     * @param centerX finite collision centre X
     * @param centerY finite collision centre Y
     * @param velocityX finite horizontal incoming velocity
     * @param velocityY finite vertical incoming velocity
     */
    public ProjectileImpactContext(double centerX, double centerY,
                                   double velocityX, double velocityY) {
        if (!Double.isFinite(centerX) || !Double.isFinite(centerY)
                || !Double.isFinite(velocityX) || !Double.isFinite(velocityY)) {
            throw new IllegalArgumentException("Contact geometry must be finite");
        }
        this.centerX = centerX;
        this.centerY = centerY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    /** Returns the collision centre X. */
    public double getCenterX() {
        return centerX;
    }

    /** Returns the collision centre Y. */
    public double getCenterY() {
        return centerY;
    }

    /** Returns the fixed incoming horizontal velocity. */
    public double getVelocityX() {
        return velocityX;
    }

    /** Returns the fixed incoming vertical velocity. */
    public double getVelocityY() {
        return velocityY;
    }
}
