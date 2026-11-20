package use_case.generate_mob_waves;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Immutable result of one Generate Mob Waves tick: the wave number and a copy of every active
 * enemy's values.
 */
public class GenerateMobWavesOutputData {
    private final int waveNumber;
    private final List<EnemyOutputData> enemies;

    /**
     * Creates the output for one tick.
     *
     * @param waveNumber the current wave number, or 0 before the first wave
     * @param enemies    the active enemies' values; the list is copied defensively
     */
    public GenerateMobWavesOutputData(int waveNumber, List<EnemyOutputData> enemies) {
        this.waveNumber = waveNumber;
        this.enemies = Collections.unmodifiableList(new ArrayList<>(enemies));
    }

    /**
     * Returns the current wave number.
     *
     * @return 0 before the first wave, then 1, 2, ...
     */
    public int getWaveNumber() {
        return waveNumber;
    }

    /**
     * Returns the active enemies' values.
     *
     * @return an unmodifiable list of immutable enemy values, in spawn order
     */
    public List<EnemyOutputData> getEnemies() {
        return enemies;
    }
}
