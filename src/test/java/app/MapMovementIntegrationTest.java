package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import data_access.InMemoryCastSpellDataAccessObject;
import data_access.InMemoryEnemyWaveDataAccessObject;
import entity.EnemyWave;
import entity.FireballSpell;
import entity.Player;
import interface_adapter.cast_spell.CastSpellController;
import interface_adapter.cast_spell.CastSpellPresenter;
import interface_adapter.cast_spell.CastSpellViewModel;
import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.enemy_wave.EnemyWaveController;
import interface_adapter.enemy_wave.EnemyWavePresenter;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import interface_adapter.game.GameController;
import interface_adapter.game.GamePresenter;
import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;
import interface_adapter.game.PlayerAnimation;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import use_case.cast_spell.CastSpellInteractor;
import use_case.cast_spell.TerminalImpactBehavior;
import use_case.cast_spell.UpdateProjectilesInteractor;
import use_case.generate_mob_waves.EdgeSpawnPositionStrategy;
import use_case.generate_mob_waves.GenerateMobWavesInteractor;
import use_case.generate_mob_waves.MobWaveSettings;
import use_case.update_game.UpdateGameInteractor;
import view.EnemyWaveLayer;
import view.FireballSprite;
import view.GamePanel;
import view.KeyboardInput;
import view.MapLayer;
import view.MouseCastInput;
import view.PlayerSprite;
import view.ProjectileLayer;
import view.SlimeSprite;

