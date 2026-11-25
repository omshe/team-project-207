package interface_adapter.enemy_wave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.generate_mob_waves.EnemyOutputData;
import use_case.generate_mob_waves.GenerateMobWavesOutputData;

class EnemyWavePresenterTest {
    private final EnemyWaveViewModel viewModel = new EnemyWaveViewModel();
    private final EnemyWavePresenter presenter = new EnemyWavePresenter(viewModel);

    private static GenerateMobWavesOutputData output(int waveNumber, EnemyOutputData... enemies) {
        return new GenerateMobWavesOutputData(waveNumber, Arrays.asList(enemies));
    }

    @Test
    void viewModelStartsEmpty() {
        EnemyWaveState state = viewModel.getState();
        assertEquals("", state.getWaveLabel());
        assertEquals("", state.getStatusMessage());
        assertTrue(state.getEnemies().isEmpty());
        assertEquals(0, state.getAnimationTick());
    }

    @Test
    void successRoundsPositionsAndCopiesFlags() {
        presenter.prepareSuccessView(output(1,
                new EnemyOutputData(10.4, 20.6, 56, 34, true, true, false, 0)));

        EnemyDisplayData enemy = viewModel.getState().getEnemies().get(0);
        assertEquals(10, enemy.getX());
        assertEquals(21, enemy.getY());
        assertEquals(56, enemy.getWidth());
        assertEquals(34, enemy.getHeight());
        assertTrue(enemy.isMoving());
        assertTrue(enemy.isFacingLeft());
    }

    @Test
    void waveLabelIsBlankBeforeTheFirstWave() {
        presenter.prepareSuccessView(output(0));
        assertEquals("", viewModel.getState().getWaveLabel());

        presenter.prepareSuccessView(output(3));
        assertEquals("Wave 3", viewModel.getState().getWaveLabel());
        assertEquals("", viewModel.getState().getStatusMessage());
    }

    @Test
    void failViewShowsTheMessageAndTheGivenState() {
        presenter.prepareFailView(output(1), "Wave 2 could not start");
        EnemyWaveState state = viewModel.getState();
        assertEquals("Wave 2 could not start", state.getStatusMessage());
        assertEquals("Wave 1", state.getWaveLabel());
        assertTrue(state.getEnemies().isEmpty());

        presenter.prepareSuccessView(output(2));
        assertEquals("", viewModel.getState().getStatusMessage());
    }

    @Test
    void animationTickCountsPresentedTicks() {
        presenter.prepareSuccessView(output(0));
        presenter.prepareFailView(output(0), "x");
        presenter.prepareSuccessView(output(0));
        assertEquals(3, viewModel.getState().getAnimationTick());
    }

    @Test
    void earlierSnapshotsNeverChange() {
        presenter.prepareSuccessView(output(1,
                new EnemyOutputData(5, 5, 10, 10, true, false, false, 0)));
        EnemyWaveState first = viewModel.getState();

        presenter.prepareSuccessView(output(1,
                new EnemyOutputData(9, 9, 10, 10, false, true, false, 0)));
        EnemyWaveState second = viewModel.getState();

        assertNotSame(first, second);
        assertEquals(5, first.getEnemies().get(0).getX());
        assertTrue(first.getEnemies().get(0).isMoving());
        assertEquals(9, second.getEnemies().get(0).getX());
        assertFalse(second.getEnemies().get(0).isMoving());
    }

    @Test
    void stateCopiesItsListAndIsReadOnly() {
        List<EnemyDisplayData> enemies = new ArrayList<>();
        enemies.add(new EnemyDisplayData(1, 2, 3, 4, false, false, false, 0));
        EnemyWaveState state = new EnemyWaveState("Wave 1", "", enemies, 7);
        enemies.clear();

        assertEquals(1, state.getEnemies().size());
        assertThrows(UnsupportedOperationException.class, () -> state.getEnemies().clear());
    }

    @Test
    void passesDefeatValuesThroughUnchanged() {
        EnemyWaveViewModel viewModel = new EnemyWaveViewModel();
        EnemyWavePresenter presenter = new EnemyWavePresenter(viewModel);
        presenter.prepareSuccessView(new GenerateMobWavesOutputData(1, List.of(
                new EnemyOutputData(5, 5, 10, 10, false, true, true, 7),
                new EnemyOutputData(50, 5, 10, 10, true, false, false, 0))));

        EnemyDisplayData defeated = viewModel.getState().getEnemies().get(0);
        assertTrue(defeated.isDefeated());
        assertEquals(7, defeated.getTicksSinceDefeat());
        assertFalse(viewModel.getState().getEnemies().get(1).isDefeated());
    }
}
