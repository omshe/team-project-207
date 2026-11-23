package use_case.generate_mob_waves;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Hitbox;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests the small value and strategy classes of the Generate Mob Waves use case. */
class SupportingClassesTest {

    @Test
    void edgeStrategyProposesEdgeMiddlesThenCornersInsideTheWorld() {
        EdgeSpawnPositionStrategy strategy = new EdgeSpawnPositionStrategy(16);
        List<Hitbox> candidates = strategy.proposeCandidates(48, 30, 960, 640,
                new Hitbox(456, 296, 48, 48));

        // The first eight are the edge middles and corners; more edge spots follow.
        assertTrue(candidates.size() > 8);
        Hitbox leftMiddle = candidates.get(0);
        assertEquals(16, leftMiddle.getX(), 0);
        assertEquals(305, leftMiddle.getY(), 0);
        Hitbox rightMiddle = candidates.get(1);
        assertEquals(960 - 16, rightMiddle.getRight(), 0);
        Hitbox topMiddle = candidates.get(2);
        assertEquals(16, topMiddle.getY(), 0);
        assertEquals(456, topMiddle.getX(), 0);
        Hitbox bottomMiddle = candidates.get(3);
        assertEquals(640 - 16, bottomMiddle.getBottom(), 0);
        Hitbox bottomRight = candidates.get(7);
        assertEquals(960 - 16, bottomRight.getRight(), 0);
        assertEquals(640 - 16, bottomRight.getBottom(), 0);
        for (Hitbox candidate : candidates) {
            assertTrue(candidate.isInsideWorld(960, 640));
            assertEquals(48, candidate.getWidth(), 0);
            assertEquals(30, candidate.getHeight(), 0);
        }
    }

    @Test
    void edgeStrategyOffersEnoughSpacedSpotsForBigWaves() {
        EdgeSpawnPositionStrategy strategy = new EdgeSpawnPositionStrategy(16);
        List<Hitbox> candidates = strategy.proposeCandidates(56, 34, 960, 640,
                new Hitbox(456, 296, 48, 48));
        // Count how many could be chosen together without touching each other.
        List<Hitbox> chosen = new ArrayList<>();
        for (Hitbox candidate : candidates) {
            assertTrue(candidate.isInsideWorld(960, 640));
            boolean free = true;
            for (Hitbox other : chosen) {
                if (candidate.separationFrom(other) < 2) {
                    free = false;
                }
            }
            if (free) {
                chosen.add(candidate);
            }
        }
        assertTrue(chosen.size() >= 40, "only " + chosen.size() + " spaced spots");
    }

    @Test
    void edgeStrategyRejectsNegativeMargin() {
        assertThrows(IllegalArgumentException.class, () -> new EdgeSpawnPositionStrategy(-1));
    }

    @Test
    void settingsKeepTheirValues() {
        MobWaveSettings settings = new MobWaveSettings(3, 56, 34, 2);
        assertEquals(3, settings.getEnemiesPerWave());
        assertEquals(56, settings.getEnemyWidth(), 0);
        assertEquals(34, settings.getEnemyHeight(), 0);
        assertEquals(2, settings.getEnemySpeed(), 0);
    }

    @Test
    void shortSettingsUseTheDefaultSpacingAndDefeatDuration() {
        MobWaveSettings settings = new MobWaveSettings(3, 56, 34, 2);
        assertEquals(MobWaveSettings.DEFAULT_ENEMY_SPACING, settings.getEnemySpacing(), 0);
        assertEquals(MobWaveSettings.DEFAULT_DEFEAT_DURATION_TICKS,
                settings.getDefeatDurationTicks());
        MobWaveSettings full = new MobWaveSettings(3, 56, 34, 2, 4, 32);
        assertEquals(4, full.getEnemySpacing(), 0);
        assertEquals(32, full.getDefeatDurationTicks());
    }

    @Test
    void eachWaveCanHaveMoreEnemiesThanTheLast() {
        MobWaveSettings growing = new MobWaveSettings(3, 56, 34, 2, 2, 32, 1, 10);
        assertEquals(1, growing.getExtraEnemiesPerWave());
        assertEquals(3, growing.getEnemiesInWave(1));
        assertEquals(4, growing.getEnemiesInWave(2));
        assertEquals(10, growing.getEnemiesInWave(8));
        assertEquals(10, growing.getEnemiesInWave(9), "capped at 10");
        assertEquals(10, growing.getEnemiesInWave(Integer.MAX_VALUE), "no overflow");
        assertEquals(10, growing.getMaxEnemiesPerWave());
        MobWaveSettings fixed = new MobWaveSettings(3, 56, 34, 2);
        assertEquals(MobWaveSettings.DEFAULT_EXTRA_ENEMIES_PER_WAVE,
                fixed.getExtraEnemiesPerWave());
        assertEquals(3, fixed.getEnemiesInWave(7));
        assertThrows(IllegalArgumentException.class, () -> growing.getEnemiesInWave(0));
        assertThrows(IllegalArgumentException.class,
                () -> new MobWaveSettings(3, 56, 34, 2, 2, 32, -1, 10));
        assertThrows(IllegalArgumentException.class,
                () -> new MobWaveSettings(3, 56, 34, 2, 2, 32, 1, 2));
    }

    @Test
    void settingsRejectNegativeSpacingOrDefeatDuration() {
        assertThrows(IllegalArgumentException.class,
                () -> new MobWaveSettings(3, 48, 48, 2, -1, 0));
        assertThrows(IllegalArgumentException.class,
                () -> new MobWaveSettings(3, 48, 48, 2, 0, -1));
    }

    @Test
    void settingsRejectOutOfRangeValues() {
        assertThrows(IllegalArgumentException.class, () -> new MobWaveSettings(0, 48, 48, 2));
        assertThrows(IllegalArgumentException.class, () -> new MobWaveSettings(3, 0, 48, 2));
        assertThrows(IllegalArgumentException.class, () -> new MobWaveSettings(3, 48, 0, 2));
        assertThrows(IllegalArgumentException.class, () -> new MobWaveSettings(3, 48, 48, -1));
    }

    @Test
    void inputDataKeepsTheDirection() {
        GenerateMobWavesInputData input = new GenerateMobWavesInputData(-1, 1);
        assertEquals(-1, input.getMoveX());
        assertEquals(1, input.getMoveY());
    }

    @Test
    void outputDataCopiesTheEnemyListAndIsReadOnly() {
        List<EnemyOutputData> enemies = new ArrayList<>();
        enemies.add(new EnemyOutputData(1, 2, 3, 4, true, true, false, 0));
        GenerateMobWavesOutputData output = new GenerateMobWavesOutputData(5, enemies);
        enemies.clear();

        assertEquals(5, output.getWaveNumber());
        assertEquals(1, output.getEnemies().size());
        assertThrows(UnsupportedOperationException.class, () -> output.getEnemies().clear());

        EnemyOutputData enemy = output.getEnemies().get(0);
        assertEquals(1, enemy.getX(), 0);
        assertEquals(2, enemy.getY(), 0);
        assertEquals(3, enemy.getWidth(), 0);
        assertEquals(4, enemy.getHeight(), 0);
        assertTrue(enemy.isMoving());
        assertTrue(enemy.isFacingLeft());
        assertFalse(new EnemyOutputData(0, 0, 1, 1, false, false, false, 0).isMoving());
        assertFalse(enemy.isDefeated());
        EnemyOutputData defeated = new EnemyOutputData(0, 0, 1, 1, false, false, true, 9);
        assertTrue(defeated.isDefeated());
        assertEquals(9, defeated.getTicksSinceDefeat());
    }
}
