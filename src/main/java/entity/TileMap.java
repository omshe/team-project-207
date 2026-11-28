package entity;

/** An immutable rectangular grid of TileType implementations with tile and pixel lookups. */
public final class TileMap {
    private final TileType[][] tiles;
    private final int tileSize;

    /**
     * Copies a terrain grid so that changes to the supplied arrays cannot change this map.
     * Terrain objects are shared and must obey TileType's immutability contract.
     *
     * @param tiles terrain arranged as rows, then columns
     * @param tileSize width and height of each square tile in pixels
     * @throws IllegalArgumentException if tileSize is not positive, the grid is null, empty,
     *                                  uneven or contains nulls, or pixel dimensions exceed an int
     */
    public TileMap(TileType[][] tiles, int tileSize) {
        if (tileSize <= 0) {
            throw new IllegalArgumentException("Tile size must be positive.");
        }
        if (tiles == null || tiles.length == 0) {
            throw new IllegalArgumentException("A tile map must contain at least one row.");
        }
        if (tiles[0] == null || tiles[0].length == 0) {
            throw new IllegalArgumentException("A tile map must contain at least one column.");
        }

        int columns = tiles[0].length;
        if ((long) columns * tileSize > Integer.MAX_VALUE
                || (long) tiles.length * tileSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Map pixel dimensions exceed the supported range.");
        }

        this.tileSize = tileSize;
        this.tiles = new TileType[tiles.length][columns];
        for (int row = 0; row < tiles.length; row++) {
            if (tiles[row] == null || tiles[row].length != columns) {
                throw new IllegalArgumentException(
                        "Row " + row + " must contain " + columns + " tiles.");
            }
            for (int col = 0; col < columns; col++) {
                if (tiles[row][col] == null) {
                    throw new IllegalArgumentException(
                            "Tile at column " + col + ", row " + row + " is null.");
                }
                this.tiles[row][col] = tiles[row][col];
            }
        }
    }

    /** Returns the number of columns in the grid. */
    public int getColumnCount() {
        return tiles[0].length;
    }

    /** Returns the number of rows in the grid. */
    public int getRowCount() {
        return tiles.length;
    }

    /** Returns the width and height of a tile in pixels. */
    public int getTileSize() {
        return tileSize;
    }

    /** Returns the map width in pixels. */
    public int getWidthInPixels() {
        return getColumnCount() * tileSize;
    }

    /** Returns the map height in pixels. */
    public int getHeightInPixels() {
        return getRowCount() * tileSize;
    }

    /**
     * Gets a tile using zero-based column and row indices.
     *
     * @param col column index, increasing to the right
     * @param row row index, increasing downward
     * @return the terrain at that grid position
     * @throws IndexOutOfBoundsException if either index is outside the grid
     */
    public TileType getTile(int col, int row) {
        if (col < 0 || col >= getColumnCount() || row < 0 || row >= getRowCount()) {
            throw new IndexOutOfBoundsException(
                    "Tile coordinates out of bounds: (" + col + ", " + row + ")");
        }
        return tiles[row][col];
    }

    /**
     * Finds the tile containing a pixel position measured from the map's top-left corner.
     * A tile includes its top and left edges, but excludes its bottom and right edges.
     *
     * @param x horizontal position in pixels
     * @param y vertical position in pixels
     * @return the terrain containing the position
     * @throws IndexOutOfBoundsException if the position is outside the map or is not finite
     */
    public TileType tileAtPixel(double x, double y) {
        if (!containsPixel(x, y)) {
            throw new IndexOutOfBoundsException(
                    "Pixel coordinates out of bounds: (" + x + ", " + y + ")");
        }
        return getTile((int) (x / tileSize), (int) (y / tileSize));
    }

    /**
     * Checks terrain walkability at a pixel position; this does not move any entity.
     *
     * @param x horizontal position in pixels
     * @param y vertical position in pixels
     * @return the terrain's walking rule, or false for positions outside the map
     *         and non-finite coordinates
     */
    public boolean isWalkable(double x, double y) {
        return containsPixel(x, y) && tileAtPixel(x, y).isWalkable();
    }

