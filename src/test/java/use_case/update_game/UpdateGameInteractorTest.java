package use_case.update_game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import entity.Player;
import org.junit.jupiter.api.Test;

/** Regression tests: adding mob waves must not change how the player moves and is clamped. */
class UpdateGameInteractorTest {

    private static class RecordingPresenter implements UpdateGameOutputBoundary {
        private UpdateGameOutputData last;

        @Override
        public void present(UpdateGameOutputData outputData) {
            last = outputData;
        }
    }

    @Test
    void movesThePlayerAndPresentsItsPosition() {
        Player player = new Player(100, 100, 48, 4);
        RecordingPresenter presenter = new RecordingPresenter();
        UpdateGameInteractor interactor = new UpdateGameInteractor(player, 960, 640, presenter);

        interactor.execute(new UpdateGameInputData(1, 0));

        assertEquals(104, presenter.last.getPlayerX(), 1e-9);
        assertEquals(100, presenter.last.getPlayerY(), 1e-9);
        assertEquals(48, presenter.last.getPlayerSize());
    }

    @Test
    void keepsThePlayerInsideTheWorld() {
        Player player = new Player(0, 0, 48, 4);
        RecordingPresenter presenter = new RecordingPresenter();
        UpdateGameInteractor interactor = new UpdateGameInteractor(player, 960, 640, presenter);

        interactor.execute(new UpdateGameInputData(-1, -1));

        assertEquals(0, presenter.last.getPlayerX(), 0);
        assertEquals(0, presenter.last.getPlayerY(), 0);
    }
}
