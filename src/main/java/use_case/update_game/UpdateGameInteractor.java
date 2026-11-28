package use_case.update_game;

import entity.Player;

/**
 * Runs one tick of player movement (Use Case 1, Move the Player): move the player over the
 * current arena, where non-walkable terrain blocks them, and keep them inside the world.
 */
public class UpdateGameInteractor implements UpdateGameInputBoundary {
    private final Player player;
    private final UpdateGameDataAccessInterface dataAccess;
    private final int worldWidth;
    private final int worldHeight;
    private final UpdateGameOutputBoundary presenter;

    /**
     * Creates the interactor.
     *
     * @param player      the player to move each tick
     * @param dataAccess  supplies the arena the player walks on; it must have a map loaded
     *                    before the first tick
     * @param worldWidth  width of the world the player must stay inside, in pixels
     * @param worldHeight height of the world the player must stay inside, in pixels
     * @param presenter   receives the result of every tick
     */
    public UpdateGameInteractor(Player player, UpdateGameDataAccessInterface dataAccess,
                                int worldWidth, int worldHeight,
                                UpdateGameOutputBoundary presenter) {
        this.player = player;
        this.dataAccess = dataAccess;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.presenter = presenter;
    }

    @Override
    public void execute(UpdateGameInputData inputData) {
        player.move(inputData.getMoveX(), inputData.getMoveY(), dataAccess.getCurrentMap());
        player.clampTo(worldWidth, worldHeight);

        presenter.present(new UpdateGameOutputData(player.getX(), player.getY(), player.getSize(),
                player.isMoving(), player.isFacingLeft()));
    }
}
