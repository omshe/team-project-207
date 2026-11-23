package use_case.generate_mob_waves;

/**
 * The tunable numbers for mob waves, kept together so balance changes happen in one place.
 *
 * <p>These are MVP defaults chosen by the team, not course requirements. Every wave currently
 * uses the same settings; scaling difficulty per wave is planned for later.
 */
public class MobWaveSettings {
    /** Spacing used by the short constructor: slimes keep 2 px apart. */
    public static final double DEFAULT_ENEMY_SPACING = 2;
    /** Defeat duration used by the short constructor: removed on the next tick. */
    public static final int DEFAULT_DEFEAT_DURATION_TICKS = 0;

    private final int enemiesPerWave;
    private final double enemyWidth;
    private final double enemyHeight;
    private final double enemySpeed;
    private final double enemySpacing;
    private final int defeatDurationTicks;

    /**
     * Creates the settings with the default spacing and defeat duration.
     *
     * @param enemiesPerWave how many enemies each wave contains; must be at least 1
     * @param enemyWidth     each enemy's hitbox width, in pixels; must be positive
     * @param enemyHeight    each enemy's hitbox height, in pixels; must be positive
     * @param enemySpeed     each enemy's speed, in pixels per tick; must not be negative
     * @throws IllegalArgumentException if any value is out of range
     */
    public MobWaveSettings(int enemiesPerWave, double enemyWidth, double enemyHeight,
                           double enemySpeed) {
        this(enemiesPerWave, enemyWidth, enemyHeight, enemySpeed, DEFAULT_ENEMY_SPACING,
                DEFAULT_DEFEAT_DURATION_TICKS);
    }

    /**
     * Creates the settings.
     *
     * @param enemiesPerWave how many enemies each wave contains; must be at least 1
     * @param enemyWidth     each enemy's hitbox width, in pixels; must be positive
     * @param enemyHeight    each enemy's hitbox height, in pixels; must be positive
     * @param enemySpeed     each enemy's speed, in pixels per tick; must not be negative
     * @param enemySpacing   smallest gap, in pixels, a living enemy keeps from other living
     *                       enemies; must not be negative
     * @param defeatDurationTicks how many ticks a defeated enemy stays in the wave (shown
     *                       fading out) before it is removed; must not be negative
     * @throws IllegalArgumentException if any value is out of range
     */
    public MobWaveSettings(int enemiesPerWave, double enemyWidth, double enemyHeight,
                           double enemySpeed, double enemySpacing, int defeatDurationTicks) {
        if (enemiesPerWave < 1) {
            throw new IllegalArgumentException("A wave needs at least one enemy, got "
                    + enemiesPerWave);
        }
        if (enemyWidth <= 0 || enemyHeight <= 0) {
            throw new IllegalArgumentException("Enemy size must be positive, got "
                    + enemyWidth + " x " + enemyHeight);
        }
        if (enemySpeed < 0) {
            throw new IllegalArgumentException("Enemy speed must not be negative, got "
                    + enemySpeed);
        }
        if (enemySpacing < 0) {
            throw new IllegalArgumentException("Enemy spacing must not be negative, got "
                    + enemySpacing);
        }
        if (defeatDurationTicks < 0) {
            throw new IllegalArgumentException("Defeat duration must not be negative, got "
                    + defeatDurationTicks);
        }
        this.enemiesPerWave = enemiesPerWave;
        this.enemyWidth = enemyWidth;
        this.enemyHeight = enemyHeight;
        this.enemySpeed = enemySpeed;
        this.enemySpacing = enemySpacing;
        this.defeatDurationTicks = defeatDurationTicks;
    }

    /**
     * Returns how many enemies each wave contains.
     *
     * @return the enemy count, at least 1
     */
    public int getEnemiesPerWave() {
        return enemiesPerWave;
    }

    /**
     * Returns each enemy's hitbox width.
     *
     * @return width, in pixels
     */
    public double getEnemyWidth() {
        return enemyWidth;
    }

    /**
     * Returns each enemy's hitbox height.
     *
     * @return height, in pixels
     */
    public double getEnemyHeight() {
        return enemyHeight;
    }

    /**
     * Returns each enemy's speed.
     *
     * @return speed, in pixels per tick
     */
    public double getEnemySpeed() {
        return enemySpeed;
    }

    /**
     * Returns the smallest gap living enemies keep from each other.
     *
     * @return spacing, in pixels
     */
    public double getEnemySpacing() {
        return enemySpacing;
    }

    /**
     * Returns how long a defeated enemy stays before it is removed.
     *
     * @return duration, in ticks
     */
    public int getDefeatDurationTicks() {
        return defeatDurationTicks;
    }

}
