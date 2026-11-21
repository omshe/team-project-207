package interface_adapter.game;

import use_case.update_game.UpdateGameInputBoundary;
import use_case.update_game.UpdateGameInputData;

/** Turns raw key state into a use-case request. Called once per tick by the game loop. */
public class GameController {
    private final UpdateGameInputBoundary updateGame;

    /**
     * Creates the controller.
     *
     * @param updateGame the use case to call once per tick
     */
    public GameController(UpdateGameInputBoundary updateGame) {
        this.updateGame = updateGame;
    }

    /**
     * Turns this tick's held keys into a move direction and runs one game tick.
     * Opposite keys held together cancel out to no movement on that axis.
     *
     * @param up    whether an up key is held
     * @param down  whether a down key is held
     * @param left  whether a left key is held
     * @param right whether a right key is held
     */
    public void tick(boolean up, boolean down, boolean left, boolean right) {
        int moveX = 0;
        if (right) {
            moveX = moveX + 1;
        }
        if (left) {
            moveX = moveX - 1;   // if both are held, they cancel out to 0
        }

        int moveY = 0;
        if (down) {
            moveY = moveY + 1;   // Swing's y grows downward, so down is +1
        }
        if (up) {
            moveY = moveY - 1;
        }

        updateGame.execute(new UpdateGameInputData(moveX, moveY));
    }
}
