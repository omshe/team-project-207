package use_case.generate_mob_waves;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Enemy;
import entity.EnemyWave;
import entity.Hitbox;
import entity.Player;
import entity.TestTerrain;
import entity.TileMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the Generate Mob Waves interactor with no Swing, files or sleeps. Each call to
 * {@code tick} is one simulated game tick.
 */
class GenerateMobWavesInteractorTest {
    private static final int WORLD_WIDTH = 960;
    private static final int WORLD_HEIGHT = 640;
    private static final int PLAYER_SIZE = 48;
    private static final double ENEMY_SPEED = 2;
    private static final TileMap OPEN_GROUND = TestTerrain.openGround(30, 20);

    private Player player;
    private EnemyWave wave;
    private RecordingPresenter presenter;
    private InMemorySession session;
    private GenerateMobWavesInteractor interactor;

    /** Records every result the interactor sends out. */
    private static class RecordingPresenter implements GenerateMobWavesOutputBoundary {
        private final List<GenerateMobWavesOutputData> successes = new ArrayList<>();
        private final List<String> failures = new ArrayList<>();
        private GenerateMobWavesOutputData lastOutput;
        private String lastFailure;
        private int calls;

        @Override
        public void prepareSuccessView(GenerateMobWavesOutputData outputData) {
            calls = calls + 1;
            successes.add(outputData);
            lastOutput = outputData;
            lastFailure = null;
        }

        @Override
        public void prepareFailView(GenerateMobWavesOutputData outputData, String errorMessage) {
            calls = calls + 1;
            failures.add(errorMessage);
            lastOutput = outputData;
            lastFailure = errorMessage;
        }
    }

    /** In-memory stand-in for the data access layer. */
    private static class InMemorySession implements GenerateMobWavesDataAccessInterface {
        private final Player player;
        private final EnemyWave wave;
        private final List<Hitbox> unwalkableAreas = new ArrayList<>();

        InMemorySession(Player player, EnemyWave wave) {
            this.player = player;
            this.wave = wave;
        }

        @Override
        public Player getPlayer() {
            return player;
        }

        @Override
        public EnemyWave getEnemyWave() {
            return wave;
        }

        @Override
        public boolean isWalkable(Hitbox area) {
            for (Hitbox blocked : unwalkableAreas) {
                if (area.overlaps(blocked)) {
                    return false;
                }
            }
            return true;
        }
    }

    /** Returns a scripted list of candidates per call and records what it was asked. */
    private static class ScriptedStrategy implements SpawnPositionStrategy {
        private final List<List<Hitbox>> responses = new ArrayList<>();
        private int calls;
        private double askedWidth;
        private double askedHeight;
        private double askedWorldWidth;
        private double askedWorldHeight;
        private Hitbox askedPlayer;

        void thenReturn(List<Hitbox> candidates) {
            responses.add(candidates);
        }

        @Override
        public List<Hitbox> proposeCandidates(double enemyWidth, double enemyHeight,
                                              double worldWidth, double worldHeight,
                                              Hitbox playerHitbox) {
            askedWidth = enemyWidth;
            askedHeight = enemyHeight;
            askedWorldWidth = worldWidth;
            askedWorldHeight = worldHeight;
            askedPlayer = playerHitbox;
            int index = Math.min(calls, responses.size() - 1);
            calls = calls + 1;
            return responses.get(index);
        }
    }

    @BeforeEach
    void setUp() {
        player = new Player((WORLD_WIDTH - PLAYER_SIZE) / 2.0, (WORLD_HEIGHT - PLAYER_SIZE) / 2.0,
                PLAYER_SIZE, 4);
        wave = new EnemyWave();
        presenter = new RecordingPresenter();
        session = new InMemorySession(player, wave);
        interactor = interactorWith(new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(3, 48, 48, ENEMY_SPEED), WORLD_WIDTH, WORLD_HEIGHT);
    }

