package use_case.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.CrystalBurstSpell;
import entity.Enemy;
import entity.FireballSpell;
import entity.Hitbox;
import entity.Player;
import entity.Projectile;
import entity.ProjectileDefinition;
import entity.ProjectileSpawn;
import entity.ShardFan;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Exercises the real caster and simulator using headless in-memory boundaries. */
class CrystalBurstInteractorTest {
    private static final double TOLERANCE = 1e-5;
    private final Memory data = new Memory();
    private final Output output = new Output();
    private final CrystalBurstSpell spell = new CrystalBurstSpell();
    private final ProjectileDefinition core = new ProjectileDefinition(
            spell, "crystal_core", 8, 16, 16);
    private final ProjectileDefinition shard = new ProjectileDefinition(
            spell, "crystal_shard", 10, 8, 8);
    private final CastSpellInteractor caster = new CastSpellInteractor(
            data, output, Map.of(spell.getId(), core));
    private final UpdateProjectilesInteractor update = new UpdateProjectilesInteractor(
            data, output, 200, 220, Map.of(
                    "crystal_core", new ShatterIntoFanBehavior(
                            new ShardFan(shard, List.of(-25.0, 0.0, 25.0))),
                    "crystal_shard", new TerminalImpactBehavior()));

    @Test
    void birthIsAtSweptContactAndChildrenFirstMoveNextTick() {
        Enemy victim = new Enemy(70, 80, 10, 10, 0);
        data.enemies.add(victim);
        castAndReachFirstHit();
        assertTrue(victim.isDefeated());
        assertEquals(1, output.successes);
        assertEquals(3, data.projectiles.size());
        assertEquals(1, output.tick.getImpacts().size());
        assertEquals("crystal_core", output.tick.getImpacts().get(0).getProjectileTypeId());
        List<ProjectileOutputData> born = output.tick.getProjectiles();
        for (int index = 0; index < 3; index++) {
            Projectile projectile = data.projectiles.get(index);
            assertEquals(62, projectile.getHitbox().getCenterX(), TOLERANCE);
            assertEquals(84, projectile.getHitbox().getCenterY(), TOLERANCE);
            assertEquals("crystal_shard", born.get(index).getProjectileTypeId());
            assertEquals(10, Math.hypot(born.get(index).getVelocityX(),
                    born.get(index).getVelocityY()), TOLERANCE);
        }
        update.execute();
        for (int index = 0; index < 3; index++) {
            Projectile projectile = data.projectiles.get(index);
            assertEquals(62 + projectile.getVelocityX(),
                    projectile.getHitbox().getCenterX(), TOLERANCE);
        }
        assertEquals(1, output.successes);
        assertTrue(output.tick.getImpacts().isEmpty());
        assertEquals(62, born.get(0).getX() + 4, TOLERANCE);
    }

    @Test
    void shardsHitOtherEnemiesOnceAndNeverSplit() {
        data.enemies.add(new Enemy(70, 80, 10, 10, 0));
        data.enemies.add(new Enemy(110, 54, 10, 10, 0));
        data.enemies.add(new Enemy(110, 79, 10, 10, 0));
        data.enemies.add(new Enemy(110, 104, 10, 10, 0));
        caster.execute(new CastSpellInputData("crystal_burst", 180, 84));
        int impacts = 0;
        for (int tick = 0; tick < 30; tick++) {
            update.execute();
            impacts += output.tick.getImpacts().size();
            assertTrue(data.projectiles.size() <= 3);
        }
        assertEquals(4, impacts);
        assertTrue(data.enemies.stream().allMatch(enemy -> enemy.isDefeated()));
        assertTrue(data.projectiles.isEmpty());
    }

    @Test
    void movedEnemyEvadesAndShardsKeepTheirFixedLinesUntilExit() {
        data.enemies.add(new Enemy(70, 80, 10, 10, 0));
        Enemy evader = new Enemy(110, 79, 10, 10, 30);
        data.enemies.add(evader);
        castAndReachFirstHit();
        for (int move = 0; move < 3; move++) {
            evader.pursue(new Hitbox(110, 200, 10, 10));
        }
        data.player.move(1, 0);
        for (int tick = 0; tick < 30; tick++) {
            List<Projectile> before = List.copyOf(data.projectiles);
            double[] xs = new double[before.size()];
            double[] ys = new double[before.size()];
            for (int index = 0; index < before.size(); index++) {
                xs[index] = before.get(index).getX();
                ys[index] = before.get(index).getY();
            }
            update.execute();
            for (int index = 0; index < before.size(); index++) {
                Projectile projectile = before.get(index);
                assertEquals(xs[index] + projectile.getVelocityX(),
                        projectile.getX(), TOLERANCE);
                assertEquals(ys[index] + projectile.getVelocityY(),
                        projectile.getY(), TOLERANCE);
            }
            assertTrue(output.tick.getImpacts().isEmpty());
        }
        assertFalse(evader.isDefeated());
        assertTrue(data.projectiles.isEmpty());
    }

