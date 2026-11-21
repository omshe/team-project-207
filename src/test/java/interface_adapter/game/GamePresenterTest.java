package interface_adapter.game;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.update_game.UpdateGameOutputData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Checks that the presenter picks the right animation and counts its ticks. */
class GamePresenterTest {
    private GameViewModel viewModel;
    private GamePresenter presenter;

    @BeforeEach
    void setUp() {
        viewModel = new GameViewModel(new GameState(0, 0, 48, PlayerAnimation.IDLE, false, 0));
        presenter = new GamePresenter(viewModel);
    }

    @Test
    void standingStillPlaysIdleAndAdvancesItsTick() {
        presenter.present(new UpdateGameOutputData(0, 0, 48, false, false));
        presenter.present(new UpdateGameOutputData(0, 0, 48, false, false));

        assertEquals(PlayerAnimation.IDLE, viewModel.getState().getPlayerAnimation());
        assertEquals(2, viewModel.getState().getPlayerAnimationTick());
    }

    @Test
    void startingToMoveSwitchesToWalkFromTheFirstFrame() {
        presenter.present(new UpdateGameOutputData(0, 0, 48, false, false));
        presenter.present(new UpdateGameOutputData(4, 0, 48, true, false));

        assertEquals(PlayerAnimation.WALK, viewModel.getState().getPlayerAnimation());
        assertEquals(0, viewModel.getState().getPlayerAnimationTick());
    }

    @Test
    void positionIsRoundedAndFacingIsPassedThrough() {
        presenter.present(new UpdateGameOutputData(10.6, 20.4, 48, true, true));

        assertEquals(11, viewModel.getState().getPlayerX());
        assertEquals(20, viewModel.getState().getPlayerY());
        assertTrue(viewModel.getState().isPlayerFacingLeft());
    }
}
