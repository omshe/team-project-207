package use_case.generate_mob_waves;

/**
 * The tunable numbers for mob waves, kept together so balance changes happen in one place.
 *
 * <p>These are MVP defaults chosen by the team, not course requirements. Waves get harder by
 * size: wave {@code n} has {@code enemiesPerWave + (n - 1) * extraEnemiesPerWave} enemies, but
 * never more than {@code maxEnemiesPerWave}.
 * Other per-wave scaling (speed, enemy types) is planned for later.
 */
public class MobWaveSettings {
    /** Spacing used by the short constructor: slimes keep 2 px apart. */
    public static final double DEFAULT_ENEMY_SPACING = 2;
    /** Defeat duration used by the short constructor: removed on the next tick. */
    public static final int DEFAULT_DEFEAT_DURATION_TICKS = 0;
    /** Growth used by the shorter constructors: every wave has the same size. */
    public static final int DEFAULT_EXTRA_ENEMIES_PER_WAVE = 0;

    private final int enemiesPerWave;
    private final double enemyWidth;
    private final double enemyHeight;
    private final double enemySpeed;
    private final double enemySpacing;
    private final int defeatDurationTicks;
    private final int extraEnemiesPerWave;
    private final int maxEnemiesPerWave;

    /**
     * Creates the settings with the default spacing, defeat duration and no growth per wave.
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
     * Creates the settings with no growth per wave.
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
        this(enemiesPerWave, enemyWidth, enemyHeight, enemySpeed, enemySpacing,
                defeatDurationTicks, DEFAULT_EXTRA_ENEMIES_PER_WAVE, enemiesPerWave);
    }

    /**
     * Creates the settings.
     *
     * @param enemiesPerWave how many enemies the first wave contains; must be at least 1
     * @param enemyWidth     each enemy's hitbox width, in pixels; must be positive
     * @param enemyHeight    each enemy's hitbox height, in pixels; must be positive
     * @param enemySpeed     each enemy's speed, in pixels per tick; must not be negative
     * @param enemySpacing   smallest gap, in pixels, a living enemy keeps from other living
     *                       enemies; must not be negative
     * @param defeatDurationTicks how many ticks a defeated enemy stays in the wave (shown
     *                       fading out) before it is removed; must not be negative
     * @param extraEnemiesPerWave how many more enemies each wave has than the one before;
     *                       must not be negative
     * @param maxEnemiesPerWave the most enemies any wave may have; must be at least
     *                       {@code enemiesPerWave}
     * @throws IllegalArgumentException if any value is out of range
     */
    public MobWaveSettings(int enemiesPerWave, double enemyWidth, double enemyHeight,
                           double enemySpeed, double enemySpacing, int defeatDurationTicks,
                           int extraEnemiesPerWave, int maxEnemiesPerWave) {
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
        if (extraEnemiesPerWave < 0) {
            throw new IllegalArgumentException("Extra enemies per wave must not be negative, got "
                    + extraEnemiesPerWave);
        }
        if (maxEnemiesPerWave < enemiesPerWave) {
            throw new IllegalArgumentException("The wave cap (" + maxEnemiesPerWave
                    + ") must be at least the first wave's size (" + enemiesPerWave + ")");
        }
        this.enemiesPerWave = enemiesPerWave;
        this.enemyWidth = enemyWidth;
        this.enemyHeight = enemyHeight;
        this.enemySpeed = enemySpeed;
        this.enemySpacing = enemySpacing;
        this.defeatDurationTicks = defeatDurationTicks;
        this.extraEnemiesPerWave = extraEnemiesPerWave;
        this.maxEnemiesPerWave = maxEnemiesPerWave;
    }

    /**
     * Returns how many enemies the first wave contains.
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

    /**
     * Returns how many more enemies each wave has than the one before.
     *
     * @return the growth per wave, at least 0
     */
    public int getExtraEnemiesPerWave() {
        return extraEnemiesPerWave;
    }

    /**
     * Returns the most enemies any wave may have.
     *
     * @return the cap, at least the first wave's size
     */
    public int getMaxEnemiesPerWave() {
        return maxEnemiesPerWave;
    }

    /**
     * Returns how many enemies the given wave contains.
     *
     * @param waveNumber the wave, starting at 1
     * @return {@code enemiesPerWave + (waveNumber - 1) * extraEnemiesPerWave}, capped at
     *     {@code maxEnemiesPerWave}
     * @throws IllegalArgumentException if {@code waveNumber} is less than 1
     */
    public int getEnemiesInWave(int waveNumber) {
        if (waveNumber < 1) {
            throw new IllegalArgumentException("Waves are numbered from 1, got " + waveNumber);
        }
        // Long arithmetic so a huge wave number cannot overflow before the cap applies.
        long uncapped = enemiesPerWave + (long) (waveNumber - 1) * extraEnemiesPerWave;
        return (int) Math.min(uncapped, maxEnemiesPerWave);
    }
}