/** Exercises movement, wizard rendering, mob waves and casting over the production map. */
class MapMovementIntegrationTest {
    @Test
    void supportsWasdAndArrowKeysWithTheMapVisible() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapLayer layer = loadMap();
            int[][] cases = {
                    {KeyEvent.VK_W, 0, -4}, {KeyEvent.VK_UP, 0, -4},
                    {KeyEvent.VK_S, 0, 4}, {KeyEvent.VK_DOWN, 0, 4},
                    {KeyEvent.VK_A, -4, 0}, {KeyEvent.VK_LEFT, -4, 0},
                    {KeyEvent.VK_D, 4, 0}, {KeyEvent.VK_RIGHT, 4, 0}
            };
            for (int[] movement : cases) {
                Player player = new Player(456, 296, 48, 4);
                GameViewModel model = new GameViewModel(new GameState(456, 296, 48,
                        PlayerAnimation.IDLE, false, 0));
                GameController controller = new GameController(
                        new UpdateGameInteractor(player, 960, 640, new GamePresenter(model)));
                EnemyWaveViewModel enemies = new EnemyWaveViewModel();
                final EnemyWaveController enemyController = enemyController(player, enemies);
                GamePanel panel = new GamePanel(model, PlayerSprite.wizard(), layer,
                        new EnemyWaveLayer(enemies, new SlimeSprite()),
                        emptyProjectiles(), 960, 640);
                KeyboardInput keyboard = new KeyboardInput();

                keyboard.keyPressed(key(panel, KeyEvent.KEY_PRESSED, movement[0]));
                tick(controller, keyboard);
                tick(enemyController, keyboard);
                keyboard.keyReleased(key(panel, KeyEvent.KEY_RELEASED, movement[0]));
                tick(controller, keyboard);
                tick(enemyController, keyboard);

                int expectedX = 456 + movement[1];
                int expectedY = 296 + movement[2];
                assertEquals(expectedX, model.getState().getPlayerX());
                assertEquals(expectedY, model.getState().getPlayerY());
                assertEquals("Wave 1", enemies.getState().getWaveLabel());
                assertEquals(3, enemies.getState().getEnemies().size());
                BufferedImage image = paint(panel);
                assertWizardDrawn(image, model.getState());
                assertEquals(Color.BLUE.getRGB(), image.getRGB(24 * 32, 5 * 32));
                assertEquals(Color.GREEN.getRGB(), image.getRGB(0, 0));
            }
        });
    }

    @Test
    void doesNotAddPlayerWaterCollision() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            MapLayer layer = loadMap();
            Player player = new Player(700, 128, 48, 4);
            GameViewModel model = new GameViewModel(new GameState(700, 128, 48,
                    PlayerAnimation.IDLE, false, 0));
            GameController controller = new GameController(
                    new UpdateGameInteractor(player, 960, 640, new GamePresenter(model)));
            GamePanel panel = new GamePanel(model, PlayerSprite.wizard(), layer,
                    new EnemyWaveLayer(new EnemyWaveViewModel(), new SlimeSprite()),
                    emptyProjectiles(), 960, 640);
            KeyboardInput keyboard = new KeyboardInput();

            keyboard.keyPressed(key(panel, KeyEvent.KEY_PRESSED, KeyEvent.VK_D));
            tick(controller, keyboard);
            keyboard.keyReleased(key(panel, KeyEvent.KEY_RELEASED, KeyEvent.VK_D));

            assertEquals(704, model.getState().getPlayerX());
            BufferedImage image = paint(panel);
            assertWizardDrawn(image, model.getState());
            assertEquals(Color.BLUE.getRGB(), image.getRGB(832, 128));
        });
    }

    @Test
    void castsAndDrawsFireballsAcrossWaterThroughTheGameLoop() throws Exception {
        Player player = new Player(648, 136, 48, 4);
        EnemyWave wave = new EnemyWave();
        GameViewModel game = new GameViewModel(new GameState(648, 136, 48,
                PlayerAnimation.IDLE, false, 0));
        GameController movement = new GameController(
                new UpdateGameInteractor(player, 960, 640, new GamePresenter(game)));
        EnemyWaveViewModel enemies = new EnemyWaveViewModel();
        EnemyWaveController waves = enemyController(player, wave, enemies);
        CastSpellViewModel spells = new CastSpellViewModel();
        CastSpellPresenter presenter = new CastSpellPresenter(spells);
        InMemoryCastSpellDataAccessObject data =
                new InMemoryCastSpellDataAccessObject(player, wave);
        FireballSpell fireball = new FireballSpell();
        CastSpellController casting = new CastSpellController(
                new CastSpellInteractor(data, presenter, Map.of(fireball.getId(), fireball),
                        8, 24, 16),
                new UpdateProjectilesInteractor(data, presenter, 960, 640,
                        Map.of("fireball", new TerminalImpactBehavior())));
        GamePanel[] panel = new GamePanel[1];
        BufferedImage[] before = new BufferedImage[1];
        SwingUtilities.invokeAndWait(() -> {
            panel[0] = new GamePanel(game, PlayerSprite.wizard(), loadMap(),
                    new EnemyWaveLayer(enemies, new SlimeSprite()),
                    new ProjectileLayer(spells, Map.of(fireball.getId(), new FireballSprite())),
                    960, 640);
            panel[0].addMouseListener(new MouseCastInput(casting, fireball.getId()));
            before[0] = paint(panel[0]);
            panel[0].dispatchEvent(new MouseEvent(panel[0], MouseEvent.MOUSE_PRESSED,
                    0, 0, 850, 160, 1, false, MouseEvent.BUTTON1));
        });
        GameLoop loop = new GameLoop(movement, waves, casting, new KeyboardInput(), panel[0]);
        for (int tick = 0; tick < 8; tick++) {
            loop.tick();
        }

        assertEquals(1, spells.getState().getProjectiles().size());
        ProjectileDisplayData projectile = spells.getState().getProjectiles().get(0);
        assertEquals(736, projectile.getCenterX());
        assertEquals(160, projectile.getCenterY());
        assertEquals(Color.BLUE.getRGB(), before[0].getRGB(736, 160));
        SwingUtilities.invokeAndWait(() -> {
            BufferedImage after = paint(panel[0]);
            int changed = 0;
            for (int y = 145; y < 175; y++) {
                for (int x = 720; x < 752; x++) {
                    if (before[0].getRGB(x, y) != after.getRGB(x, y)) {
                        changed++;
                    }
                }
            }
            assertTrue(changed > 0, "The moving fireball should be visible over the water.");
        });
    }

    private static ProjectileLayer emptyProjectiles() {
        return new ProjectileLayer(new CastSpellViewModel(), Map.of());
    }

    private static void assertWizardDrawn(BufferedImage image, GameState state) {
        BufferedImage expected = new BufferedImage(960, 640, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = expected.createGraphics();
        try {
            PlayerSprite.wizard().draw(graphics, state);
        } finally {
            graphics.dispose();
        }
        int opaquePixels = 0;
        for (int y = 0; y < expected.getHeight(); y++) {
            for (int x = 0; x < expected.getWidth(); x++) {
                if ((expected.getRGB(x, y) >>> 24) == 255) {
                    assertEquals(expected.getRGB(x, y), image.getRGB(x, y));
                    opaquePixels++;
                }
            }
        }
        assertTrue(opaquePixels > 0, "The wizard should be drawn above the terrain.");
    }

    private static MapLayer loadMap() {
        return new MapBuilder().build("level1.txt", 32,
                Map.of(0, "/tiles/water.png", 1, "/tiles/dirt.png"), message -> fail(message));
    }

    private static KeyEvent key(GamePanel panel, int eventId, int keyCode) {
        return new KeyEvent(panel, eventId, 0, 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    private static EnemyWaveController enemyController(Player player, EnemyWaveViewModel model) {
        return enemyController(player, new EnemyWave(), model);
    }

    private static EnemyWaveController enemyController(Player player, EnemyWave wave,
                                                       EnemyWaveViewModel model) {
        return new EnemyWaveController(new GenerateMobWavesInteractor(
                new InMemoryEnemyWaveDataAccessObject(player, wave),
                new EnemyWavePresenter(model), new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(3, 56, 34, 2), 960, 640));
    }

    private static void tick(GameController controller, KeyboardInput keyboard) {
        controller.tick(keyboard.isUp(), keyboard.isDown(), keyboard.isLeft(), keyboard.isRight());
    }

    private static void tick(EnemyWaveController controller, KeyboardInput keyboard) {
        controller.tick(keyboard.isUp(), keyboard.isDown(), keyboard.isLeft(), keyboard.isRight());
    }

    private static BufferedImage paint(GamePanel panel) {
        panel.setSize(panel.getPreferredSize());
        BufferedImage image = new BufferedImage(
                panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            panel.paint(graphics);
        } finally {
            graphics.dispose();
        }
        return image;
    }
}
