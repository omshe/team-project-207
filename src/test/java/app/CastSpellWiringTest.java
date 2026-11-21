package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.InMemoryCastSpellDataAccessObject;
import data_access.InMemoryEnemyWaveDataAccessObject;
import entity.Enemy;
import entity.EnemyWave;
import entity.FireballSpell;
import entity.Player;
import interface_adapter.cast_spell.CastSpellController;
import interface_adapter.cast_spell.CastSpellPresenter;
import interface_adapter.cast_spell.CastSpellViewModel;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import interface_adapter.enemy_wave.EnemyWaveController;
import interface_adapter.enemy_wave.EnemyWavePresenter;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import interface_adapter.game.GameController;
import interface_adapter.game.GamePresenter;
import interface_adapter.game.GameState;
import interface_adapter.game.GameViewModel;
import interface_adapter.game.PlayerAnimation;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.cast_spell.CastSpellInteractor;
import use_case.cast_spell.UpdateProjectilesInteractor;
import use_case.generate_mob_waves.EdgeSpawnPositionStrategy;
import use_case.generate_mob_waves.GenerateMobWavesInteractor;
import use_case.generate_mob_waves.MobWaveSettings;
import use_case.update_game.UpdateGameInteractor;
import view.KeyboardInput;
import view.MouseCastInput;

/** Exercises real casting, movement, and enemy classes through GameLoop's tick order. */
class CastSpellWiringTest {
    private final Player player = new Player(456, 296, 48, 4);
    private final EnemyWave wave = new EnemyWave();
    private final CastSpellViewModel castViewModel = new CastSpellViewModel();
    private final EnemyWaveViewModel enemyViewModel = new EnemyWaveViewModel();
    private final GameViewModel gameViewModel =
            new GameViewModel(new GameState(456, 296, 48,
                    PlayerAnimation.IDLE, player.isFacingLeft(), 0));
    private final InMemoryCastSpellDataAccessObject storage =
            new InMemoryCastSpellDataAccessObject(player, wave);
    private final KeyboardInput keyboard = new KeyboardInput();
    private JPanel surface;
    private GameLoop loop;

    /** Wires shared entities and an off-screen mouse-input surface as Main does. */
    @BeforeEach
    void setUp() throws Exception {
        CastSpellPresenter presenter = new CastSpellPresenter(castViewModel);
        FireballSpell fireball = new FireballSpell();

        CastSpellController casting = new CastSpellController(
                new CastSpellInteractor(storage, presenter,
                        Map.of(fireball.getId(), fireball), 8, 24, 16),
                new UpdateProjectilesInteractor(storage, presenter, 960, 640));

        GameController movement = new GameController(
                new UpdateGameInteractor(
                        player, 960, 640, new GamePresenter(gameViewModel)));

        EnemyWaveController enemies = new EnemyWaveController(
                new GenerateMobWavesInteractor(
                        new InMemoryEnemyWaveDataAccessObject(player, wave),
                        new EnemyWavePresenter(enemyViewModel),
                        new EdgeSpawnPositionStrategy(16),
                        new MobWaveSettings(3, 56, 34, 2), 960, 640));

        SwingUtilities.invokeAndWait(() -> {
            surface = new JPanel();
            surface.addMouseListener(new MouseCastInput(casting, fireball.getId()));
        });

        loop = new GameLoop(movement, enemies, casting, keyboard, surface);
    }

    /** Movement runs before launch, and a consumed press never repeats on later ticks. */
    @Test
    void clickLaunchesAfterPlayerMovementAndOnlyOnce() throws Exception {
        press(800, 320);
        SwingUtilities.invokeAndWait(() -> keyboard.keyPressed(new KeyEvent(
                surface, KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_D, 'd')));

        loop.tick();

        assertEquals(460, player.getX());
        assertEquals(460, gameViewModel.getState().getPlayerX());
        assertEquals(3, enemyViewModel.getState().getEnemies().size());
        assertEquals(1, storage.getProjectiles().size());
        assertEquals(492,
                castViewModel.getState().getProjectiles().get(0).getCenterX());
        assertEquals("Cast Fireball.", castViewModel.getState().getStatusMessage());

        loop.tick();

        assertEquals(1, storage.getProjectiles().size());
        assertEquals(500,
                castViewModel.getState().getProjectiles().get(0).getCenterX());
    }

    /** Casting sees the same enemies as pursuit and preserves the enemy after contact. */
    @Test
    void enemyContactRemovesFlightAndDeliversOneImpact() throws Exception {
        Enemy enemy = new Enemy(520, 310, 20, 20, 2);
        wave.startNextWave(List.of(enemy));
        press(800, 320);
        List<ProjectileImpactDisplayData> impacts = new ArrayList<>();

        for (int i = 0; i < 10; i++) {
            loop.tick();
            impacts.addAll(castViewModel.drainImpacts());
        }

        assertTrue(storage.getProjectiles().isEmpty());
        assertTrue(castViewModel.getState().getProjectiles().isEmpty());
        assertEquals(1, impacts.size());
        assertEquals("fireball", impacts.get(0).getSpellId());
        assertEquals(1, wave.getActiveEnemies().size());
        assertSame(enemy, wave.getActiveEnemies().get(0));
        assertTrue(enemy.getX() < 520);
    }

    /** Flight passes its original aim point and eventually exits without an impact. */
    @Test
    void flightContinuesPastAimUntilWorldExit() throws Exception {
        press(500, 320);

        for (int i = 0; i < 5; i++) {
            loop.tick();
        }
        assertTrue(
                castViewModel.getState().getProjectiles().get(0).getCenterX() > 500);

        for (int i = 0; i < 150; i++) {
            loop.tick();
            assertTrue(castViewModel.drainImpacts().isEmpty());
        }

        assertTrue(storage.getProjectiles().isEmpty());
        assertTrue(castViewModel.getState().getProjectiles().isEmpty());
    }

    /** Aiming at the player's center updates feedback without launching a projectile. */
    @Test
    void coincidentAimShowsFailureFeedback() throws Exception {
        press(480, 320);

        loop.tick();

        assertTrue(storage.getProjectiles().isEmpty());
        assertEquals("Cannot determine a direction for that aim point.",
                castViewModel.getState().getStatusMessage());
    }

    /** Several clicks can create coexisting projectiles through the real pipeline. */
    @Test
    void multipleClicksLaunchIndependentProjectiles() throws Exception {
        press(800, 320);
        press(480, 600);

        loop.tick();

        assertEquals(2, storage.getProjectiles().size());
        assertEquals(488,
                castViewModel.getState().getProjectiles().get(0).getCenterX());
        assertEquals(328,
                castViewModel.getState().getProjectiles().get(1).getCenterY());
    }

    /** Dispatches input on Swing's thread without running a game tick there. */
    private void press(int x, int y) throws Exception {
        SwingUtilities.invokeAndWait(() -> surface.dispatchEvent(new MouseEvent(
                surface, MouseEvent.MOUSE_PRESSED, 0, 0, x, y, 1, false,
                MouseEvent.BUTTON1)));
    }
}
