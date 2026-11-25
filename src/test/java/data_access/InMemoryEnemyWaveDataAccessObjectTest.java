package data_access;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.DirtTileType;
import entity.EnemyWave;
import entity.Hitbox;
import entity.Player;
import entity.TileMap;
import entity.TileType;
import entity.WaterTileType;
import org.junit.jupiter.api.Test;

class InMemoryEnemyWaveDataAccessObjectTest {
    private final Player player = new Player(0, 0, 48, 4);
    private final EnemyWave wave = new EnemyWave();

    @Test
    void returnsTheSameSharedPlayerAndWave() {
        InMemoryEnemyWaveDataAccessObject dataAccess =
                new InMemoryEnemyWaveDataAccessObject(player, wave);

        assertSame(player, dataAccess.getPlayer());
        assertSame(wave, dataAccess.getEnemyWave());
    }

    @Test
    void withoutTerrainEveryAreaIsWalkable() {
        InMemoryEnemyWaveDataAccessObject dataAccess =
                new InMemoryEnemyWaveDataAccessObject(player, wave);
        assertTrue(dataAccess.isWalkable(new Hitbox(-50, -50, 10, 10)));
    }

    @Test
    void anAreaIsWalkableOnlyIfEveryTileUnderItIs() {
        // 3 x 2 tiles of 32 px; the middle tile of the top row is water.
        TileType dirt = new DirtTileType();
        TileType water = new WaterTileType();
        TileMap map = new TileMap(new TileType[][] {
            {dirt, water, dirt},
            {dirt, dirt, dirt},
        }, 32);
        InMemoryEnemyWaveDataAccessObject dataAccess =
                new InMemoryEnemyWaveDataAccessObject(player, wave, map);

        assertTrue(dataAccess.isWalkable(new Hitbox(0, 0, 32, 64)), "left column is dirt");
        assertTrue(dataAccess.isWalkable(new Hitbox(0, 32, 96, 32)), "bottom row is dirt");
        assertFalse(dataAccess.isWalkable(new Hitbox(31, 0, 2, 2)), "one pixel into water");
        assertFalse(dataAccess.isWalkable(new Hitbox(40, 20, 10, 20)), "partly over water");
        assertTrue(dataAccess.isWalkable(new Hitbox(64, 0, 32, 32)),
                "edges ending on a tile border do not reach the next tile");
        assertFalse(dataAccess.isWalkable(new Hitbox(80, 40, 20, 10)), "past the map's edge");
        assertFalse(dataAccess.isWalkable(new Hitbox(-1, 40, 10, 10)), "before the map's edge");
    }
}
