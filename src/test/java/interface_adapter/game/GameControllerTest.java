package interface_adapter.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.update_game.UpdateGameInputBoundary;
import use_case.update_game.UpdateGameInputData;

/** Checks that the controller turns held keys into the right move direction (no Swing, fake use case). */
class GameControllerTest {

    private static class RecordingUseCase implements UpdateGameInputBoundary {
        private UpdateGameInputData last;
        private int calls;

        @Override
        public void execute(UpdateGameInputData inputData) {
            last = inputData;
            calls++;
        }
    }

    private RecordingUseCase useCase;
    private GameController controller;

    @BeforeEach
    void setUp() {
        useCase = new RecordingUseCase();
        controller = new GameController(useCase);
    }

    @Test
    void noKeysMeansNoMovementButStillRunsTheTick() {
        controller.tick(false, false, false, false);

        assertEquals(1, useCase.calls);
        assertEquals(0, useCase.last.getMoveX());
        assertEquals(0, useCase.last.getMoveY());
    }

    @Test
    void eachKeyMapsToItsDirection() {
        controller.tick(false, false, false, true);
        assertEquals(1, useCase.last.getMoveX());

        controller.tick(false, false, true, false);
        assertEquals(-1, useCase.last.getMoveX());

        controller.tick(false, true, false, false);
        assertEquals(1, useCase.last.getMoveY());   // screen y grows downward

        controller.tick(true, false, false, false);
        assertEquals(-1, useCase.last.getMoveY());
    }

    @Test
    void twoKeysOnDifferentAxesMoveDiagonally() {
        controller.tick(true, false, false, true);

        assertEquals(1, useCase.last.getMoveX());
        assertEquals(-1, useCase.last.getMoveY());
    }

    @Test
    void oppositeKeysCancelOut() {
        controller.tick(true, true, true, true);

        assertEquals(0, useCase.last.getMoveX());
        assertEquals(0, useCase.last.getMoveY());
    }
}
