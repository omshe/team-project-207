package entity;

import java.util.Objects;

/** Immutable identity and flight settings for one kind of projectile. */
public final class ProjectileDefinition {
    private final Spell spell;
    private final String projectileTypeId;
    private final double speed;
    private final double width;
    private final double height;

    /**
     * Creates validated projectile settings, independent of visual dimensions.
     *
     * @param spell originating spell
     * @param projectileTypeId non-blank domain identifier, not an image path
     * @param speed positive finite pixels per tick
     * @param width positive finite hitbox width
     * @param height positive finite hitbox height
     * @throws NullPointerException if an identity is null
     * @throws IllegalArgumentException if an identifier is blank or settings are invalid
     */
    public ProjectileDefinition(Spell spell, String projectileTypeId,
                                double speed, double width, double height) {
        this.spell = Objects.requireNonNull(spell);
        this.projectileTypeId = Objects.requireNonNull(projectileTypeId);
        if (projectileTypeId.isBlank() || !Double.isFinite(speed) || speed <= 0
                || !Double.isFinite(width) || width <= 0
                || !Double.isFinite(height) || height <= 0) {
            throw new IllegalArgumentException("Projectile settings must be positive and finite");
        }
        this.speed = speed;
        this.width = width;
        this.height = height;
    }

    /** Returns the originating spell. */
    public Spell getSpell() {
        return spell;
    }

    /** Returns the domain projectile type. */
    public String getProjectileTypeId() {
        return projectileTypeId;
    }

    /** Returns the configured speed in pixels per tick. */
    public double getSpeed() {
        return speed;
    }

    /** Returns the hitbox width. */
    public double getWidth() {
        return width;
    }

    /** Returns the hitbox height. */
    public double getHeight() {
        return height;
    }
}
