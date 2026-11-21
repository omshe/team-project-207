package use_case.update_game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks one tick of the game rules using a fake presenter (no Swing): movement, clamping,
 * facing and moving state. Also guards that adding mob waves did not change player movement.
 */
class UpdateGameInteractorTest {

    private static class RecordingPresenter implements UpdateGameOutputBoundary {
        private UpdateGameOutputData last;

        @Override
        public void present(UpdateGameOutputData outputData) {
            last = outputData;
        }
    }

    private RecordingPresenter presenter;
    private UpdateGameInteractor interactor;

    @BeforeEach
    void setUp() {
        presenter = new RecordingPresenter();
        interactor = new UpdateGameInteractor(new Player(100, 100, 48, 4), 960, 640, presenter);
    }

    @Test
    void movesThePlayerAndPresentsItsPosition() {
        interactor.execute(new UpdateGameInputData(1, 0));

        assertEquals(104, presenter.last.getPlayerX(), 1e-9);
        assertEquals(100, presenter.last.getPlayerY(), 1e-9);
        assertEquals(48, presenter.last.getPlayerSize());
    }

    @Test
    void movingRightIsMovingAndFacesRight() {
        interactor.execute(new UpdateGameInputData(1, 0));

        assertTrue(presenter.last.isPlayerMoving());
        assertFalse(presenter.last.isPlayerFacingLeft());
    }

    @Test
    void movingLeftFacesLeft() {
        interactor.execute(new UpdateGameInputData(-1, 0));

        assertTrue(presenter.last.isPlayerFacingLeft());
    }

    @Test
    void movingStraightUpKeepsThePreviousFacing() {
        interactor.execute(new UpdateGameInputData(-1, 0));
        interactor.execute(new UpdateGameInputData(0, -1));

        assertTrue(presenter.last.isPlayerFacingLeft());
        assertTrue(presenter.last.isPlayerMoving());
    }

    @Test
    void noInputMeansNotMoving() {
        interactor.execute(new UpdateGameInputData(1, 0));
        interactor.execute(new UpdateGameInputData(0, 0));

        assertFalse(presenter.last.isPlayerMoving());
        assertEquals(104, presenter.last.getPlayerX(), 1e-9);
    }

    @Test
    void keepsThePlayerInsideTheTopLeftOfTheWorld() {
        UpdateGameInteractor edge = new UpdateGameInteractor(
                new Player(0, 0, 48, 4), 960, 640, presenter);

        edge.execute(new UpdateGameInputData(-1, -1));

        assertEquals(0, presenter.last.getPlayerX(), 0);
        assertEquals(0, presenter.last.getPlayerY(), 0);
    }

    @Test
    void keepsThePlayerInsideTheRightOfTheWorld() {
        UpdateGameInteractor edge = new UpdateGameInteractor(
                new Player(958, 0, 48, 4), 960, 640, presenter);

        edge.execute(new UpdateGameInputData(1, -1));

        assertEquals(960 - 48, presenter.last.getPlayerX(), 1e-9);
        assertEquals(0, presenter.last.getPlayerY(), 1e-9);
    }
}
