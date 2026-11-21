package app;

import data_access.InMemoryEnemyWaveDataAccessObject;
import entity.EnemyWave;
import entity.Player;
import interface_adapter.enemy_wave.EnemyWaveController;
import interface_adapter.enemy_wave.EnemyWavePresenter;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import interface_adapter.game.GameController;
import interface_adapter.game.GamePresenter;
import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;
import interface_adapter.game.PlayerAnimation;
import javax.swing.SwingUtilities;
import use_case.generate_mob_waves.EdgeSpawnPositionStrategy;
import use_case.generate_mob_waves.GenerateMobWavesInteractor;
import use_case.generate_mob_waves.MobWaveSettings;
import use_case.update_game.UpdateGameInteractor;
import view.EnemyWaveLayer;
import view.GamePanel;
import view.GameWindow;
import view.KeyboardInput;
import view.PlayerSprite;
import view.SlimeSprite;

/** Builds every object, connects them, and starts the game. The only class that knows them all. */
public class Main {
    private static final int SCREEN_WIDTH = 960;
    private static final int SCREEN_HEIGHT = 640;
    private static final int PLAYER_SIZE = 48;
    private static final double PLAYER_SPEED = 4;

    // Mob waves (tunable MVP defaults). The 56 x 34 hitbox matches the slime art at exactly
    // 2x scale, which keeps the pixel art crisp. Speed is half the player's, so they can outrun it.
    private static final int ENEMIES_PER_WAVE = 3;
    private static final double ENEMY_WIDTH = 56;
    private static final double ENEMY_HEIGHT = 34;
    private static final double ENEMY_SPEED = 2;
    private static final double SPAWN_EDGE_MARGIN = 16;

    /**
     * Starts the game.
     *
     * @param args ignored
     */
    public static void main(String[] args) {
        // Build the window on Swing's thread (Swing should only be used from that thread).
        SwingUtilities.invokeLater(() -> start());
    }

    private static void start() {
        // Entities
        Player player = new Player((SCREEN_WIDTH - PLAYER_SIZE) / 2.0,
                (SCREEN_HEIGHT - PLAYER_SIZE) / 2.0, PLAYER_SIZE, PLAYER_SPEED);

        // Interface adapters + use case
        GameViewModel viewModel = new GameViewModel(
                new GameState((int) player.getX(), (int) player.getY(), player.getSize(),
                        PlayerAnimation.IDLE, player.isFacingLeft(), 0));
        GamePresenter presenter = new GamePresenter(viewModel);
        UpdateGameInteractor updateGame = new UpdateGameInteractor(player, SCREEN_WIDTH,
                SCREEN_HEIGHT, presenter);
        final GameController controller = new GameController(updateGame);

        // Generate Mob Waves: shares the same player object, so enemies chase where it really is.
        EnemyWaveViewModel enemyWaveViewModel = new EnemyWaveViewModel();
        GenerateMobWavesInteractor generateMobWaves = new GenerateMobWavesInteractor(
                new InMemoryEnemyWaveDataAccessObject(player, new EnemyWave()),
                new EnemyWavePresenter(enemyWaveViewModel),
                new EdgeSpawnPositionStrategy(SPAWN_EDGE_MARGIN),
                new MobWaveSettings(ENEMIES_PER_WAVE, ENEMY_WIDTH, ENEMY_HEIGHT, ENEMY_SPEED),
                SCREEN_WIDTH, SCREEN_HEIGHT);
        final EnemyWaveController enemyWaveController = new EnemyWaveController(generateMobWaves);

        // Frameworks: Swing window, input, loop
        KeyboardInput keyboard = new KeyboardInput();
        EnemyWaveLayer enemyWaveLayer = new EnemyWaveLayer(enemyWaveViewModel, new SlimeSprite());
        GamePanel panel = new GamePanel(viewModel, PlayerSprite.wizard(), enemyWaveLayer,
                SCREEN_WIDTH, SCREEN_HEIGHT);
        panel.addKeyListener(keyboard);

        GameWindow window = new GameWindow(panel);
        window.setVisible(true);
        panel.requestFocusInWindow();

        new GameLoop(controller, enemyWaveController, keyboard, panel).start();
    }
}
