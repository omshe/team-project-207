package data_access;

import entity.EnemyWave;
import entity.Player;
import use_case.generate_mob_waves.GenerateMobWavesDataAccessInterface;

/**
 * Keeps the mob-wave session state in memory for the length of one game.
 *
 * <p>The player is passed in rather than created here, so this object shares the exact
 * player instance that the movement use case moves. Nothing is saved to disk; persistence is
 * not part of this feature.
 */
public class InMemoryEnemyWaveDataAccessObject implements GenerateMobWavesDataAccessInterface {
    private final Player player;
    private final EnemyWave enemyWave;

    /**
     * Creates the in-memory session state.
     *
     * @param player    the game's single player, shared with the movement use case
     * @param enemyWave the wave state to keep, normally a new {@link EnemyWave}
     */
    public InMemoryEnemyWaveDataAccessObject(Player player, EnemyWave enemyWave) {
        this.player = player;
        this.enemyWave = enemyWave;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public EnemyWave getEnemyWave() {
        return enemyWave;
    }
}