    private GenerateMobWavesInteractor interactorWith(SpawnPositionStrategy strategy,
                                                      MobWaveSettings settings,
                                                      double worldWidth, double worldHeight) {
        return new GenerateMobWavesInteractor(session, presenter,
                strategy, settings, worldWidth, worldHeight);
    }

    private void tick(int moveX, int moveY) {
        interactor.execute(new GenerateMobWavesInputData(moveX, moveY));
    }

    private void idleTicks(int count) {
        for (int index = 0; index < count; index++) {
            tick(0, 0);
        }
    }

    private Hitbox playerHitbox() {
        return new Hitbox(player.getX(), player.getY(), PLAYER_SIZE, PLAYER_SIZE);
    }

    private void removeAllEnemies() {
        for (Enemy enemy : wave.getActiveEnemies()) {
            wave.removeEnemy(enemy);
        }
    }

    // ----- Trigger (main step 1, extension 1a) -----

    @Test
    void idleTicksBeforeTheEncounterSpawnNothing() {
        idleTicks(120);
        assertFalse(wave.hasStarted());
        assertEquals(120, presenter.successes.size());
        assertEquals(0, presenter.lastOutput.getWaveNumber());
        assertTrue(presenter.lastOutput.getEnemies().isEmpty());
        assertTrue(presenter.failures.isEmpty());
    }

    @Test
    void firstMovementStartsWaveOneWithTheConfiguredCount() {
        tick(1, 0);
        assertEquals(1, wave.getWaveNumber());
        assertEquals(3, wave.getActiveEnemies().size());
        assertEquals(1, presenter.lastOutput.getWaveNumber());
        assertEquals(3, presenter.lastOutput.getEnemies().size());
    }

    @Test
    void anyNonZeroDirectionCountsAsMovement() {
        tick(0, -1);
        assertTrue(wave.hasStarted());
    }

    @Test
    void holdingKeyNeverSpawnsAgain() {
        for (int index = 0; index < 600; index++) {
            tick(1, 1);
        }
        assertEquals(1, wave.getWaveNumber());
        assertEquals(3, wave.getActiveEnemies().size());
    }

    @Test
    void presentsExactlyOncePerTick() {
        idleTicks(5);
        tick(1, 0);
        idleTicks(5);
        assertEquals(11, presenter.calls);
    }

    // ----- Placement (main step 2) -----

    @Test
    void spawnedEnemiesAreInsideTheWorldAndApartFromThePlayerAndEachOther() {
        tick(1, 0);
        List<Enemy> enemies = wave.getActiveEnemies();
        for (int first = 0; first < enemies.size(); first++) {
            Hitbox box = enemies.get(first).getHitbox();
            assertTrue(box.isInsideWorld(WORLD_WIDTH, WORLD_HEIGHT));
            assertFalse(box.touches(playerHitbox()));
            for (int second = first + 1; second < enemies.size(); second++) {
                assertFalse(box.touches(enemies.get(second).getHitbox()));
            }
        }
    }

    @Test
    void spawnedEnemiesUseTheConfiguredSizeAndSpeed() {
        interactor = interactorWith(new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(2, 56, 34, 1.5), WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        for (Enemy enemy : wave.getActiveEnemies()) {
            assertEquals(56, enemy.getWidth(), 0);
            assertEquals(34, enemy.getHeight(), 0);
            assertEquals(1.5, enemy.getSpeed(), 0);
        }
        assertEquals(2, wave.getActiveEnemies().size());
    }

    @Test
    void delegatesCandidateChoiceToTheStrategyAndKeepsItsOrder() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        Hitbox first = new Hitbox(10, 10, 48, 48);
        Hitbox second = new Hitbox(800, 10, 48, 48);
        Hitbox third = new Hitbox(10, 500, 48, 48);
        Hitbox unused = new Hitbox(800, 500, 48, 48);
        strategy.thenReturn(Arrays.asList(first, second, third, unused));
        interactor = interactorWith(strategy, new MobWaveSettings(3, 48, 48, 0),
                WORLD_WIDTH, WORLD_HEIGHT);

        tick(1, 0);

        assertEquals(1, strategy.calls);
        assertEquals(48, strategy.askedWidth, 0);
        assertEquals(48, strategy.askedHeight, 0);
        assertEquals(WORLD_WIDTH, strategy.askedWorldWidth, 0);
        assertEquals(WORLD_HEIGHT, strategy.askedWorldHeight, 0);
        assertEquals(player.getX(), strategy.askedPlayer.getX(), 0);
        List<Enemy> enemies = wave.getActiveEnemies();
        assertEquals(10, enemies.get(0).getX(), 0);
        assertEquals(800, enemies.get(1).getX(), 0);
        assertEquals(500, enemies.get(2).getY(), 0);
    }

