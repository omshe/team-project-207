package use_case.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Enemy;
import entity.FireballSpell;
import entity.Projectile;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Tests projectile ticks, swept contact, world exit, and immutable output.
 */
class UpdateProjectilesInteractorTest {
    private static final double TOLERANCE = 1e-5;

    private final FakeDataAccess dataAccess = new FakeDataAccess();
    private final FakePresenter presenter = new FakePresenter();
    private final UpdateProjectilesInputBoundary interactor =
            new UpdateProjectilesInteractor(dataAccess, presenter, 100, 100);

    /**
     * Verifies one presentation per tick, including an empty simulation.
     */
    @Test
    void emptyTicksStillPresentOnce() {
        interactor.execute();

        assertEquals(1, presenter.calls);
        assertEquals(1, presenter.result.getTick());
        assertTrue(presenter.result.getProjectiles().isEmpty());
        assertTrue(presenter.result.getImpacts().isEmpty());

        interactor.execute();

        assertEquals(2, presenter.calls);
        assertEquals(2, presenter.result.getTick());
    }

    /**
     * Verifies motion and copies of all values needed for rendering.
     */
    @Test
    void survivingProjectileMovesAndProducesSnapshot() {
        Projectile projectile = projectile(10, 10, 3, 4);
        dataAccess.projectiles.add(projectile);

        interactor.execute();

        ProjectileOutputData output =
                presenter.result.getProjectiles().get(0);

        assertSame(projectile, dataAccess.projectiles.get(0));
        assertEquals("fireball", output.getSpellId());
        assertEquals(13, output.getX(), TOLERANCE);
        assertEquals(14, output.getY(), TOLERANCE);
        assertEquals(4, output.getWidth(), TOLERANCE);
        assertEquals(4, output.getHeight(), TOLERANCE);
        assertEquals(3, output.getVelocityX(), TOLERANCE);
        assertEquals(4, output.getVelocityY(), TOLERANCE);
        assertTrue(presenter.result.getImpacts().isEmpty());
    }

    /**
     * Verifies contact between tick endpoints in each cardinal direction.
     */
    @ParameterizedTest
    @CsvSource({
            "10,10,80,0,50,10,48,12",
            "80,10,-70,0,40,10,52,12",
            "10,10,0,80,10,50,12,48",
            "10,80,0,-70,10,40,12,52"
    })
    void fastProjectileHitsBetweenEndpoints(
            double x, double y,
            double velocityX, double velocityY,
            double enemyX, double enemyY,
            double impactX, double impactY) {
        dataAccess.projectiles.add(projectile(x, y, velocityX, velocityY));
        dataAccess.enemies.add(new Enemy(enemyX, enemyY, 10, 10, 2));

        interactor.execute();

        assertTrue(dataAccess.projectiles.isEmpty());
        assertEquals(1, presenter.result.getImpacts().size());

        ProjectileImpactOutputData impact =
                presenter.result.getImpacts().get(0);

        assertEquals("fireball", impact.getSpellId());
        assertEquals(impactX, impact.getX(), TOLERANCE);
        assertEquals(impactY, impact.getY(), TOLERANCE);
        assertEquals(1, dataAccess.enemies.size());
    }

    /**
     * Verifies first contact regardless of enemy list order and no repeated impacts.
     */
    @Test
    void earliestContactProducesOneImpact() {
        dataAccess.projectiles.add(projectile(10, 10, 80, 0));
        dataAccess.enemies.add(new Enemy(70, 10, 10, 10, 2));
        dataAccess.enemies.add(new Enemy(40, 10, 10, 10, 2));

        interactor.execute();

        assertEquals(1, presenter.result.getImpacts().size());
        assertEquals(38,
                presenter.result.getImpacts().get(0).getX(), TOLERANCE);
        assertEquals(2, dataAccess.enemies.size());

        interactor.execute();

        assertTrue(presenter.result.getImpacts().isEmpty());
    }

