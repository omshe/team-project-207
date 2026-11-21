package use_case.update_game;

/** Receives the result of a tick. Implemented by the presenter. */
public interface UpdateGameOutputBoundary {
    /**
     * Receives the game's state after one tick. Called once per tick, on the game-loop thread.
     * Implementations prepare it for display and must not change the game.
     *
     * @param outputData the player's position, size, movement and facing after the tick; never null
     */
    void present(UpdateGameOutputData outputData);
}
