package use_case.update_game;

import entity.Player;

/**
 * Runs one tick of the game rules. Right now: move the player and keep them inside the world.
 * Later steps (enemies, spells, collisions, waves) are added here in order.
 */
public class UpdateGameInteractor implements UpdateGameInputBoundary {
    private final Player player;
    private final int worldWidth;
    private final int worldHeight;
    private final UpdateGameOutputBoundary presenter;

    /**
     * Creates the interactor.
     *
     * @param player      the player to move each tick
     * @param worldWidth  width of the world the player must stay inside, in pixels
     * @param worldHeight height of the world the player must stay inside, in pixels
     * @param presenter   receives the result of every tick
     */
    public UpdateGameInteractor(Player player, int worldWidth, int worldHeight,
                                UpdateGameOutputBoundary presenter) {
        this.player = player;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        this.presenter = presenter;
    }

    @Override
    public void execute(UpdateGameInputData inputData) {
        player.move(inputData.getMoveX(), inputData.getMoveY());
        player.clampTo(worldWidth, worldHeight);

        presenter.present(new UpdateGameOutputData(player.getX(), player.getY(), player.getSize(),
                player.isMoving(), player.isFacingLeft()));
    }
}