    /**
     * Verifies one impact when several enemies are touched at the same time.
     */
    @Test
    void simultaneousContactsProduceOneImpact() {
        dataAccess.projectiles.add(projectile(10, 10, 80, 0));
        dataAccess.enemies.add(new Enemy(50, 10, 10, 10, 2));
        dataAccess.enemies.add(new Enemy(50, 10, 10, 10, 2));

        interactor.execute();

        assertEquals(1, presenter.result.getImpacts().size());
        assertTrue(dataAccess.projectiles.isEmpty());
        assertEquals(2, dataAccess.enemies.size());
    }

    /**
     * Verifies diagonal contact and rejects a false hit inside the path's bounding box.
     */
    @Test
    void diagonalContactUsesTheActualPath() {
        dataAccess.projectiles.add(projectile(10, 10, 60, 60));
        dataAccess.enemies.add(new Enemy(10, 60, 10, 10, 2));

        interactor.execute();

        assertEquals(1, dataAccess.projectiles.size());
        assertTrue(presenter.result.getImpacts().isEmpty());

        dataAccess.projectiles.clear();
        dataAccess.enemies.clear();
        dataAccess.projectiles.add(projectile(10, 10, 60, 60));
        dataAccess.enemies.add(new Enemy(40, 40, 10, 10, 2));

        interactor.execute();

        assertEquals(1, presenter.result.getImpacts().size());
        assertEquals(38,
                presenter.result.getImpacts().get(0).getX(), TOLERANCE);
        assertEquals(38,
                presenter.result.getImpacts().get(0).getY(), TOLERANCE);
    }

    /**
     * Verifies immediate contact for a projectile already touching an enemy.
     */
    @Test
    void initialContactCountsEvenWhenMovingAway() {
        dataAccess.projectiles.add(projectile(10, 10, -3, 0));
        dataAccess.enemies.add(new Enemy(14, 10, 10, 10, 2));

        interactor.execute();

        assertTrue(dataAccess.projectiles.isEmpty());
        assertEquals(12,
                presenter.result.getImpacts().get(0).getX(), TOLERANCE);
    }

    /**
     * Verifies that zero velocity is handled for separated and overlapping hitboxes.
     */
    @Test
    void stationaryProjectileIsHandled() {
        dataAccess.projectiles.add(projectile(10, 10, 0, 0));

        interactor.execute();

        assertEquals(1, presenter.result.getProjectiles().size());

        dataAccess.enemies.add(new Enemy(12, 12, 10, 10, 2));

        interactor.execute();

        assertEquals(1, presenter.result.getImpacts().size());
        assertTrue(dataAccess.projectiles.isEmpty());
    }

    /**
     * Verifies silent expiration across every world edge.
     */
    @ParameterizedTest
    @CsvSource({
            "95,10,10,0",
            "1,10,-5,0",
            "10,95,0,10",
            "10,1,0,-5"
    })
    void worldExitProducesNoImpact(
            double x, double y, double velocityX, double velocityY) {
        dataAccess.projectiles.add(projectile(x, y, velocityX, velocityY));

        interactor.execute();

        assertTrue(dataAccess.projectiles.isEmpty());
        assertTrue(presenter.result.getProjectiles().isEmpty());
        assertTrue(presenter.result.getImpacts().isEmpty());
    }

    /**
     * Verifies that touching the world boundary remains valid until the next outward step.
     */
    @Test
    void exactWorldBoundaryRemainsInside() {
        dataAccess.projectiles.add(projectile(90, 10, 6, 0));

        interactor.execute();

        assertEquals(1, dataAccess.projectiles.size());

        interactor.execute();

        assertTrue(dataAccess.projectiles.isEmpty());
    }

    /**
     * Verifies that an enemy beyond world exit cannot cause an impact.
     */
    @Test
    void collisionsAfterWorldExitAreIgnored() {
        dataAccess.projectiles.add(projectile(10, 10, 150, 0));
        dataAccess.enemies.add(new Enemy(110, 10, 10, 10, 2));

        interactor.execute();

        assertTrue(dataAccess.projectiles.isEmpty());
        assertTrue(presenter.result.getImpacts().isEmpty());
    }

    /**
     * Verifies contact before world exit even when the final movement position is outside.
     */
    @Test
    void collisionBeforeWorldExitProducesImpact() {
        dataAccess.projectiles.add(projectile(10, 10, 150, 0));
        dataAccess.enemies.add(new Enemy(90, 10, 10, 10, 2));

        interactor.execute();

        assertEquals(1, presenter.result.getImpacts().size());
        assertEquals(88,
                presenter.result.getImpacts().get(0).getX(), TOLERANCE);
    }

