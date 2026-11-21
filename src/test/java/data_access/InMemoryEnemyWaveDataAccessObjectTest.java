package data_access;

import static org.junit.jupiter.api.Assertions.assertSame;

import entity.EnemyWave;
import entity.Player;
import org.junit.jupiter.api.Test;

class InMemoryEnemyWaveDataAccessObjectTest {

    @Test
    void returnsTheSameSharedPlayerAndWave() {
        Player player = new Player(0, 0, 48, 4);
        EnemyWave wave = new EnemyWave();
        InMemoryEnemyWaveDataAccessObject dataAccess =
                new InMemoryEnemyWaveDataAccessObject(player, wave);

        assertSame(player, dataAccess.getPlayer());
        assertSame(wave, dataAccess.getEnemyWave());
    }
}
