package entity;

/**
 * A basic enemy (mob) that chases a target and stops when it reaches it.
 *
 * <p>An enemy has a position (top-left corner of its hitbox, in pixels), a fixed hitbox size
 * and a speed in <b>pixels per tick</b>. It knows nothing about sprites, the screen or input;
 * it only records whether it moved on its last tick and which horizontal way it last moved, so
 * outer layers can choose an animation.
 *
 * <p><b>Pursuit contract</b> ({@link #pursue(Hitbox)}): each call moves the enemy at most
 * {@code speed} pixels in a straight line toward the target's centre. The step length is the
 * same in every direction, so diagonal chasing is not faster. The enemy never moves past the
 * point where its hitbox first touches the target (see {@link Hitbox#touches(Hitbox)}), so it
 * cannot overshoot even when the remaining gap is smaller than one step. While touching, it
 * stays still; once the target moves away, the next call resumes the chase.
 */
public class Enemy {
    private double left;
    private double top;
    private final double width;
    private final double height;
    private final double speed;
    private boolean moving;
    private boolean facingLeft;

    /**
     * Creates an enemy that starts still and facing right.
     *
     * @param left starting left edge, in pixels
     * @param top starting top edge, in pixels
     * @param width hitbox width, in pixels; must be positive
     * @param height hitbox height, in pixels; must be positive
     * @param speed largest distance moved per tick, in pixels; must not be negative
     * @throws IllegalArgumentException if the size is not positive or the speed is negative
     */
    public Enemy(double left, double top, double width, double height, double speed) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Enemy size must be positive, got " + width + " x " + height);
        }
        if (speed < 0) {
            throw new IllegalArgumentException("Enemy speed must not be negative, got " + speed);
        }
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    /**
     * Moves one tick toward the target, stopping at first contact (see the class contract).
     *
     * <p>Afterwards {@link #isMoving()} reports whether the enemy actually moved this tick, and
     * {@link #isFacingLeft()} reports the horizontal direction of its latest movement.
     *
     * @param target the hitbox to chase, normally the player's, at its position for this tick
     */
    public void pursue(Hitbox target) {
        moving = false;
        Hitbox current = getHitbox();
        double deltaX = target.getCenterX() - current.getCenterX();
        double deltaY = target.getCenterY() - current.getCenterY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Already in contact: stay put. Zero distance (coincident centres) is checked
        // explicitly so the division below can never divide by zero.
        if (distance == 0 || current.touches(target)) {
            return;
        }

        // Unit vector from this enemy's centre to the target's centre.
        double directionX = deltaX / distance;
        double directionY = deltaY / distance;

        double stepLength = Math.min(speed, distanceUntilContact(current, target,
                directionX, directionY));
        if (stepLength <= 0) {
            return;
        }

        left = left + directionX * stepLength;
        top = top + directionY * stepLength;
        moving = true;
        updateFacing(directionX);
    }

    /**
     * Returns how far this enemy can travel along the direction before first touching the
     * target.
     *
     * <p>Moving toward the target's centre shrinks the gap on each axis at a constant rate
     * (the absolute value of that axis' direction component). The boxes touch once
     * <em>both</em> gaps reach zero, so contact happens at the later of the two per-axis times.
     * An axis whose gap is already closed contributes zero.
     */
    private static double distanceUntilContact(Hitbox current, Hitbox target,
                                               double directionX, double directionY) {
        double gapX = current.horizontalGapTo(target);
        double gapY = current.verticalGapTo(target);

        double distanceToCloseX = 0;
        if (gapX > 0) {
            // A positive gap means the centres differ on this axis, so directionX is not zero.
            distanceToCloseX = gapX / Math.abs(directionX);
        }
        double distanceToCloseY = 0;
        if (gapY > 0) {
            distanceToCloseY = gapY / Math.abs(directionY);
        }
        return Math.max(distanceToCloseX, distanceToCloseY);
    }

    /** Remembers the horizontal direction of the latest movement; purely vertical moves keep it. */
    private void updateFacing(double directionX) {
        if (directionX < 0) {
            facingLeft = true;
        } else if (directionX > 0) {
            facingLeft = false;
        }
    }

    /**
     * Returns this enemy's current hitbox.
     *
     * @return a new immutable hitbox at the current position
     */
    public Hitbox getHitbox() {
        return new Hitbox(left, top, width, height);
    }

    /**
     * Returns the left edge.
     *
     * @return left edge, in pixels
     */
    public double getX() {
        return left;
    }

    /**
     * Returns the top edge.
     *
     * @return top edge, in pixels
     */
    public double getY() {
        return top;
    }

    /**
     * Returns the hitbox width.
     *
     * @return width, in pixels
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the hitbox height.
     *
     * @return height, in pixels
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the speed.
     *
     * @return largest distance moved per tick, in pixels
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns whether the enemy moved during its latest {@link #pursue(Hitbox)} call.
     *
     * @return {@code true} if it moved; {@code false} if it was touching the target or has
     *     not pursued yet
     */
    public boolean isMoving() {
        return moving;
    }

    /**
     * Returns whether the enemy's latest horizontal movement was to the left.
     *
     * @return {@code true} if facing left; {@code false} if facing right (the initial facing)
     */
    public boolean isFacingLeft() {
        return facingLeft;
    }
}
