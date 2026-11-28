package data_access;

import entity.TileMap;
import use_case.update_game.UpdateGameDataAccessInterface;

/**
 * Holds the arena the player walks on in memory. Startup gives it the same loaded map that is
 * drawn and that the enemies use, so the player collides with exactly the terrain on screen.
 */
public final class InMemoryUpdateGameDataAccessObject implements UpdateGameDataAccessInterface {
    private final TileMap arena;

    /**
     * Stores the arena to hand to the movement use case.
     *
     * @param arena the loaded map the player walks on
     * @throws IllegalArgumentException if {@code arena} is null
     */
    public InMemoryUpdateGameDataAccessObject(TileMap arena) {
        if (arena == null) {
            throw new IllegalArgumentException("Arena must not be null.");
        }
        this.arena = arena;
    }

    /** {@inheritDoc} */
    @Override
    public TileMap getCurrentMap() {
        return arena;
    }
}
