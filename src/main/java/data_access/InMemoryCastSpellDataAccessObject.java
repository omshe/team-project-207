package data_access;

import entity.Enemy;
import entity.EnemyWave;
import entity.Player;
import entity.Projectile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.cast_spell.CastSpellDataAccessInterface;
import use_case.cast_spell.UpdateProjectilesDataAccessInterface;

/**
 * Shares in-memory session state between projectile launching and simulation.
 */
public final class InMemoryCastSpellDataAccessObject
        implements CastSpellDataAccessInterface, UpdateProjectilesDataAccessInterface {

    private final Player player;
    private final EnemyWave enemyWave;
    private final List<Projectile> projectiles = new ArrayList<>();

    /**
     * Creates storage using the same objects as movement and enemy-wave updates.
     * Access this object on the game-loop thread.
     *
     * @param player the session's shared player
     * @param enemyWave the session's shared enemy wave
     * @throws NullPointerException if player or enemyWave is null
     */
    public InMemoryCastSpellDataAccessObject(Player player, EnemyWave enemyWave) {
        this.player = Objects.requireNonNull(player, "Player must not be null");
        this.enemyWave = Objects.requireNonNull(enemyWave, "Enemy wave must not be null");
    }

    /** {@inheritDoc} */
    @Override
    public Player getPlayer() {
        return player;
    }

    /**
     * {@inheritDoc}
     *
     * @throws NullPointerException if projectile is null
     */
    @Override
    public void addProjectile(Projectile projectile) {
        projectiles.add(Objects.requireNonNull(
                projectile, "Projectile must not be null"));
    }

    /** {@inheritDoc} */
    @Override
    public List<Projectile> getProjectiles() {
        return List.copyOf(projectiles);
    }

    /** {@inheritDoc} */
    @Override
    public boolean removeProjectile(Projectile projectile) {
        return projectiles.remove(projectile);
    }

    /** {@inheritDoc} */
    @Override
    public List<Enemy> getEnemies() {
        return enemyWave.getActiveEnemies();
    }
}
