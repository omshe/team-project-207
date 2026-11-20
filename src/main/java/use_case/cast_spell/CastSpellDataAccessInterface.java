package use_case.cast_spell;

import entity.Player;
import entity.Projectile;

/**
 * Provides the shared session state needed to launch a projectile.
 */
public interface CastSpellDataAccessInterface {

    /**
     * Returns the same player instance used by the movement use case.
     *
     * @return the current session's player
     */
    Player getPlayer();

    /**
     * Stores one projectile in the session's collection for subsequent updates.
     * Called on the game-loop thread.
     *
     * @param projectile the projectile to add exactly once
     */
    void addProjectile(Projectile projectile);
}