    /**
     * Limits a horizontal move so that the mover never overlaps non-walkable terrain or leaves
     * the map.
     *
     * <p>The mover may stop flush against blocked terrain or the map edge: sharing an edge is
     * allowed, overlapping is not. Only tiles the mover would newly enter are checked, so a mover
     * that already overlaps blocked terrain can still walk off it. Overlaps no thicker than
     * {@link Hitbox#CONTACT_TOLERANCE} are ignored, matching the hitbox contact rule. Nothing is
     * moved; the caller applies the result.
     *
     * @param mover the mover's hitbox before the move; expected to lie inside the map
     * @param deltaX the requested move in pixels; positive is right and negative is left
     * @return {@code deltaX} when the path is clear, otherwise the distance to the first blocking
     *     edge, which has the same sign as {@code deltaX} or is zero
     */
    public double limitHorizontalMove(Hitbox mover, double deltaX) {
        int firstRow = firstLineOverlapped(mover.getY());
        int lastRow = lastLineOverlapped(mover.getBottom());
        return limitMove(mover.getX(), mover.getRight(), deltaX, true, firstRow, lastRow);
    }

    /**
     * Limits a vertical move so that the mover never overlaps non-walkable terrain or leaves the
     * map. It follows the same rules as {@link #limitHorizontalMove(Hitbox, double)}.
     *
     * @param mover the mover's hitbox before the move; expected to lie inside the map
     * @param deltaY the requested move in pixels; positive is down and negative is up
     * @return {@code deltaY} when the path is clear, otherwise the distance to the first blocking
     *     edge, which has the same sign as {@code deltaY} or is zero
     */
    public double limitVerticalMove(Hitbox mover, double deltaY) {
        int firstColumn = firstLineOverlapped(mover.getX());
        int lastColumn = lastLineOverlapped(mover.getRight());
        return limitMove(mover.getY(), mover.getBottom(), deltaY, false, firstColumn, lastColumn);
    }

    private boolean containsPixel(double x, double y) {
        return x >= 0 && x < getWidthInPixels() && y >= 0 && y < getHeightInPixels();
    }

    /**
     * Walks the columns (or rows) the mover's leading edge would enter, nearest first, and stops
     * at the first blocked one. {@code near} and {@code far} are the mover's edges on the axis of
     * travel, and a "line" is one column or row index on that axis. {@code alongX} is true for a
     * horizontal move (lines are columns) and false for a vertical one (lines are rows).
     * {@code firstAcross} and {@code lastAcross} are the rows (or columns) the mover covers.
     */
    private double limitMove(double near, double far, double delta, boolean alongX,
                             int firstAcross, int lastAcross) {
        double tolerance = Hitbox.CONTACT_TOLERANCE;
        if (delta > 0) {
            int line = (int) Math.ceil((far - tolerance) / tileSize);
            while ((double) line * tileSize < far + delta - tolerance) {
                if (isLineBlocked(line, alongX, firstAcross, lastAcross)) {
                    return Math.max(0, (double) line * tileSize - far);
                }
                line++;
            }
            return delta;
        }
        if (delta < 0) {
            int line = (int) Math.floor((near + tolerance) / tileSize) - 1;
            while ((double) (line + 1) * tileSize > near + delta + tolerance) {
                if (isLineBlocked(line, alongX, firstAcross, lastAcross)) {
                    return Math.min(0, (double) (line + 1) * tileSize - near);
                }
                line--;
            }
            return delta;
        }
        return 0;
    }

    /** Returns the first column or row index that a span starting at {@code start} overlaps. */
    private int firstLineOverlapped(double start) {
        return (int) Math.floor((start + Hitbox.CONTACT_TOLERANCE) / tileSize);
    }

    /** Returns the last column or row index that a span ending at {@code end} overlaps. */
    private int lastLineOverlapped(double end) {
        return (int) Math.ceil((end - Hitbox.CONTACT_TOLERANCE) / tileSize) - 1;
    }

    /**
     * Checks one column (when {@code alongX}) or one row (otherwise) between the given rows or
     * columns, and returns true if any tile there is not walkable.
     */
    private boolean isLineBlocked(int line, boolean alongX, int firstAcross, int lastAcross) {
        if (alongX) {
            return isColumnBlocked(line, firstAcross, lastAcross);
        }
        return isRowBlocked(line, firstAcross, lastAcross);
    }

    private boolean isColumnBlocked(int column, int firstRow, int lastRow) {
        for (int row = firstRow; row <= lastRow; row++) {
            if (!isTileWalkable(column, row)) {
                return true;
            }
        }
        return false;
    }

    private boolean isRowBlocked(int row, int firstColumn, int lastColumn) {
        for (int column = firstColumn; column <= lastColumn; column++) {
            if (!isTileWalkable(column, row)) {
                return true;
            }
        }
        return false;
    }

    /** Grid positions outside the map count as blocked. */
    private boolean isTileWalkable(int column, int row) {
        boolean insideGrid = column >= 0 && column < getColumnCount()
                && row >= 0 && row < getRowCount();
        return insideGrid && tiles[row][column].isWalkable();
    }
}
