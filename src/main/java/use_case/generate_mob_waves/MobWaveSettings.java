package use_case.generate_mob_waves;

/**
 * The tunable numbers for mob waves, kept together so balance changes happen in one place.
 *
 * <p>These are MVP defaults chosen by the team, not course requirements. Every wave currently
 * uses the same settings; scaling difficulty per wave is planned for later.
 */
public class MobWaveSettings {
    private final int enemiesPerWave;
    private final double enemyWidth;
    private final double enemyHeight;
    private final double enemySpeed;

    /**
     * Creates the settings.
     *
     * @param enemiesPerWave how many enemies each wave contains; must be at least 1
     * @param enemyWidth     each enemy's hitbox width, in pixels; must be positive
     * @param enemyHeight    each enemy's hitbox height, in pixels; must be positive
     * @param enemySpeed     each enemy's speed, in pixels per tick; must not be negative
     * @throws IllegalArgumentException if any value is out of range
     */
    public MobWaveSettings(int enemiesPerWave, double enemyWidth, double enemyHeight,
                           double enemySpeed) {
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
        this.enemiesPerWave = enemiesPerWave;
        this.enemyWidth = enemyWidth;
        this.enemyHeight = enemyHeight;
        this.enemySpeed = enemySpeed;
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
}
