package entity;

/** Builds small terrain maps for tests, with 32-pixel tiles. */
public final class TestTerrain {
    /** Tile size of every map built here, in pixels. */
    public static final int TILE_SIZE = 32;

    private TestTerrain() {
    }

    /**
     * Builds a map that is dirt everywhere, so only its edges block movement.
     *
     * @param columns number of columns; must be positive
     * @param rows    number of rows; must be positive
     * @return a walkable map of the given size
     */
    public static TileMap openGround(int columns, int rows) {
        TileType[][] tiles = new TileType[rows][columns];
        TileType dirt = new DirtTileType();
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < columns; col++) {
                tiles[row][col] = dirt;
            }
        }
        return new TileMap(tiles, TILE_SIZE);
    }

    /**
     * Builds a map from rows of characters, where {@code '1'} is dirt and {@code '0'} is water.
     *
     * @param rows one string per row, all the same length, top row first
     * @return the described map
     */
    public static TileMap fromRows(String... rows) {
        TileType dirt = new DirtTileType();
        TileType water = new WaterTileType();
        TileType[][] tiles = new TileType[rows.length][];
        for (int row = 0; row < rows.length; row++) {
            tiles[row] = new TileType[rows[row].length()];
            for (int col = 0; col < rows[row].length(); col++) {
                if (rows[row].charAt(col) == '0') {
                    tiles[row][col] = water;
                } else {
                    tiles[row][col] = dirt;
                }
            }
        }
        return new TileMap(tiles, TILE_SIZE);
    }
}
