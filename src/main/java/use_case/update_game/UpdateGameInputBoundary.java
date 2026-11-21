package use_case.update_game;

/** Called once per tick by the controller to advance the game. */
public interface UpdateGameInputBoundary {
    /**
     * Advances the game by exactly one tick using this tick's input.
     * Callers may expect the result to be sent to the output boundary once, before this returns;
     * nothing is returned to the caller.
     *
     * @param inputData the movement the player asked for on this tick; must not be null
     */
    void execute(UpdateGameInputData inputData);
}
