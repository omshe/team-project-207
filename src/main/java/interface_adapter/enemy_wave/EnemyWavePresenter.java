package interface_adapter.enemy_wave;

import java.util.ArrayList;
import java.util.List;
import use_case.generate_mob_waves.EnemyOutputData;
import use_case.generate_mob_waves.GenerateMobWavesOutputBoundary;
import use_case.generate_mob_waves.GenerateMobWavesOutputData;

/**
 * Turns each Generate Mob Waves result into a new {@link EnemyWaveState}: rounds positions to
 * whole pixels, writes the wave heading and message text, and counts presented ticks for
 * animation timing. It holds no game rules.
 */
public class EnemyWavePresenter implements GenerateMobWavesOutputBoundary {
    private final EnemyWaveViewModel viewModel;
    private long presentedTicks;

    /**
     * Creates the presenter.
     *
     * @param viewModel where new snapshots are published
     */
    public EnemyWavePresenter(EnemyWaveViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(GenerateMobWavesOutputData outputData) {
        publish(outputData, "");
    }

    @Override
    public void prepareFailView(GenerateMobWavesOutputData outputData, String errorMessage) {
        publish(outputData, errorMessage);
    }

    /** Builds and publishes one snapshot. Called exactly once per simulation tick. */
    private void publish(GenerateMobWavesOutputData outputData, String statusMessage) {
        presentedTicks = presentedTicks + 1;

        List<EnemyDisplayData> enemies = new ArrayList<>();
        for (EnemyOutputData enemy : outputData.getEnemies()) {
            enemies.add(new EnemyDisplayData(
                    (int) Math.round(enemy.getX()),
                    (int) Math.round(enemy.getY()),
                    (int) Math.round(enemy.getWidth()),
                    (int) Math.round(enemy.getHeight()),
                    enemy.isMoving(),
                    enemy.isFacingLeft()));
        }

        viewModel.setState(new EnemyWaveState(formatWaveLabel(outputData.getWaveNumber()),
                statusMessage, enemies, presentedTicks));
    }

    /** Wave 0 means no wave has started, so there is no heading to show. */
    private static String formatWaveLabel(int waveNumber) {
        if (waveNumber == 0) {
            return "";
        }
        return "Wave " + waveNumber;
    }
}
