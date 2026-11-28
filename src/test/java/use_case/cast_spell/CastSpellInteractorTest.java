package use_case.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.FireballSpell;
import entity.Player;
import entity.Projectile;
import entity.Spell;
import entity.TestTerrain;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * Tests launching and rejection through fake storage and presentation boundaries.
 */
class CastSpellInteractorTest {
    private static final double TOLERANCE = 1e-9;

    private final Spell spell = new FireballSpell();
    private final FakeDataAccess dataAccess = new FakeDataAccess();
    private final FakePresenter presenter = new FakePresenter();
    private final CastSpellInputBoundary interactor = new CastSpellInteractor(
            dataAccess, presenter, Map.of(spell.getId(), spell), 10, 10, 8);

    /**
     * Verifies that one request stores one centred projectile and reports success.
     */
    @Test
    void validRequestLaunchesOneProjectile() {
        interactor.execute(new CastSpellInputData("fireball", 224, 224));

        assertEquals(1, dataAccess.projectiles.size());

        Projectile projectile = dataAccess.projectiles.get(0);
        assertSame(spell, projectile.getSpell());
        assertEquals(124, projectile.getHitbox().getCenterX(), TOLERANCE);
        assertEquals(224, projectile.getHitbox().getCenterY(), TOLERANCE);

        assertEquals(1, presenter.successCalls);
        assertEquals(0, presenter.failureCalls);
        assertEquals("fireball", presenter.result.getSpellId());
        assertEquals("Fireball", presenter.result.getSpellName());
    }

    /**
     * Verifies that horizontal and diagonal launches have the configured speed.
     */
    @Test
    void launchSpeedIsIndependentOfDirection() {
        interactor.execute(new CastSpellInputData("fireball", 224, 224));
        interactor.execute(new CastSpellInputData("fireball", 154, 264));

        Projectile horizontal = dataAccess.projectiles.get(0);
        Projectile diagonal = dataAccess.projectiles.get(1);

        horizontal.move();
        diagonal.move();

        assertEquals(129, horizontal.getX(), TOLERANCE);
        assertEquals(220, horizontal.getY(), TOLERANCE);
        assertEquals(125, diagonal.getX(), TOLERANCE);
        assertEquals(228, diagonal.getY(), TOLERANCE);

        assertEquals(10, Math.hypot(
                diagonal.getX() - 119,
                diagonal.getY() - 220), TOLERANCE);

        assertEquals(2, presenter.successCalls);
        assertEquals(0, presenter.failureCalls);
    }

    /**
     * Verifies that launch position is read when the request is processed.
     */
    @Test
    void launchUsesCurrentPlayerPosition() {
        dataAccess.player.move(1, 0, TestTerrain.openGround(30, 20));

        interactor.execute(new CastSpellInputData("fireball", 224, 224));

        assertEquals(128,
                dataAccess.projectiles.get(0).getHitbox().getCenterX(),
                TOLERANCE);
    }

    /**
     * Verifies that unavailable spell identifiers produce failures without projectiles.
     */
    @Test
    void unavailableSpellIsRejected() {
        interactor.execute(new CastSpellInputData("unknown", 224, 224));
        interactor.execute(new CastSpellInputData(null, 224, 224));
        interactor.execute(new CastSpellInputData("", 224, 224));

        assertTrue(dataAccess.projectiles.isEmpty());
        assertEquals(0, presenter.successCalls);
        assertEquals(3, presenter.failureCalls);
    }

    /**
     * Verifies that either aim coordinate rejects NaN and infinity.
     */
    @Test
    void nonFiniteAimIsRejected() {
        double[] invalidValues = {
                Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY
        };

        for (double invalid : invalidValues) {
            interactor.execute(new CastSpellInputData("fireball", invalid, 224));
            interactor.execute(new CastSpellInputData("fireball", 224, invalid));
        }

        assertTrue(dataAccess.projectiles.isEmpty());
        assertEquals(0, presenter.successCalls);
        assertEquals(6, presenter.failureCalls);
    }

    /**
     * Verifies that aiming at the player's centre cannot divide by zero.
     */
    @Test
    void coincidentAimIsRejected() {
        interactor.execute(new CastSpellInputData("fireball", 124, 224));

        assertTrue(dataAccess.projectiles.isEmpty());
        assertEquals(0, presenter.successCalls);
        assertEquals(1, presenter.failureCalls);
    }

    /**
     * Verifies that requests whose direction overflows are safely rejected.
     */
    @Test
    void unrepresentableDirectionIsRejected() {
        interactor.execute(new CastSpellInputData(
                "fireball", Double.MAX_VALUE, Double.MAX_VALUE));

        assertTrue(dataAccess.projectiles.isEmpty());
        assertEquals(0, presenter.successCalls);
        assertEquals(1, presenter.failureCalls);
    }

    /**
     * Verifies validation of the injected projectile configuration.
     */
    @Test
    void invalidProjectileSettingsAreRejected() {
        Map<String, Spell> spells = Map.of(spell.getId(), spell);

        assertThrows(IllegalArgumentException.class,
                () -> new CastSpellInteractor(
                        dataAccess, presenter, spells, 0, 10, 8));

        assertThrows(IllegalArgumentException.class,
                () -> new CastSpellInteractor(
                        dataAccess, presenter, spells, 10, 0, 8));

        assertThrows(IllegalArgumentException.class,
                () -> new CastSpellInteractor(
                        dataAccess, presenter, spells, 10, 10, Double.NaN));
    }

    /**
     * Holds an isolated player and projectiles for each test.
     */
    private static final class FakeDataAccess implements CastSpellDataAccessInterface {
        private final Player player = new Player(100, 200, 48, 4);
        private final List<Projectile> projectiles = new ArrayList<>();

        /** {@inheritDoc} */
        @Override
        public Player getPlayer() {
            return player;
        }

        /** {@inheritDoc} */
        @Override
        public void addProjectile(Projectile projectile) {
            projectiles.add(projectile);
        }
    }

    /**
     * Records the use case's success and failure notifications.
     */
    private static final class FakePresenter implements CastSpellOutputBoundary {
        private int successCalls;
        private int failureCalls;
        private CastSpellOutputData result;

        /** {@inheritDoc} */
        @Override
        public void prepareSuccessView(CastSpellOutputData outputData) {
            successCalls++;
            result = outputData;
        }

        /** {@inheritDoc} */
        @Override
        public void prepareFailView(String errorMessage) {
            failureCalls++;
        }
    }
}
