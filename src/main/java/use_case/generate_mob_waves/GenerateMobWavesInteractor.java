package use_case.generate_mob_waves;

import entity.Enemy;
import entity.EnemyWave;
import entity.Hitbox;
import entity.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Application rules for the Generate Mob Waves use case, run once per simulation tick after
 * the player has moved.
 *
 * <p>Each tick it does three things, in order:
 * <ol>
 *   <li><b>Start a wave if one is due.</b> The first wave is due on the first tick where the
 *       player asks to move (a non-zero direction). After that, a new wave is due only once
 *       the current wave has been cleared. A wave starts only if <em>all</em> of its enemies
 *       fit (inside the world, not touching the player or each other); otherwise nothing is
 *       placed, the fail view is shown, and the next tick tries again.</li>
 *   <li><b>Chase.</b> Every active enemy pursues the player's current hitbox (see
 *       {@link Enemy#pursue(Hitbox)}).</li>
 *   <li><b>Present.</b> Send an immutable copy of the wave state to the output boundary.</li>
 * </ol>
 *
 * <p>Contact never removes an enemy. Enemies leave a wave only through
 * {@link EnemyWave#removeEnemy(Enemy)}, which future combat will call.
 */
public class GenerateMobWavesInteractor implements GenerateMobWavesInputBoundary {
    private final GenerateMobWavesDataAccessInterface dataAccess;
    private final GenerateMobWavesOutputBoundary presenter;
    private final SpawnPositionStrategy spawnPositionStrategy;
    private final MobWaveSettings settings;
    private final double worldWidth;
    private final double worldHeight;

    /**
     * Creates the interactor. All collaborators are injected so tests can substitute fakes.
     *
     * @param dataAccess            access to the shared player and the wave state
     * @param presenter             where each tick's result is sent
     * @param spawnPositionStrategy where to look for spawn spots
     * @param settings              enemy count, size and speed
     * @param worldWidth            world width, in pixels
     * @param worldHeight           world height, in pixels
     */
    public GenerateMobWavesInteractor(GenerateMobWavesDataAccessInterface dataAccess,
                                      GenerateMobWavesOutputBoundary presenter,
                                      SpawnPositionStrategy spawnPositionStrategy,
                                      MobWaveSettings settings,
                                      double worldWidth, double worldHeight) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
        this.spawnPositionStrategy = spawnPositionStrategy;
        this.settings = settings;
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    @Override
    public void execute(GenerateMobWavesInputData inputData) {
        EnemyWave wave = dataAccess.getEnemyWave();
        Hitbox playerHitbox = hitboxOf(dataAccess.getPlayer());

        // Step 1: start a wave if one is due.
        if (isWaveDue(wave, inputData)) {
            boolean waveStarted = tryStartWave(wave, playerHitbox);
            if (!waveStarted) {
                int dueWaveNumber = wave.getWaveNumber() + 1;
                presenter.prepareFailView(buildOutputData(wave),
                        "Wave " + dueWaveNumber + " could not start: not enough free space. "
                                + "Trying again.");
                return;
            }
        }

        // Step 2: every active enemy chases the player's position for this tick.
        for (Enemy enemy : wave.getActiveEnemies()) {
            enemy.pursue(playerHitbox);
        }

        // Step 3: present.
        presenter.prepareSuccessView(buildOutputData(wave));
    }

    /**
     * Decides whether a wave should start this tick.
     *
     * <p>Before the first wave, the trigger is the player's first movement request (use-case
     * step 1; extension 1a covers no request). That trigger is remembered, so if the first wave
     * cannot be placed, it stays due and is retried on every later tick, even while the player
     * stands still (extension 2a). After the first wave, a new wave is due only when the current
     * one is cleared (step 7; extension 7a covers enemies remaining).
     */
    private boolean isWaveDue(EnemyWave wave, GenerateMobWavesInputData inputData) {
        if (!wave.hasStarted()) {
            if (isMovementRequested(inputData)) {
                wave.triggerEncounter();
            }
            return wave.isEncounterTriggered();
        }
        return wave.isCleared();
    }

    private boolean isMovementRequested(GenerateMobWavesInputData inputData) {
        return inputData.getMoveX() != 0 || inputData.getMoveY() != 0;
    }

    /**
     * Tries to place a whole wave and start it.
     *
     * @return {@code true} if the wave started; {@code false} if not every enemy fit, in which
     *     case the wave state is unchanged (no partial wave)
     */
    private boolean tryStartWave(EnemyWave wave, Hitbox playerHitbox) {
        List<Hitbox> spawnSpots = chooseSpawnSpots(playerHitbox);
        if (spawnSpots.size() < settings.getEnemiesPerWave()) {
            return false;
        }

        List<Enemy> enemies = new ArrayList<>();
        for (Hitbox spot : spawnSpots) {
            enemies.add(new Enemy(spot.getX(), spot.getY(), settings.getEnemyWidth(),
                    settings.getEnemyHeight(), settings.getEnemySpeed()));
        }
        wave.startNextWave(enemies);
        return true;
    }

    /**
     * Picks valid spawn spots from the strategy's candidates, in the strategy's order.
     *
     * <p>The search is finite: it checks each candidate once and stops as soon as it has
     * enough spots.
     *
     * @return up to {@code enemiesPerWave} valid, mutually non-touching spots
     */
    private List<Hitbox> chooseSpawnSpots(Hitbox playerHitbox) {
        List<Hitbox> candidates = spawnPositionStrategy.proposeCandidates(
                settings.getEnemyWidth(), settings.getEnemyHeight(),
                worldWidth, worldHeight, playerHitbox);

        List<Hitbox> chosen = new ArrayList<>();
        for (Hitbox candidate : candidates) {
            if (chosen.size() == settings.getEnemiesPerWave()) {
                break;
            }
            if (isValidSpawnSpot(candidate, playerHitbox, chosen)) {
                chosen.add(candidate);
            }
        }
        return chosen;
    }

    /**
     * A spawn spot is valid if it is fully inside the world and touches neither the player
     * nor any spot already chosen.
     */
    private boolean isValidSpawnSpot(Hitbox candidate, Hitbox playerHitbox,
                                     List<Hitbox> alreadyChosen) {
        if (!candidate.isInsideWorld(worldWidth, worldHeight)) {
            return false;
        }
        if (candidate.touches(playerHitbox)) {
            return false;
        }
        for (Hitbox other : alreadyChosen) {
            if (candidate.touches(other)) {
                return false;
            }
        }
        return true;
    }

    /** The player is a square; this converts it to a hitbox without changing Player itself. */
    private static Hitbox hitboxOf(Player player) {
        return new Hitbox(player.getX(), player.getY(), player.getSize(), player.getSize());
    }

    /** Copies the live entities into immutable output values. */
    private static GenerateMobWavesOutputData buildOutputData(EnemyWave wave) {
        List<EnemyOutputData> enemyData = new ArrayList<>();
        for (Enemy enemy : wave.getActiveEnemies()) {
            enemyData.add(new EnemyOutputData(enemy.getX(), enemy.getY(), enemy.getWidth(),
                    enemy.getHeight(), enemy.isMoving(), enemy.isFacingLeft()));
        }
        return new GenerateMobWavesOutputData(wave.getWaveNumber(), enemyData);
    }
}
