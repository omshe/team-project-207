package use_case.cast_spell;

import entity.Enemy;
import entity.Projectile;
import java.util.List;

/**
 * Provides the session objects needed to update and expire projectiles.
 */
public interface UpdateProjectilesDataAccessInterface {

    /**
     * Returns an unmodifiable snapshot of the current projectile collection.
     * The projectile objects remain live and may be moved by the use case.
     *
     * @return the current projectiles in insertion order
     */
    List<Projectile> getProjectiles();

    /**
     * Stores one newborn secondary projectile on the game-loop thread.
     *
     * @param projectile non-null projectile to add once after the original update iteration
     */
    void addProjectile(Projectile projectile);

    /**
     * Removes a stored projectile, identified by its object identity.
     *
     * @param projectile the projectile to remove
     * @return true if it was stored and has been removed, otherwise false
     */
    boolean removeProjectile(Projectile projectile);

    /**
     * Returns an unmodifiable snapshot of the current wave's active enemies.
     * The enemy objects are shared with the enemy-wave use case.
     *
     * @return the current enemies to check for projectile contact
     */
    List<Enemy> getEnemies();
}