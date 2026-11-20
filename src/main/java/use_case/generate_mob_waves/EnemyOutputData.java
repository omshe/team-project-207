package use_case.generate_mob_waves;

/**
 * Immutable copy of one enemy's values at the end of a tick.
 *
 * <p>It exists so that the live, mutable {@code Enemy} entities never leave the use-case layer.
 * Positions stay as {@code double}; rounding for display is the presenter's job.
 */
public class EnemyOutputData {
    private final double left;
    private final double top;
    private final double width;
    private final double height;
    private final boolean moving;
    private final boolean facingLeft;

    /**
     * Creates the enemy's output values.
     *
     * @param left left edge, in pixels
     * @param top top edge, in pixels
     * @param width hitbox width, in pixels
     * @param height hitbox height, in pixels
     * @param moving whether the enemy moved this tick
     * @param facingLeft whether the enemy's latest horizontal movement was to the left
     */
    public EnemyOutputData(double left, double top, double width, double height,
                           boolean moving, boolean facingLeft) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.moving = moving;
        this.facingLeft = facingLeft;
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
     * Returns whether the enemy moved this tick.
     *
     * @return {@code true} if it moved
     */
    public boolean isMoving() {
        return moving;
    }

    /**
     * Returns whether the enemy faces left.
     *
     * @return {@code true} if its latest horizontal movement was to the left
     */
    public boolean isFacingLeft() {
        return facingLeft;
    }
}