    @Test
    void invalidRequestsDoNotCreateAnythingAndShardsAreNotCastable() {
        for (String id : new String[] {null, "unknown", "crystal_shard"}) {
            caster.execute(new CastSpellInputData(id, 180, 84));
        }
        caster.execute(new CastSpellInputData("crystal_burst", 24, 84));
        caster.execute(new CastSpellInputData("crystal_burst", Double.NaN, 84));
        caster.execute(new CastSpellInputData("crystal_burst", 24, Double.POSITIVE_INFINITY));
        assertEquals(6, output.failures);
        assertEquals(0, output.successes);
        assertTrue(data.projectiles.isEmpty());
    }

    @Test
    void exitDoesNotShatterAndSameTickVictimCannotTriggerTwoSplits() {
        data.projectiles.add(new Projectile(core, 180, 20, 30, 0));
        update.execute();
        assertTrue(data.projectiles.isEmpty());
        assertTrue(output.tick.getImpacts().isEmpty());
        Enemy victim = new Enemy(70, 80, 10, 10, 0);
        data.enemies.add(victim);
        data.projectiles.add(new Projectile(core, 50, 76, 20, 0));
        data.projectiles.add(new Projectile(core, 50, 76, 20, 0));
        update.execute();
        assertEquals(1, output.tick.getImpacts().size());
        assertEquals(4, data.projectiles.size());
        assertTrue(victim.isDefeated());
    }

    @Test
    void missingStrategyFailsBeforeAnyProjectileMoves() {
        data.projectiles.add(new Projectile(core, 10, 10, 8, 0));
        UpdateProjectilesInteractor invalid = new UpdateProjectilesInteractor(
                data, output, 200, 220, Map.of());
        assertThrows(IllegalStateException.class, () -> invalid.execute());
        assertEquals(10, data.projectiles.get(0).getX());
    }

    @Test
    void separateDefinitionsPreserveEachSpellsSpeedAndDimensions() {
        FireballSpell fireball = new FireballSpell();
        ProjectileDefinition fireballFlight = new ProjectileDefinition(
                fireball, "fireball", 4, 24, 16);
        CastSpellInteractor multiple = new CastSpellInteractor(data, output,
                Map.of("fireball", fireballFlight, "crystal_burst", core));
        multiple.execute(new CastSpellInputData("fireball", 180, 84));
        multiple.execute(new CastSpellInputData("crystal_burst", 180, 84));
        assertEquals(4, data.projectiles.get(0).getVelocityX());
        assertEquals(24, data.projectiles.get(0).getHitbox().getWidth());
        assertEquals(8, data.projectiles.get(1).getVelocityX());
        assertEquals(16, data.projectiles.get(1).getHitbox().getWidth());
        assertEquals(2, output.successes);
    }

    @Test
    void outOfWorldChildIsDiscardedWithoutAnExtraImpact() {
        data.enemies.add(new Enemy(70, 80, 10, 10, 0));
        data.projectiles.add(new Projectile(core, 50, 76, 20, 0));
        ProjectileDefinition large = new ProjectileDefinition(spell, "large", 10, 400, 400);
        ProjectileImpactBehavior oversized = context -> List.of(new ProjectileSpawn(
                large, context.getCenterX(), context.getCenterY(), 10, 0));
        UpdateProjectilesInteractor guarded = new UpdateProjectilesInteractor(
                data, output, 200, 220, Map.of(
                        "crystal_core", oversized, "large", new TerminalImpactBehavior()));
        guarded.execute();
        assertTrue(data.projectiles.isEmpty());
        assertTrue(data.enemies.get(0).isDefeated());
        assertEquals(1, output.tick.getImpacts().size());
    }

    private void castAndReachFirstHit() {
        caster.execute(new CastSpellInputData("crystal_burst", 180, 84));
        for (int tick = 0; tick < 10; tick++) {
            update.execute();
            if (!output.tick.getImpacts().isEmpty()) {
                return;
            }
        }
        throw new AssertionError("The core should reach its first target");
    }

    /** Shares domain objects through narrow in-memory data-access interfaces. */
    private static final class Memory
            implements CastSpellDataAccessInterface, UpdateProjectilesDataAccessInterface {
        private final Player player = new Player(20, 80, 8, 4);
        private final List<Projectile> projectiles = new ArrayList<>();
        private final List<Enemy> enemies = new ArrayList<>();

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public void addProjectile(Projectile projectile) {
            projectiles.add(projectile);
        }

        @Override
        public List<Projectile> getProjectiles() {
            return List.copyOf(projectiles);
        }

        @Override
        public boolean removeProjectile(Projectile projectile) {
            return projectiles.remove(projectile);
        }

        @Override
        public List<Enemy> getEnemies() {
            return List.copyOf(enemies);
        }
    }

    /** Records public boundary output without Swing or persistence. */
    private static final class Output
            implements CastSpellOutputBoundary, UpdateProjectilesOutputBoundary {
        private int successes;
        private int failures;
        private UpdateProjectilesOutputData tick;

        @Override
        public void prepareSuccessView(CastSpellOutputData result) {
            successes++;
        }

        @Override
        public void prepareFailView(String message) {
            failures++;
        }

        @Override
        public void present(UpdateProjectilesOutputData result) {
            tick = result;
        }
    }
}