    /**
     * Verifies independent updates when projectiles survive, collide, and exit together.
     */
    @Test
    void multipleProjectilesAreUpdatedIndependently() {
        dataAccess.projectiles.add(projectile(10, 10, 60, 0));
        dataAccess.projectiles.add(projectile(95, 70, 10, 0));

        Projectile survivor = projectile(10, 50, 3, 0);
        dataAccess.projectiles.add(survivor);
        dataAccess.enemies.add(new Enemy(50, 10, 10, 10, 2));

        interactor.execute();

        assertEquals(1, dataAccess.projectiles.size());
        assertSame(survivor, dataAccess.projectiles.get(0));
        assertEquals(1, presenter.result.getProjectiles().size());
        assertEquals(1, presenter.result.getImpacts().size());
        assertEquals(1, presenter.calls);
    }

    /**
     * Verifies removal of an invalid initial world placement without an impact.
     */
    @Test
    void projectileStartingOutsideWorldIsRemoved() {
        dataAccess.projectiles.add(projectile(-1, 10, 3, 0));

        interactor.execute();

        assertTrue(dataAccess.projectiles.isEmpty());
        assertTrue(presenter.result.getImpacts().isEmpty());
    }

    /**
     * Verifies immutable lists, independent collection copies, and position snapshots.
     */
    @Test
    void outputIsAnImmutableSnapshot() {
        dataAccess.projectiles.add(projectile(10, 10, 3, 4));

        interactor.execute();

        UpdateProjectilesOutputData first = presenter.result;

        interactor.execute();

        assertEquals(13,
                first.getProjectiles().get(0).getX(), TOLERANCE);
        assertEquals(16,
                presenter.result.getProjectiles().get(0).getX(), TOLERANCE);

        assertThrows(UnsupportedOperationException.class,
                () -> first.getProjectiles().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> first.getImpacts().add(
                        new ProjectileImpactOutputData("fireball", 1, 2)));

        List<ProjectileOutputData> source =
                new ArrayList<>(first.getProjectiles());
        UpdateProjectilesOutputData copy =
                new UpdateProjectilesOutputData(1, source, List.of());

        source.clear();

        assertEquals(1, copy.getProjectiles().size());
    }

    /**
     * Verifies validation of required collaborators and world dimensions.
     */
    @Test
    void invalidConfigurationIsRejected() {
        assertThrows(NullPointerException.class,
                () -> new UpdateProjectilesInteractor(
                        null, presenter, 100, 100));
        assertThrows(NullPointerException.class,
                () -> new UpdateProjectilesInteractor(
                        dataAccess, null, 100, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new UpdateProjectilesInteractor(
                        dataAccess, presenter, 0, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new UpdateProjectilesInteractor(
                        dataAccess, presenter, 100, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new UpdateProjectilesInteractor(
                        dataAccess, presenter, Double.NaN, 100));
        assertThrows(IllegalArgumentException.class,
                () -> new UpdateProjectilesInteractor(
                        dataAccess, presenter, 100, Double.POSITIVE_INFINITY));
    }

    /**
     * Creates a four-pixel square projectile for simulation scenarios.
     */
    private static Projectile projectile(
            double x, double y, double velocityX, double velocityY) {
        return new Projectile(
                new FireballSpell(), x, y, velocityX, velocityY, 4, 4);
    }

    /**
     * Provides isolated mutable entities through structural snapshots.
     */
    private static final class FakeDataAccess
            implements UpdateProjectilesDataAccessInterface {

        private final List<Projectile> projectiles = new ArrayList<>();
        private final List<Enemy> enemies = new ArrayList<>();

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
            return List.copyOf(enemies);
        }
    }

    /**
     * Records the most recent tick result and presentation count.
     */
    private static final class FakePresenter
            implements UpdateProjectilesOutputBoundary {

        private int calls;
        private UpdateProjectilesOutputData result;

        /** {@inheritDoc} */
        @Override
        public void present(UpdateProjectilesOutputData outputData) {
            calls++;
            result = outputData;
        }
    }
}