    @Test
    void skipsCandidatesOutsideTheWorldTouchingThePlayerOrEachOther() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        Hitbox outside = new Hitbox(-5, 10, 48, 48);
        Hitbox onPlayer = new Hitbox(player.getX() + 48, player.getY(), 48, 48);  // shares an edge
        Hitbox good = new Hitbox(10, 10, 48, 48);
        Hitbox touchingGood = new Hitbox(58, 10, 48, 48);                         // shares an edge
        Hitbox alsoGood = new Hitbox(800, 500, 48, 48);
        strategy.thenReturn(Arrays.asList(outside, onPlayer, good, touchingGood, alsoGood));
        interactor = interactorWith(strategy, new MobWaveSettings(2, 48, 48, 0),
                WORLD_WIDTH, WORLD_HEIGHT);

        tick(1, 0);

        List<Enemy> enemies = wave.getActiveEnemies();
        assertEquals(2, enemies.size());
        assertEquals(10, enemies.get(0).getX(), 0);
        assertEquals(800, enemies.get(1).getX(), 0);
    }

    // ----- Not enough space (extension 2a) -----

    @Test
    void worldTooSmallForTheWaveSpawnsNothingAndReportsFailure() {
        // A 100 x 100 world around a 48 px player has no room for three 48 px enemies.
        player = new Player(26, 26, PLAYER_SIZE, 4);
        interactor = interactorWith(new EdgeSpawnPositionStrategy(0),
                new MobWaveSettings(3, 48, 48, ENEMY_SPEED), 100, 100);

        tick(1, 0);

        assertFalse(wave.hasStarted());
        assertTrue(wave.getActiveEnemies().isEmpty());
        assertEquals(1, presenter.failures.size());
        assertTrue(presenter.lastFailure.contains("Wave 1"));
        assertTrue(presenter.lastOutput.getEnemies().isEmpty());
        assertEquals(0, presenter.lastOutput.getWaveNumber());
    }

    @Test
    void partialFitNeverPlacesPartOfWave() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(Arrays.asList(new Hitbox(10, 10, 48, 48),
                new Hitbox(800, 10, 48, 48)));
        interactor = interactorWith(strategy, new MobWaveSettings(3, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);

        tick(1, 0);

        assertFalse(wave.hasStarted());
        assertTrue(wave.getActiveEnemies().isEmpty());
        assertNotNull(presenter.lastFailure);
    }

    @Test
    void emptyCandidateListFailsWithoutLooping() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(new ArrayList<>());
        interactor = interactorWith(strategy, new MobWaveSettings(3, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);

        tick(1, 0);

        assertFalse(wave.hasStarted());
        assertEquals(1, presenter.failures.size());
    }

    @Test
    void failedSpawnIsRetriedOnLaterTickThenSucceeds() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(new ArrayList<>());
        strategy.thenReturn(new ArrayList<>());
        strategy.thenReturn(Arrays.asList(new Hitbox(10, 10, 48, 48)));
        interactor = interactorWith(strategy, new MobWaveSettings(1, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);

        tick(1, 0);
        assertFalse(wave.hasStarted());
        // The player stops moving, but the triggered wave is still due, so it is retried.
        tick(0, 0);
        assertEquals(2, strategy.calls);
        assertFalse(wave.hasStarted());
        assertNotNull(presenter.lastFailure);
        tick(0, 0);

        assertEquals(3, strategy.calls);
        assertEquals(1, wave.getWaveNumber());
        assertEquals(2, presenter.failures.size());
        assertNull(presenter.lastFailure);
    }

    // ----- Chase and contact (main steps 4-6, extension 5a) -----

    @Test
    void enemiesMoveTowardThePlayerEveryTick() {
        tick(1, 0);
        List<Enemy> enemies = wave.getActiveEnemies();
        double[] distances = new double[enemies.size()];
        for (int index = 0; index < enemies.size(); index++) {
            distances[index] = centreDistance(enemies.get(index).getHitbox(), playerHitbox());
        }

        tick(0, 0);

        for (int index = 0; index < enemies.size(); index++) {
            double newDistance = centreDistance(enemies.get(index).getHitbox(), playerHitbox());
            assertEquals(distances[index] - ENEMY_SPEED, newDistance, 1e-9);
            assertTrue(enemies.get(index).isMoving());
        }
    }

    @Test
    void enemiesChaseThePlayersCurrentPositionAfterItMoves() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(Arrays.asList(new Hitbox(10, 296, 48, 48)));  // level with the player
        interactor = interactorWith(strategy, new MobWaveSettings(1, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        Enemy enemy = wave.getActiveEnemies().get(0);
        double beforeY = enemy.getY();

        // Player moves far down; the enemy's next step must bend downward.
        for (int index = 0; index < 50; index++) {
            player.move(0, 1, OPEN_GROUND);
        }
        tick(0, 1);

        assertTrue(enemy.getY() > beforeY);
    }

    @Test
    void enemiesStopAtContactWithoutOverlapAndStayWhileThePlayerStands() {
        tick(1, 0);
        boolean everyoneTouching = false;
        for (int index = 0; index < 1000 && !everyoneTouching; index++) {
            tick(0, 0);
            everyoneTouching = true;
            for (Enemy enemy : wave.getActiveEnemies()) {
                Hitbox box = enemy.getHitbox();
                double largestGap = Math.max(box.horizontalGapTo(playerHitbox()),
                        box.verticalGapTo(playerHitbox()));
                assertTrue(largestGap >= -Hitbox.CONTACT_TOLERANCE, "enemy overlapped player");
                everyoneTouching = everyoneTouching && box.touches(playerHitbox());
            }
        }
        assertTrue(everyoneTouching);

        idleTicks(30);
        for (Enemy enemy : wave.getActiveEnemies()) {
            assertFalse(enemy.isMoving());
        }
        for (EnemyOutputData shown : presenter.lastOutput.getEnemies()) {
            assertFalse(shown.isMoving());
        }
    }

    @Test
    void enemiesResumeChasingWhenThePlayerMovesAway() {
        tick(1, 0);
        idleTicks(1000);
        for (int index = 0; index < 30; index++) {
            player.move(0, 1, OPEN_GROUND);
        }
        tick(0, 1);
        boolean someoneMoved = false;
        for (Enemy enemy : wave.getActiveEnemies()) {
            someoneMoved = someoneMoved || enemy.isMoving();
        }
        assertTrue(someoneMoved);
    }

    // ----- Wave progression (main step 7, extension 7a) -----

    @Test
    void contactAloneNeverClearsTheWave() {
        tick(1, 0);
        idleTicks(2000);
        assertEquals(1, wave.getWaveNumber());
        assertEquals(3, wave.getActiveEnemies().size());
    }

    @Test
    void noNewWaveWhileAnyEnemyRemains() {
        tick(1, 0);
        List<Enemy> enemies = wave.getActiveEnemies();
        wave.removeEnemy(enemies.get(0));
        wave.removeEnemy(enemies.get(1));

        idleTicks(10);

        assertEquals(1, wave.getWaveNumber());
        assertEquals(1, wave.getActiveEnemies().size());
        assertSame(enemies.get(2), wave.getActiveEnemies().get(0));
    }

    @Test
    void removingTheLastEnemyStartsTheNextWaveExactlyOnce() {
        tick(1, 0);
        removeAllEnemies();

        tick(0, 0);   // later waves do not need a movement request
        assertEquals(2, wave.getWaveNumber());
        assertEquals(3, wave.getActiveEnemies().size());
        assertEquals(2, presenter.lastOutput.getWaveNumber());

        idleTicks(20);
        assertEquals(2, wave.getWaveNumber());
    }

    @Test
    void laterWaveThatCannotFitReportsTheDueWaveNumber() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(Arrays.asList(new Hitbox(10, 10, 48, 48)));
        strategy.thenReturn(new ArrayList<>());
        interactor = interactorWith(strategy, new MobWaveSettings(1, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        removeAllEnemies();

        tick(0, 0);

        assertEquals(1, wave.getWaveNumber());
        assertTrue(presenter.lastFailure.contains("Wave 2"));
        assertEquals(1, presenter.lastOutput.getWaveNumber());
        assertTrue(presenter.lastOutput.getEnemies().isEmpty());
    }

    // ----- Output -----

    @Test
    void outputIsCopyThatDoesNotChangeOnLaterTicks() {
        tick(1, 0);
        GenerateMobWavesOutputData firstOutput = presenter.lastOutput;
        double firstX = firstOutput.getEnemies().get(0).getX();

        idleTicks(10);

        assertEquals(firstX, firstOutput.getEnemies().get(0).getX(), 0);
        assertTrue(presenter.lastOutput.getEnemies().get(0).getX() != firstX);
    }

    private static double centreDistance(Hitbox first, Hitbox second) {
        double deltaX = first.getCenterX() - second.getCenterX();
        double deltaY = first.getCenterY() - second.getCenterY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    // ----- No stacking: living enemies keep their spacing -----

    /** Smallest separation between any two living enemies right now. */
    private double closestPairSeparation() {
        List<Enemy> enemies = wave.getActiveEnemies();
        double closest = Double.POSITIVE_INFINITY;
        for (int first = 0; first < enemies.size(); first++) {
            for (int second = first + 1; second < enemies.size(); second++) {
                closest = Math.min(closest, enemies.get(first).getHitbox()
                        .separationFrom(enemies.get(second).getHitbox()));
            }
        }
        return closest;
    }

    private static List<Hitbox> spots(Hitbox... boxes) {
        return new ArrayList<>(Arrays.asList(boxes));
    }

    @Test
    void chasingSlimesNeverComeCloserThanTheSpacingAndAllReachThePlayer() {
        tick(1, 0);
        for (int index = 0; index < 900; index++) {
            tick(0, 0);
            assertTrue(closestPairSeparation() >= MobWaveSettings.DEFAULT_ENEMY_SPACING - 1e-6,
                    "tick " + index + ": separation " + closestPairSeparation());
            for (Enemy enemy : wave.getActiveEnemies()) {
                assertFalse(enemy.getHitbox().overlaps(playerHitbox()));
            }
        }
        for (Enemy enemy : wave.getActiveEnemies()) {
            assertTrue(enemy.getHitbox().touches(playerHitbox()),
                    "did not reach the player: " + enemy.getHitbox().getX() + ", "
                            + enemy.getHitbox().getY());
        }
    }

    @Test
    void slimeBehindAnotherWalksAroundItToReachThePlayer() {
        // The player sits at (456, 296). Slime A touches its left side; slime B queues
        // directly behind A on the same line.
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(spots(new Hitbox(406, 296, 48, 48),
                new Hitbox(300, 296, 48, 48)));
        interactor = interactorWith(strategy, new MobWaveSettings(2, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        Enemy behind = wave.getActiveEnemies().get(1);

        for (int index = 0; index < 300; index++) {
            tick(0, 0);
            assertTrue(closestPairSeparation() >= 2 - 1e-6);
        }
        assertTrue(behind.getHitbox().touches(playerHitbox()));
    }

    @Test
    void slimesThatStartTooCloseCanStillMoveApart() {
        Enemy first = new Enemy(100, 100, 48, 48, ENEMY_SPEED);
        Enemy second = new Enemy(149, 100, 48, 48, ENEMY_SPEED);
        wave.startNextWave(List.of(first, second));
        // The player is far to the right, so the right-hand slime may walk away.
        double before = second.getX();
        tick(0, 0);
        assertTrue(second.getX() > before);
        assertTrue(closestPairSeparation() >= 1 - 1e-6);
    }

    @Test
    void defeatedSlimesDoNotBlockLivingOnes() {
        Enemy corpse = new Enemy(408, 296, 48, 48, ENEMY_SPEED);
        Enemy walker = new Enemy(300, 296, 48, 48, ENEMY_SPEED);
        interactor = interactorWith(new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(3, 48, 48, ENEMY_SPEED, 2, 1000), WORLD_WIDTH, WORLD_HEIGHT);
        wave.startNextWave(List.of(corpse, walker));
        corpse.defeat();
        idleTicks(60);
        // The walker went straight through the corpse's spot to the player.
        assertTrue(walker.getHitbox().touches(playerHitbox()));
        assertEquals(408, walker.getX(), 1e-6);
        assertEquals(296, walker.getY(), 1e-6);
    }

    @Test
    void spawnSkipsSpotsCloserThanTheSpacingToChosenSpot() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(spots(new Hitbox(16, 16, 48, 48), new Hitbox(65, 16, 48, 48),
                new Hitbox(66, 16, 48, 48)));
        interactor = interactorWith(strategy, new MobWaveSettings(2, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        // Spawned at x = 66 (the 65 spot is 1 px from the first), then took one step.
        assertEquals(66, wave.getActiveEnemies().get(1).getX(), ENEMY_SPEED);
    }

    // ----- Terrain: slimes only stand on walkable tiles -----

    @Test
    void spawnSkipsSpotsOnUnwalkableTerrain() {
        session.unwalkableAreas.add(new Hitbox(0, 0, 100, 100));
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(spots(new Hitbox(16, 16, 48, 48), new Hitbox(200, 16, 48, 48)));
        interactor = interactorWith(strategy, new MobWaveSettings(1, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        // Spawned on the walkable spot at x = 200, then took one step.
        assertEquals(200, wave.getActiveEnemies().get(0).getX(), ENEMY_SPEED);
    }

    @Test
    void waveWaitsWhenNoSpawnSpotIsWalkable() {
        session.unwalkableAreas.add(new Hitbox(0, 0, WORLD_WIDTH, WORLD_HEIGHT));
        tick(1, 0);
        assertFalse(wave.hasStarted());
        assertNotNull(presenter.lastFailure);
    }

    @Test
    void slimesWalkAroundWaterInsteadOfThroughIt() {
        // A lake stands between the slime and the player.
        Hitbox lake = new Hitbox(200, 250, 96, 140);
        session.unwalkableAreas.add(lake);
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(spots(new Hitbox(16, 296, 48, 48)));
        interactor = interactorWith(strategy, new MobWaveSettings(1, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        Enemy slime = wave.getActiveEnemies().get(0);

        for (int index = 0; index < 600; index++) {
            tick(0, 0);
            assertFalse(slime.getHitbox().overlaps(lake), "stepped into water at tick " + index);
        }
        assertTrue(slime.getHitbox().touches(playerHitbox()));
    }

    @Test
    void slimesStayInsideTheWorldWhileDetouring() {
        session.unwalkableAreas.add(new Hitbox(100, 0, 40, 600));
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(spots(new Hitbox(16, 296, 48, 48)));
        interactor = interactorWith(strategy, new MobWaveSettings(1, 48, 48, ENEMY_SPEED),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        Enemy slime = wave.getActiveEnemies().get(0);
        for (int index = 0; index < 600; index++) {
            tick(0, 0);
            assertTrue(slime.getHitbox().isInsideWorld(WORLD_WIDTH, WORLD_HEIGHT));
        }
    }

    // ----- Defeat: shown for the defeat duration, then removed -----

    @Test
    void defeatedSlimesStayForTheDefeatDurationThenTheNextWaveStarts() {
        interactor = interactorWith(new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(3, 48, 48, ENEMY_SPEED, 2, 5), WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        List<Enemy> firstWave = wave.getActiveEnemies();
        for (Enemy enemy : firstWave) {
            enemy.defeat();
        }

        for (int shown = 1; shown <= 4; shown++) {
            tick(0, 0);
            assertEquals(1, wave.getWaveNumber());
            assertTrue(wave.getActiveEnemies().isEmpty());
            assertEquals(3, presenter.lastOutput.getEnemies().size());
            EnemyOutputData output = presenter.lastOutput.getEnemies().get(0);
            assertTrue(output.isDefeated());
            assertFalse(output.isMoving());
            assertEquals(shown, output.getTicksSinceDefeat());
        }

        tick(0, 0);
        assertEquals(2, wave.getWaveNumber());
        for (Enemy enemy : firstWave) {
            assertFalse(wave.getRemainingEnemies().contains(enemy));
        }
        assertEquals(3, presenter.lastOutput.getEnemies().size());
        assertFalse(presenter.lastOutput.getEnemies().get(0).isDefeated());
    }

    @Test
    void defeatedSlimesStopMoving() {
        tick(1, 0);
        Enemy enemy = wave.getActiveEnemies().get(0);
        enemy.defeat();
        double x = enemy.getX();
        double y = enemy.getY();
        tick(1, 0);
        assertEquals(x, enemy.getX(), 0);
        assertEquals(y, enemy.getY(), 0);
    }

    @Test
    void theWaveStaysActiveWhileAnyDefeatedSlimeIsStillFading() {
        interactor = interactorWith(new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(1, 48, 48, ENEMY_SPEED, 2, 10), WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        wave.getActiveEnemies().get(0).defeat();
        idleTicks(9);
        assertTrue(wave.isActive());
        assertEquals(1, wave.getWaveNumber());
        tick(0, 0);
        assertEquals(2, wave.getWaveNumber());
    }

    // ----- Difficulty: each wave can be bigger than the last -----

    private void defeatAndClearWave() {
        for (Enemy enemy : wave.getActiveEnemies()) {
            enemy.defeat();
        }
        tick(0, 0);
    }

    @Test
    void eachNewWaveHasOneMoreSlimeWhenConfigured() {
        interactor = interactorWith(new EdgeSpawnPositionStrategy(16),
                new MobWaveSettings(3, 48, 48, ENEMY_SPEED, 2, 0, 1, 6), WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        assertEquals(3, wave.getActiveEnemies().size());
        for (int expectedWave = 2; expectedWave <= 4; expectedWave++) {
            defeatAndClearWave();
            assertEquals(expectedWave, wave.getWaveNumber());
            assertEquals(expectedWave + 2, wave.getActiveEnemies().size());
            assertEquals(expectedWave + 2, presenter.lastOutput.getEnemies().size());
        }
        // The cap of 6 holds from wave 4 on.
        for (int expectedWave = 5; expectedWave <= 7; expectedWave++) {
            defeatAndClearWave();
            assertEquals(expectedWave, wave.getWaveNumber());
            assertEquals(6, wave.getActiveEnemies().size());
        }
    }

    @Test
    void waveTooBigForTheSpawnSpotsWaitsInsteadOfStartingPartly() {
        ScriptedStrategy strategy = new ScriptedStrategy();
        strategy.thenReturn(spots(new Hitbox(16, 16, 48, 48), new Hitbox(200, 16, 48, 48),
                new Hitbox(400, 16, 48, 48)));
        interactor = interactorWith(strategy,
                new MobWaveSettings(3, 48, 48, ENEMY_SPEED, 2, 0, 1, 10),
                WORLD_WIDTH, WORLD_HEIGHT);
        tick(1, 0);
        defeatAndClearWave();
        // Wave 2 needs four spots but only three exist.
        assertEquals(1, wave.getWaveNumber());
        assertTrue(wave.isCleared());
        assertNotNull(presenter.lastFailure);
        assertTrue(presenter.lastFailure.contains("Wave 2"));
    }
}
