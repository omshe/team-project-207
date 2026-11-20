package use_case.generate_mob_waves;

/**
 * Immutable input for one tick: the movement the player asked for, as -1, 0 or 1 on each axis.
 *
 * <p>Uses the same convention as the player's movement input: positive x is right and positive
 * y is down. Opposite keys held together have already cancelled out to 0 by the time this
 * object is built.
 */
public class GenerateMobWavesInputData {
    private final int moveX;
    private final int moveY;

    /**
     * Creates the input for one tick.
     *
     * @param moveX requested horizontal direction: -1 (left), 0 or 1 (right)
     * @param moveY requested vertical direction: -1 (up), 0 or 1 (down)
     */
    public GenerateMobWavesInputData(int moveX, int moveY) {
        this.moveX = moveX;
        this.moveY = moveY;
    }

    /**
     * Returns the requested horizontal direction.
     *
     * @return -1, 0 or 1
     */
    public int getMoveX() {
        return moveX;
    }

    /**
     * Returns the requested vertical direction.
     *
     * @return -1, 0 or 1
     */
    public int getMoveY() {
        return moveY;
    }
}
