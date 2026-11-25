package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Tests how a wave treats defeated enemies that have not been removed yet. */
class EnemyWaveDefeatTest {

    @Test
    void defeatedEnemiesLeaveTheActiveListButStayInTheWaveUntilRemoved() {
        Enemy first = new Enemy(0, 0, 10, 10, 2);
        Enemy second = new Enemy(50, 0, 10, 10, 2);
        EnemyWave wave = new EnemyWave();
        wave.startNextWave(List.of(first, second));

        first.defeat();

        assertEquals(List.of(second), wave.getActiveEnemies());
        assertEquals(List.of(first), wave.getDefeatedEnemies());
        assertEquals(List.of(first, second), wave.getRemainingEnemies());
        assertTrue(wave.isActive());

        second.defeat();
        assertTrue(wave.getActiveEnemies().isEmpty());
        assertTrue(wave.isActive(), "a wave is not cleared while defeated enemies remain");
        assertFalse(wave.isCleared());
        assertThrows(IllegalStateException.class,
                () -> wave.startNextWave(List.of(new Enemy(0, 0, 10, 10, 2))));

        assertTrue(wave.removeEnemy(first));
        assertTrue(wave.removeEnemy(second));
        assertTrue(wave.isCleared());
    }

    @Test
    void snapshotsAreReadOnly() {
        Enemy enemy = new Enemy(0, 0, 10, 10, 2);
        EnemyWave wave = new EnemyWave();
        wave.startNextWave(List.of(enemy));
        enemy.defeat();
        assertThrows(UnsupportedOperationException.class,
                () -> wave.getDefeatedEnemies().clear());
        assertThrows(UnsupportedOperationException.class,
                () -> wave.getRemainingEnemies().clear());
        assertSame(enemy, wave.getRemainingEnemies().get(0));
    }
}
