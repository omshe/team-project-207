package interface_adapter.enemy_wave;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import use_case.generate_mob_waves.GenerateMobWavesInputBoundary;
import use_case.generate_mob_waves.GenerateMobWavesInputData;

class EnemyWaveControllerTest {

    /** Records the last request the controller sent. */
    private static class RecordingInputBoundary implements GenerateMobWavesInputBoundary {
        private GenerateMobWavesInputData lastInput;
        private int calls;

        @Override
        public void execute(GenerateMobWavesInputData inputData) {
            lastInput = inputData;
            calls = calls + 1;
        }
    }

    private final RecordingInputBoundary useCase = new RecordingInputBoundary();
    private final EnemyWaveController controller = new EnemyWaveController(useCase);

    private void assertDirection(int expectedX, int expectedY) {
        assertEquals(expectedX, useCase.lastInput.getMoveX());
        assertEquals(expectedY, useCase.lastInput.getMoveY());
    }

    @Test
    void noKeysMeansNoDirection() {
        controller.tick(false, false, false, false);
        assertDirection(0, 0);
        assertEquals(1, useCase.calls);
    }

    @Test
    void singleKeysMapToScreenDirections() {
        controller.tick(false, false, false, true);
        assertDirection(1, 0);
        controller.tick(false, false, true, false);
        assertDirection(-1, 0);
        controller.tick(false, true, false, false);
        assertDirection(0, 1);
        controller.tick(true, false, false, false);
        assertDirection(0, -1);
    }

    @Test
    void oppositeKeysCancel() {
        controller.tick(false, false, true, true);
        assertDirection(0, 0);
        controller.tick(true, true, false, false);
        assertDirection(0, 0);
    }

    @Test
    void diagonalKeysCombine() {
        controller.tick(true, false, false, true);
        assertDirection(1, -1);
    }
}
