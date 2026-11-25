package use_case.generate_mob_waves;

import entity.EnemyWave;
import entity.Hitbox;
import entity.Player;

/**
 * The game-session state that the Generate Mob Waves use case needs, and nothing more (ISP).
 *
 * <p>Owned by the use case and implemented in the {@code data_access} layer. Implementations
 * must return the <b>same</b> player object that the player-movement use case moves, so that
 * enemies chase the player's real, current position.
 */
public interface GenerateMobWavesDataAccessInterface {
    /**
     * Returns the player that enemies chase. The interactor only reads its position and size.
     *
     * @return the session's single player
     */
    Player getPlayer();

    /**
     * Returns the encounter's wave state, which the interactor updates in place.
     *
     * @return the session's single enemy wave
     */
    EnemyWave getEnemyWave();

    /**
     * Returns whether every bit of the given area is on walkable terrain, so an enemy may stand
     * there. Areas reaching outside the terrain are not walkable.
     *
     * @param area the hitbox an enemy would occupy
     * @return {@code true} if the whole area is walkable
     */
    boolean isWalkable(Hitbox area);
}
