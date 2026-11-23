package app;

import data_access.FileMapDataAccessObject;
import data_access.InMemoryCastSpellDataAccessObject;
import data_access.InMemoryEnemyWaveDataAccessObject;
import entity.DirtTileType;
import entity.EnemyWave;
import entity.FireballSpell;
import entity.Player;
import entity.TileMap;
import entity.TileTypeRegistry;
import entity.WaterTileType;
import interface_adapter.cast_spell.CastSpellController;
import interface_adapter.cast_spell.CastSpellPresenter;
import interface_adapter.cast_spell.CastSpellViewModel;
import interface_adapter.enemy_wave.EnemyWaveController;
import interface_adapter.enemy_wave.EnemyWavePresenter;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import interface_adapter.game.GameController;
import interface_adapter.game.GamePresenter;
import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;
import interface_adapter.game.PlayerAnimation;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import use_case.cast_spell.CastSpellInteractor;
import use_case.cast_spell.UpdateProjectilesInteractor;
import use_case.generate_mob_waves.EdgeSpawnPositionStrategy;
import use_case.generate_mob_waves.GenerateMobWavesInteractor;
import use_case.generate_mob_waves.MobWaveSettings;
import use_case.load_map.MapLoadingException;
import use_case.update_game.UpdateGameInteractor;
import view.EnemyWaveLayer;
import view.FireballSprite;
import view.GamePanel;
import view.GameWindow;
import view.HitboxOverlayKey;
import view.KeyboardInput;
import view.MapLayer;
import view.MouseCastInput;
import view.PlayerSprite;
import view.ProjectileLayer;
import view.SlimeSprite;

/** Assembles the game, prepares its map, and starts it on Swing's event thread. */
public class Main {
    private static final int SCREEN_WIDTH = 960;
    private static final int SCREEN_HEIGHT = 640;
    private static final int PLAYER_SIZE = 48;
    private static final double PLAYER_SPEED = 4;
    private static final int TILE_SIZE = 32;
    private static final String STARTING_MAP = "level1.txt";

    // Mob waves (tunable MVP defaults). The 56 x 34 hitbox matches the slime art at exactly
    // 2x scale, which keeps the pixel art crisp. Speed is half the player's, so they can outrun it.
    private static final int ENEMIES_PER_WAVE = 3;
    private static final double ENEMY_WIDTH = 56;
    private static final double ENEMY_HEIGHT = 34;
    private static final double ENEMY_SPEED = 2;
    private static final double SPAWN_EDGE_MARGIN = 16;
    // Living slimes keep at least this many pixels between each other so they never stack.
    private static final double ENEMY_SPACING = 2;

    // Fireball settings (tunable MVP defaults).
    private static final double PROJECTILE_SPEED = 8;
    private static final double PROJECTILE_WIDTH = 24;
    private static final double PROJECTILE_HEIGHT = 16;

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
        // Load and prepare the map before opening the window or starting the loop.
        MapLayer mapLayer = new MapBuilder().build(STARTING_MAP, TILE_SIZE,
                Map.of(0, "/tiles/water.png", 1, "/tiles/dirt.png"),
                message -> GameWindow.showStartupError(message));
        if (mapLayer == null) {
            return;
        }
        TileMap enemyTerrain = loadEnemyTerrain();
        if (enemyTerrain == null) {
            return;
        }

        // Entities
        Player player = new Player((SCREEN_WIDTH - PLAYER_SIZE) / 2.0,
                (SCREEN_HEIGHT - PLAYER_SIZE) / 2.0, PLAYER_SIZE, PLAYER_SPEED);
        EnemyWave enemyWave = new EnemyWave();

