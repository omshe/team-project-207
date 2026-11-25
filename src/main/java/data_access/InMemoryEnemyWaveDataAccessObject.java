package data_access;

import entity.EnemyWave;
import entity.Hitbox;
import entity.Player;
import entity.TileMap;
import use_case.generate_mob_waves.GenerateMobWavesDataAccessInterface;

/**
 * Keeps the mob-wave session state in memory for the length of one game, and answers terrain
 * questions from the loaded tile map.
 *
 * <p>The player is passed in rather than created here, so this object shares the exact
 * player instance that the movement use case moves. Nothing is saved to disk; persistence is
 * not part of this feature.
 */
public class InMemoryEnemyWaveDataAccessObject implements GenerateMobWavesDataAccessInterface {
    private final Player player;
    private final EnemyWave enemyWave;
    private final TileMap terrain;

    /**
     * Creates the session state for an open field with no terrain: every area inside the world
     * counts as walkable. Useful for tests and for wiring that has no map.
     *
     * @param player    the game's single player, shared with the movement use case
     * @param enemyWave the wave state to keep, normally a new {@link EnemyWave}
     */
    public InMemoryEnemyWaveDataAccessObject(Player player, EnemyWave enemyWave) {
        this(player, enemyWave, null);
    }

    /**
     * Creates the session state on the given terrain.
     *
     * @param player    the game's single player, shared with the movement use case
     * @param enemyWave the wave state to keep, normally a new {@link EnemyWave}
     * @param terrain   the loaded tile map whose walkability enemies obey, or {@code null} for
     *                  an open field
     */
    public InMemoryEnemyWaveDataAccessObject(Player player, EnemyWave enemyWave,
                                             TileMap terrain) {
        this.player = player;
        this.enemyWave = enemyWave;
        this.terrain = terrain;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public EnemyWave getEnemyWave() {
        return enemyWave;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Checks every tile the area covers, using {@link TileMap#isWalkable(double, double)}.
     * A box's right and bottom edges are exclusive, so a box that ends exactly on a tile
     * border does not count as standing on the next tile.
     */
    @Override
    public boolean isWalkable(Hitbox area) {
        if (terrain == null) {
            return true;
        }
        int tileSize = terrain.getTileSize();
        int firstColumn = (int) Math.floor(area.getX() / tileSize);
        int lastColumn = (int) Math.ceil(area.getRight() / tileSize) - 1;
        int firstRow = (int) Math.floor(area.getY() / tileSize);
        int lastRow = (int) Math.ceil(area.getBottom() / tileSize) - 1;

        for (int row = firstRow; row <= lastRow; row++) {
            for (int column = firstColumn; column <= lastColumn; column++) {
                // Ask about the tile's top-left pixel; outside the map is never walkable.
                if (!terrain.isWalkable((double) column * tileSize, (double) row * tileSize)) {
                    return false;
                }
            }
        }
        return true;
    }
}
