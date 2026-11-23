package interface_adapter.enemy_wave;

/**
 * Immutable, display-ready values for drawing one enemy: a whole-pixel hitbox plus generic
 * movement, facing and defeat values. The view decides which sprite and animation these mean.
 */
public class EnemyDisplayData {
    private final int left;
    private final int top;
    private final int width;
    private final int height;
    private final boolean moving;
    private final boolean facingLeft;
    private final boolean defeated;
    private final int ticksSinceDefeat;

    /**
     * Creates the display values.
     *
     * @param left left edge of the hitbox, in screen pixels
     * @param top top edge of the hitbox, in screen pixels
     * @param width hitbox width, in pixels
     * @param height hitbox height, in pixels
     * @param moving whether the enemy moved this tick
     * @param facingLeft whether the enemy faces left
     * @param defeated whether the enemy is defeated and should be drawn fading out
     * @param ticksSinceDefeat ticks since the defeat, or 0 if not defeated
     */
    public EnemyDisplayData(int left, int top, int width, int height, boolean moving,
                            boolean facingLeft, boolean defeated, int ticksSinceDefeat) {
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
     * Returns the left edge of the hitbox.
     *
     * @return left edge, in screen pixels
     */
    public int getX() {
        return left;
    }

    /**
     * Returns the top edge of the hitbox.
     *
     * @return top edge, in screen pixels
     */
    public int getY() {
        return top;
    }

    /**
     * Returns the hitbox width.
     *
     * @return width, in pixels
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the hitbox height.
     *
     * @return height, in pixels
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns whether the enemy moved this tick.
     *
     * @return {@code true} if it moved (draw walking), {@code false} if still (draw idle)
     */
    public boolean isMoving() {
        return moving;
    }

    /**
     * Returns whether the enemy faces left.
     *
     * @return {@code true} if facing left
     */
    public boolean isFacingLeft() {
        return facingLeft;
    }

    /**
     * Returns whether the enemy is defeated.
     *
     * @return {@code true} if it should be drawn fading out (it has no hitbox any more)
     */
    public boolean isDefeated() {
        return defeated;
    }

    /**
     * Returns the ticks since the defeat, used to pick the death-animation frame.
     *
     * @return ticks since the defeat, or 0 if not defeated
     */
    public int getTicksSinceDefeat() {
        return ticksSinceDefeat;
    }
}
