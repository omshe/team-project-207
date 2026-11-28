package use_case.update_game;

import entity.TileMap;

/** Supplies the update-game use case with the arena the player walks on. */
public interface UpdateGameDataAccessInterface {
    /**
     * Returns the arena the game is currently played on. Callers should ask again each tick
     * rather than keep the result, so that a newly loaded arena takes effect.
     *
     * @return the arena the player walks on; never null
     */
    TileMap getCurrentMap();
}