        // Interface adapters + use case
        GameViewModel viewModel = new GameViewModel(
                new GameState((int) player.getX(), (int) player.getY(), player.getSize(),
                        PlayerAnimation.IDLE, player.isFacingLeft(), 0));
        GamePresenter presenter = new GamePresenter(viewModel);
        UpdateGameInteractor updateGame = new UpdateGameInteractor(player, SCREEN_WIDTH,
                SCREEN_HEIGHT, presenter);
        final GameController controller = new GameController(updateGame);

        // Generate Mob Waves: shares the same player object, so enemies chase where it really is.
        // A defeated slime stays exactly as long as its death animation takes to play once.
        SlimeSprite slimeSprite = new SlimeSprite();
        EnemyWaveViewModel enemyWaveViewModel = new EnemyWaveViewModel();
        GenerateMobWavesInteractor generateMobWaves = new GenerateMobWavesInteractor(
                new InMemoryEnemyWaveDataAccessObject(player, enemyWave, enemyTerrain),
                new EnemyWavePresenter(enemyWaveViewModel),
                new EdgeSpawnPositionStrategy(SPAWN_EDGE_MARGIN),
                new MobWaveSettings(ENEMIES_PER_WAVE, ENEMY_WIDTH, ENEMY_HEIGHT, ENEMY_SPEED,
                        ENEMY_SPACING, slimeSprite.getDeathAnimationTicks()),
                SCREEN_WIDTH, SCREEN_HEIGHT);
        final EnemyWaveController enemyWaveController = new EnemyWaveController(generateMobWaves);

        // Cast a Spell: shares the same player and enemy wave.
        CastSpellViewModel castSpellViewModel = new CastSpellViewModel();
        CastSpellPresenter castSpellPresenter = new CastSpellPresenter(castSpellViewModel);
        InMemoryCastSpellDataAccessObject castDataAccess =
                new InMemoryCastSpellDataAccessObject(player, enemyWave);
        FireballSpell fireball = new FireballSpell();
        CastSpellController castSpellController = new CastSpellController(
                new CastSpellInteractor(castDataAccess, castSpellPresenter,
                        Map.of(fireball.getId(), fireball), PROJECTILE_SPEED,
                        PROJECTILE_WIDTH, PROJECTILE_HEIGHT),
                new UpdateProjectilesInteractor(castDataAccess, castSpellPresenter,
                        SCREEN_WIDTH, SCREEN_HEIGHT));

        // Frameworks: Swing window, input, loop
        KeyboardInput keyboard = new KeyboardInput();
        EnemyWaveLayer enemyWaveLayer = new EnemyWaveLayer(enemyWaveViewModel, slimeSprite);
        ProjectileLayer projectileLayer = new ProjectileLayer(castSpellViewModel,
                Map.of(fireball.getId(), new FireballSprite()));
        GamePanel panel = new GamePanel(viewModel, PlayerSprite.wizard(), mapLayer, enemyWaveLayer,
                projectileLayer, SCREEN_WIDTH, SCREEN_HEIGHT);
        panel.addKeyListener(keyboard);
        panel.addKeyListener(new HitboxOverlayKey(enemyWaveLayer));
        panel.addMouseListener(new MouseCastInput(castSpellController, fireball.getId()));

        GameWindow window = new GameWindow(panel);
        window.setVisible(true);
        panel.requestFocusInWindow();

        new GameLoop(controller, enemyWaveController, castSpellController, keyboard, panel).start();
    }

    /**
     * Loads the starting map's terrain for the enemies' walkability checks. MapBuilder loads the
     * same file for drawing but keeps only display data, so the terrain is read again here.
     *
     * @return the terrain, or {@code null} after reporting an error
     */
    private static TileMap loadEnemyTerrain() {
        TileTypeRegistry tileTypes = new TileTypeRegistry(
                List.of(new WaterTileType(), new DirtTileType()));
        try {
            return new FileMapDataAccessObject(tileTypes, TILE_SIZE).load(STARTING_MAP);
        } catch (MapLoadingException error) {
            GameWindow.showStartupError(error.getMessage());
            return null;
        }
    }
}
