package app;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import data_access.FileMapDataAccessObject;
import data_access.InMemoryEnemyWaveDataAccessObject;
import entity.DirtTileType;
import entity.Enemy;
import entity.EnemyWave;
import entity.Hitbox;
import entity.Player;
import entity.TileMap;
import entity.TileTypeRegistry;
import entity.WaterTileType;
import interface_adapter.enemy_wave.EnemyWavePresenter;
import interface_adapter.enemy_wave.EnemyWaveViewModel;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.generate_mob_waves.EdgeSpawnPositionStrategy;
import use_case.generate_mob_waves.GenerateMobWavesInputData;
import use_case.generate_mob_waves.GenerateMobWavesInteractor;
import use_case.generate_mob_waves.MobWaveSettings;

/**
 * Runs mob waves on the real {@code level1.txt} arena with the game's real numbers (56 x 34
 * slimes, speed 2, 2 px spacing), checking the rules that must hold on every tick.
 */
class MobWaveArenaIntegrationTest {
    private static final int TILE_SIZE = 32;
    private static final double SPACING = 2;

    private TileMap terrain;
    private InMemoryEnemyWaveDataAccessObject dataAccess;
    private Player player;
    private EnemyWave wave;
    private GenerateMobWavesInteractor interactor;

    @BeforeEach
    void setUp() throws Exception {
        terrain = new FileMapDataAccessObject(new TileTypeRegistry(
                List.of(new WaterTileType(), new DirtTileType())), TILE_SIZE).load("level1.txt");
        player = new Player(336, 216, 48, 4);
        setUpWithPlayer();
    }

    private void setUpWithPlayer() {
        wave = new EnemyWave();
        dataAccess = new InMemoryEnemyWaveDataAccessObject(player, wave, terrain);
        interactor = new GenerateMobWavesInteractor(dataAccess,
                new EnemyWavePresenter(new EnemyWaveViewModel()),
                new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(3, 56, 34, 2, SPACING), 960, 640);
    }

    private Hitbox playerHitbox() {
        return new Hitbox(player.getX(), player.getY(), player.getSize(), player.getSize());
    }

    /**
     * Checks every per-tick rule: on land, inside the world, not stacked. The player may walk
     * into a slime (player movement ignores enemies), so the no-overlap-with-player rule is
     * only checked when the player stands still.
     */
    private void assertRulesHold(int tick, boolean playerStill) {
        List<Enemy> living = wave.getActiveEnemies();
        for (int first = 0; first < living.size(); first++) {
            Hitbox box = living.get(first).getHitbox();
            assertTrue(dataAccess.isWalkable(box), "on water at tick " + tick);
            assertTrue(box.isInsideWorld(960, 640), "left the world at tick " + tick);
            if (playerStill) {
                assertFalse(box.overlaps(playerHitbox()), "walked into the player at tick " + tick);
            }
            for (int second = first + 1; second < living.size(); second++) {
                assertTrue(box.separationFrom(living.get(second).getHitbox())
                        >= SPACING - 1e-6, "slimes stacked at tick " + tick);
            }
        }
    }

    @Test
    void slimesSurroundStillPlayerWithoutStackingOrEnteringWater() {
        player = new Player(456, 296, 48, 4);
        setUpWithPlayer();
        interactor.execute(new GenerateMobWavesInputData(1, 0));
        for (int tick = 0; tick < 1200; tick++) {
            interactor.execute(new GenerateMobWavesInputData(0, 0));
            assertRulesHold(tick, true);
        }
        for (Enemy enemy : wave.getActiveEnemies()) {
            assertTrue(enemy.getHitbox().touches(playerHitbox()));
        }
    }

    @Test
    void slimesKeepTheRulesWhileThePlayerRunsLaps() {
        interactor.execute(new GenerateMobWavesInputData(1, 0));
        // The player runs square laps through the arena's open middle; enemies keep chasing.
        int[][] legs = {{1, 0, 60}, {0, 1, 40}, {-1, 0, 60}, {0, -1, 40}};
        int tick = 0;
        for (int lap = 0; lap < 10; lap++) {
            for (int[] leg : legs) {
                for (int step = 0; step < leg[2]; step++) {
                    player.move(leg[0], leg[1]);
                    interactor.execute(new GenerateMobWavesInputData(leg[0], leg[1]));
                    assertRulesHold(tick, false);
                    tick = tick + 1;
                }
            }
        }
    }

    /** Chases a still target with one 56 x 34 slime on the real terrain. */
    private int ticksToReach(Hitbox target, double startX, double startY, int maxTicks) {
        Enemy slime = new Enemy(startX, startY, 56, 34, 2);
        for (int tick = 0; tick < maxTicks; tick++) {
            if (slime.getHitbox().touches(target)) {
                return tick;
            }
            slime.pursue(target, (current, next) -> next.isInsideWorld(960, 640)
                    && dataAccess.isWalkable(next));
        }
        return -1;
    }

    @Test
    void slimeRoundsTheUpperRightLakeInsteadOfJitteringAtItsCorner() {
        // Regression (Codex review, 2026-11-23): this slime used to oscillate near (833, 245)
        // forever, though there is an open route above the lake.
        assertTrue(ticksToReach(new Hitbox(650, 50, 48, 48), 888, 303, 2000) >= 0);
    }

    @Test
    void fromEverySpawnSpotSlimeReachesEveryReachablePlayerSpot() {
        double[][] spawns = {{16, 303}, {888, 303}, {452, 16}, {452, 590},
            {16, 16}, {888, 16}, {16, 590}, {888, 590}};
        int cases = 0;
        for (int playerX = 0; playerX <= 912; playerX += 32) {
            for (int playerY = 0; playerY <= 592; playerY += 32) {
                Hitbox target = new Hitbox(playerX, playerY, 48, 48);
                if (!dataAccess.isWalkable(target)) {
                    continue;
                }
                for (double[] spawn : spawns) {
                    if (new Hitbox(spawn[0], spawn[1], 56, 34).touches(target)) {
                        continue;
                    }
                    cases = cases + 1;
                    assertTrue(ticksToReach(target, spawn[0], spawn[1], 4000) >= 0,
                            "stuck: player " + playerX + "," + playerY + " spawn "
                                    + spawn[0] + "," + spawn[1]);
                }
            }
        }
        assertTrue(cases > 3000);
    }

}
