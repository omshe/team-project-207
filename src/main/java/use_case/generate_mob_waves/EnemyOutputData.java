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
    private final boolean defeated;
    private final int ticksSinceDefeat;

    /**
     * Creates the enemy's output values.
     *
     * @param left left edge, in pixels
     * @param top top edge, in pixels
     * @param width hitbox width, in pixels
     * @param height hitbox height, in pixels
     * @param moving whether the enemy moved this tick
     * @param facingLeft whether the enemy's latest horizontal movement was to the left
     * @param defeated whether the enemy has been defeated and is fading out
     * @param ticksSinceDefeat ticks since the defeat, or 0 if not defeated
     */
    public EnemyOutputData(double left, double top, double width, double height,
                           boolean moving, boolean facingLeft, boolean defeated,
                           int ticksSinceDefeat) {
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.moving = moving;
        this.facingLeft = facingLeft;
        this.defeated = defeated;
        this.ticksSinceDefeat = ticksSinceDefeat;
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

    /**
     * Returns whether the enemy has been defeated.
     *
     * @return {@code true} if it is defeated and fading out
     */
    public boolean isDefeated() {
        return defeated;
    }

    /**
     * Returns the ticks since the defeat.
     *
     * @return ticks since the defeat, or 0 if not defeated
     */
    public int getTicksSinceDefeat() {
        return ticksSinceDefeat;
    }
}
