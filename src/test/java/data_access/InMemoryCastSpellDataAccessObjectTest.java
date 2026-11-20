package data_access;

import entity.Enemy;
import entity.EnemyWave;
import entity.FireballSpell;
import entity.Player;
import entity.Projectile;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.cast_spell.CastSpellDataAccessInterface;
import use_case.cast_spell.UpdateProjectilesDataAccessInterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests shared session objects, projectile storage, removal, and snapshots.
 */
class InMemoryCastSpellDataAccessObjectTest {

    private final Player player = new Player(100, 200, 48, 4);
    private final EnemyWave enemyWave = new EnemyWave();
    private final InMemoryCastSpellDataAccessObject dataAccess =
            new InMemoryCastSpellDataAccessObject(player, enemyWave);

    /**
     * Verifies that a new session starts with no projectiles.
     */
    @Test
    void storageStartsEmpty() {
        assertTrue(dataAccess.getProjectiles().isEmpty());
    }

    /**
     * Verifies that casting reads the same player used by movement.
     */
    @Test
    void getPlayerReturnsSharedPlayer() {
        assertSame(player, dataAccess.getPlayer());

        player.move(1, 0);

        assertEquals(104, dataAccess.getPlayer().getX(), 1e-9);
    }

    /**
     * Verifies that the launch and simulation boundaries share projectile storage.
     */
    @Test
    void castingAndUpdatingShareStoredProjectiles() {
        CastSpellDataAccessInterface casting = dataAccess;
        UpdateProjectilesDataAccessInterface updating = dataAccess;
        Projectile projectile = new Projectile(
                new FireballSpell(), 100, 200, 3, -4, 10, 8);

        casting.addProjectile(projectile);

        assertEquals(1, updating.getProjectiles().size());
        assertSame(projectile, updating.getProjectiles().get(0));
    }

    /**
     * Verifies that removing one projectile preserves the others.
     */
    @Test
    void removingProjectilePreservesOtherProjectiles() {
        Projectile first = new Projectile(
                new FireballSpell(), 100, 200, 3, -4, 10, 8);
        Projectile second = new Projectile(
                new FireballSpell(), 300, 200, -3, 4, 10, 8);
        dataAccess.addProjectile(first);
        dataAccess.addProjectile(second);

        assertTrue(dataAccess.removeProjectile(first));

        assertEquals(1, dataAccess.getProjectiles().size());
        assertSame(second, dataAccess.getProjectiles().get(0));
        assertFalse(dataAccess.removeProjectile(first));
    }

    /**
     * Verifies that removal uses identity even for identically configured projectiles.
     */
    @Test
    void removingUnknownProjectilePreservesStorage() {
        FireballSpell spell = new FireballSpell();
        Projectile stored = new Projectile(
                spell, 100, 200, 3, -4, 10, 8);
        Projectile other = new Projectile(
                spell, 100, 200, 3, -4, 10, 8);
        dataAccess.addProjectile(stored);

        assertFalse(dataAccess.removeProjectile(other));

        assertEquals(1, dataAccess.getProjectiles().size());
        assertSame(stored, dataAccess.getProjectiles().get(0));
    }

    /**
     * Verifies that collection snapshots are unmodifiable and survive later changes.
     */
    @Test
    void projectileSnapshotIsUnmodifiableAndStructurallyIndependent() {
        Projectile first = new Projectile(
                new FireballSpell(), 100, 200, 3, -4, 10, 8);
        Projectile second = new Projectile(
                new FireballSpell(), 300, 200, -3, 4, 10, 8);
        dataAccess.addProjectile(first);

        List<Projectile> snapshot = dataAccess.getProjectiles();

        dataAccess.addProjectile(second);
        dataAccess.removeProjectile(first);

        assertEquals(1, snapshot.size());
        assertSame(first, snapshot.get(0));
        assertEquals(1, dataAccess.getProjectiles().size());
        assertSame(second, dataAccess.getProjectiles().get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(second));
    }

    /**
     * Verifies that enemy queries follow changes to the shared wave.
     */
    @Test
    void getEnemiesReflectsSharedWave() {
        Enemy first = new Enemy(0, 0, 10, 10, 2);
        enemyWave.startNextWave(List.of(first));

        List<Enemy> snapshot = dataAccess.getEnemies();

        assertSame(first, snapshot.get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.clear());

        enemyWave.removeEnemy(first);

        assertTrue(dataAccess.getEnemies().isEmpty());
        assertEquals(1, snapshot.size());

        Enemy second = new Enemy(300, 200, 10, 10, 2);
        enemyWave.startNextWave(List.of(second));

        assertSame(second, dataAccess.getEnemies().get(0));
    }

    /**
     * Verifies that null session objects and projectiles are rejected.
     */
    @Test
    void nullDependenciesAndProjectilesAreRejected() {
        assertThrows(NullPointerException.class,
                () -> new InMemoryCastSpellDataAccessObject(null, enemyWave));
        assertThrows(NullPointerException.class,
                () -> new InMemoryCastSpellDataAccessObject(player, null));
        assertThrows(NullPointerException.class,
                () -> dataAccess.addProjectile(null));

        assertTrue(dataAccess.getProjectiles().isEmpty());
    }
}
