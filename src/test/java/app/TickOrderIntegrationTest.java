package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.InMemoryEnemyWaveDataAccessObject;
import entity.EnemyWave;
import entity.Player;
import interface_adapter.enemy_wave.EnemyDisplayData;
import interface_adapter.enemy_wave.EnemyWaveController;
import interface_adapter.enemy_wave.EnemyWavePresenter;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import interface_adapter.game.GameController;
import interface_adapter.game.GamePresenter;
import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;
import org.junit.jupiter.api.Test;
import use_case.generate_mob_waves.EdgeSpawnPositionStrategy;
import use_case.generate_mob_waves.GenerateMobWavesInteractor;
import use_case.generate_mob_waves.MobWaveSettings;
import use_case.update_game.UpdateGameInteractor;

/**
 * Wires the real player and mob-wave classes the same way {@code Main} does (minus Swing) and
 * runs ticks in the game loop's order: player controller first, then the mob-wave controller.
 */
class TickOrderIntegrationTest {
    private final Player player = new Player(456, 296, 48, 4);
    private final GameViewModel gameViewModel = new GameViewModel(new GameState(456, 296, 48));
    private final EnemyWaveViewModel enemyWaveViewModel = new EnemyWaveViewModel();
    private final GameController gameController = new GameController(
            new UpdateGameInteractor(player, 960, 640, new GamePresenter(gameViewModel)));
    private final EnemyWaveController enemyWaveController = new EnemyWaveController(
            new GenerateMobWavesInteractor(
                    new InMemoryEnemyWaveDataAccessObject(player, new EnemyWave()),
                    new EnemyWavePresenter(enemyWaveViewModel),
                    new EdgeSpawnPositionStrategy(16),
                    new MobWaveSettings(3, 56, 34, 2),
                    960, 640));

    private void tick(boolean up, boolean down, boolean left, boolean right) {
        gameController.tick(up, down, left, right);
        enemyWaveController.tick(up, down, left, right);
    }

    @Test
    void idleThenMoveStartsTheWaveAndBothScreensUpdate() {
        for (int index = 0; index < 30; index++) {
            tick(false, false, false, false);
        }
        assertTrue(enemyWaveViewModel.getState().getEnemies().isEmpty());

        tick(false, false, false, true);

        assertEquals("Wave 1", enemyWaveViewModel.getState().getWaveLabel());
        assertEquals(3, enemyWaveViewModel.getState().getEnemies().size());
        // The mob presenter did not disturb the player's view model.
        assertEquals(460, gameViewModel.getState().getPlayerX());
        assertEquals(296, gameViewModel.getState().getPlayerY());
    }

    @Test
    void oppositeKeysAloneNeverStartTheWave() {
        for (int index = 0; index < 30; index++) {
            tick(false, false, true, true);
        }
        assertEquals("", enemyWaveViewModel.getState().getWaveLabel());
    }

    @Test
    void enemiesChaseThePositionThePlayerHasAfterThisTicksMove() {
        tick(false, false, false, true);   // start wave 1
        EnemyDisplayData leftEnemyBefore = enemyWaveViewModel.getState().getEnemies().get(0);

        // Player runs straight down. The left-edge enemy must already bend downward on the
        // same tick, which only happens if it chases the *updated* player position.
        for (int index = 0; index < 20; index++) {
            tick(false, true, false, false);
        }

        EnemyDisplayData leftEnemyAfter = enemyWaveViewModel.getState().getEnemies().get(0);
        assertTrue(leftEnemyAfter.getX() > leftEnemyBefore.getX());
        assertTrue(leftEnemyAfter.getY() > leftEnemyBefore.getY());
        assertTrue(leftEnemyAfter.isMoving());
    }

    @Test
    void enemiesEventuallyStopAtThePlayerAndTheWaveStays() {
        tick(false, false, false, true);
        for (int index = 0; index < 1000; index++) {
            tick(false, false, false, false);
        }
        for (EnemyDisplayData enemy : enemyWaveViewModel.getState().getEnemies()) {
            assertTrue(!enemy.isMoving());
        }
        assertEquals("Wave 1", enemyWaveViewModel.getState().getWaveLabel());
        assertEquals(3, enemyWaveViewModel.getState().getEnemies().size());
    }
}
