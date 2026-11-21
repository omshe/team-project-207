package entity;

import java.util.Objects;

/**
 * A moving spell projectile with a position, fixed velocity, and rectangular hitbox.
 */
public final class Projectile {

    private final Spell spell;
    private final double velocityX;
    private final double velocityY;
    private final double width;
    private final double height;

    private double positionX;
    private double positionY;

    /**
     * Creates a projectile at the given top-left position.
     *
     * @param spell the spell represented by this projectile
     * @param positionX the initial left edge in world pixels
     * @param positionY the initial top edge in world pixels
     * @param velocityX horizontal movement in pixels per tick
     * @param velocityY vertical movement in pixels per tick
     * @param width the positive, finite hitbox width in pixels
     * @param height the positive, finite hitbox height in pixels
     * @throws NullPointerException if spell is null
     * @throws IllegalArgumentException if position or velocity is not finite,
     *                                  or either dimension is not positive and finite
     */
    public Projectile(Spell spell, double positionX, double positionY,
                      double velocityX, double velocityY,
                      double width, double height) {
        this.spell = Objects.requireNonNull(spell, "Spell must not be null");

        if (!Double.isFinite(positionX) || !Double.isFinite(positionY)) {
            throw new IllegalArgumentException("Position must be finite");
        }
        if (!Double.isFinite(velocityX) || !Double.isFinite(velocityY)) {
            throw new IllegalArgumentException("Velocity must be finite");
        }
        if (!Double.isFinite(width) || !Double.isFinite(height)
                || width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Projectile dimensions must be positive and finite");
        }

        this.positionX = positionX;
        this.positionY = positionY;
        this.velocityX = velocityX;
        this.velocityY = velocityY;
        this.width = width;
        this.height = height;
    }

    /**
     * Advances the projectile by its fixed velocity for one game tick.
     */
    public void move() {
        positionX += velocityX;
        positionY += velocityY;
    }

    /**
     * Returns the spell represented by this projectile.
     *
     * @return the associated spell
     */
    public Spell getSpell() {
        return spell;
    }

    /**
     * Returns the projectile's left edge.
     *
     * @return the horizontal position in world pixels
     */
    public double getX() {
        return positionX;
    }

    /**
     * Returns the projectile's top edge.
     *
     * @return the vertical position in world pixels
     */
    public double getY() {
        return positionY;
    }

    /**
     * Returns a hitbox at the projectile's current position.
     *
     * @return a new immutable hitbox with this projectile's position and dimensions
     */
    public Hitbox getHitbox() {
        return new Hitbox(positionX, positionY, width, height);
    }
}
