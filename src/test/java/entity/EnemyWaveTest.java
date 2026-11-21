package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnemyWaveTest {

    private static List<Enemy> enemies(int count) {
        List<Enemy> list = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            list.add(new Enemy(index * 100, 0, 10, 10, 2));
        }
        return list;
    }

    @Test
    void startsNotStarted() {
        EnemyWave wave = new EnemyWave();
        assertEquals(0, wave.getWaveNumber());
        assertFalse(wave.hasStarted());
        assertFalse(wave.isActive());
        assertFalse(wave.isCleared());
        assertTrue(wave.getActiveEnemies().isEmpty());
    }

    @Test
    void triggerIsRememberedUntilTheFirstWaveStarts() {
        EnemyWave wave = new EnemyWave();
        assertFalse(wave.isEncounterTriggered());

        wave.triggerEncounter();
        wave.triggerEncounter();   // repeating has no further effect
        assertTrue(wave.isEncounterTriggered());
        assertFalse(wave.hasStarted());

        wave.startNextWave(enemies(1));
        assertTrue(wave.isEncounterTriggered());
    }

    @Test
    void startingWaveMakesItActive() {
        EnemyWave wave = new EnemyWave();
        wave.startNextWave(enemies(3));
        assertEquals(1, wave.getWaveNumber());
        assertTrue(wave.hasStarted());
        assertTrue(wave.isActive());
        assertFalse(wave.isCleared());
        assertEquals(3, wave.getActiveEnemies().size());
    }

    @Test
    void cannotStartWaveWhileOneIsActive() {
        EnemyWave wave = new EnemyWave();
        wave.startNextWave(enemies(3));
        assertThrows(IllegalStateException.class, () -> wave.startNextWave(enemies(3)));
        assertEquals(1, wave.getWaveNumber());
    }

    @Test
    void cannotStartAnEmptyWave() {
        EnemyWave wave = new EnemyWave();
        assertThrows(IllegalArgumentException.class, () -> wave.startNextWave(new ArrayList<>()));
        assertFalse(wave.hasStarted());
    }

    @Test
    void removingEveryEnemyClearsTheWaveAndAllowsTheNext() {
        EnemyWave wave = new EnemyWave();
        wave.startNextWave(enemies(2));
        for (Enemy enemy : wave.getActiveEnemies()) {
            assertTrue(wave.removeEnemy(enemy));
        }
        assertTrue(wave.isCleared());
        assertFalse(wave.isActive());

        wave.startNextWave(enemies(2));
        assertEquals(2, wave.getWaveNumber());
        assertTrue(wave.isActive());
    }

    @Test
    void removingAnUnknownEnemyChangesNothing() {
        EnemyWave wave = new EnemyWave();
        wave.startNextWave(enemies(2));
        assertFalse(wave.removeEnemy(new Enemy(0, 0, 10, 10, 2)));
        assertEquals(2, wave.getActiveEnemies().size());
    }

    @Test
    void waveCopiesTheInputListAndReturnsReadOnlySnapshots() {
        EnemyWave wave = new EnemyWave();
        List<Enemy> spawned = enemies(2);
        wave.startNextWave(spawned);
        spawned.clear();
        assertEquals(2, wave.getActiveEnemies().size());

        List<Enemy> snapshot = wave.getActiveEnemies();
        assertThrows(UnsupportedOperationException.class, () -> snapshot.remove(0));

        // A snapshot taken earlier does not change when the wave does.
        wave.removeEnemy(snapshot.get(0));
        assertEquals(2, snapshot.size());
        assertEquals(1, wave.getActiveEnemies().size());
        assertSame(snapshot.get(1), wave.getActiveEnemies().get(0));
    }
}
