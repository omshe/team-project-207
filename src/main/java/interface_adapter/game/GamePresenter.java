package interface_adapter.game;

import use_case.update_game.UpdateGameOutputBoundary;
import use_case.update_game.UpdateGameOutputData;

/**
 * Turns a tick's result into a new GameState for the view: rounds positions to pixels,
 * picks the player's animation, and counts how long that animation has been playing.
 */
public class GamePresenter implements UpdateGameOutputBoundary {
    private final GameViewModel viewModel;
    private PlayerAnimation currentAnimation;
    private int animationTick;

    /**
     * Creates the presenter. The animation and its tick count continue from the view model's
     * current state.
     *
     * @param viewModel where each new GameState is written
     */
    public GamePresenter(GameViewModel viewModel) {
        this.viewModel = viewModel;
        this.currentAnimation = viewModel.getState().getPlayerAnimation();
        this.animationTick = viewModel.getState().getPlayerAnimationTick();
    }

    @Override
    public void present(UpdateGameOutputData outputData) {
        PlayerAnimation animation = PlayerAnimation.IDLE;
        if (outputData.isPlayerMoving()) {
            animation = PlayerAnimation.WALK;
        }

        if (animation == currentAnimation) {
            animationTick++;
        } else {
            // restart from the first frame when the animation changes
            currentAnimation = animation;
            animationTick = 0;
        }

        viewModel.setState(new GameState(
                (int) Math.round(outputData.getPlayerX()),
                (int) Math.round(outputData.getPlayerY()),
                outputData.getPlayerSize(),
                currentAnimation,
                outputData.isPlayerFacingLeft(),
                animationTick));
    }
}
