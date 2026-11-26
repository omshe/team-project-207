package entity;

import java.util.Objects;

/** Immutable request to create one fixed-velocity projectile at a world centre. */
public final class ProjectileSpawn {
    private final ProjectileDefinition definition;
    private final double centerX;
    private final double centerY;
    private final double velocityX;
    private final double velocityY;

    /**
     * Creates a validated spawn description without touching session storage.
     *
     * @param definition identity and dimensions
     * @param centerX finite birth centre X
     * @param centerY finite birth centre Y
     * @param velocityX finite fixed horizontal velocity
     * @param velocityY finite fixed vertical velocity
     */
    public ProjectileSpawn(ProjectileDefinition definition, double centerX, double centerY,
                           double velocityX, double velocityY) {
        this.definition = Objects.requireNonNull(definition);
        if (!Double.isFinite(centerX) || !Double.isFinite(centerY)
                || !Double.isFinite(velocityX) || !Double.isFinite(velocityY)) {
            throw new IllegalArgumentException("Spawn position and velocity must be finite");
        }
        this.centerX = centerX;
        this.centerY = centerY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
    }

    /** Returns a new independent projectile; the spawn description remains unchanged. */
    public Projectile createProjectile() {
        return new Projectile(definition, centerX - definition.getWidth() / 2,
                centerY - definition.getHeight() / 2, velocityX, velocityY);
    }
}
