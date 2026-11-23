package use_case.generate_mob_waves;

import entity.Enemy;
import entity.EnemyWave;
import entity.Hitbox;
import entity.MovementSpace;
import entity.Player;
import java.util.ArrayList;
import java.util.List;

/**
 * Application rules for the Generate Mob Waves use case, run once per simulation tick after
 * the player has moved.
 *
 * <p>Each tick it does four things, in order:
 * <ol>
 *   <li><b>Clear finished defeats.</b> Each defeated enemy counts one more tick; once it has
 *       been shown for the configured defeat duration, it is removed from the wave.</li>
 *   <li><b>Start a wave if one is due.</b> The first wave is due on the first tick where the
 *       player asks to move (a non-zero direction). After that, a new wave is due only once
 *       the current wave has been cleared. A wave starts only if <em>all</em> of its enemies
 *       fit (inside the world, on walkable terrain, not touching the player, and spaced apart
 *       from each other); otherwise nothing is placed, the fail view is shown, and the next
 *       tick tries again.</li>
 *   <li><b>Chase.</b> Every living enemy pursues the player's current hitbox (see
 *       {@link Enemy#pursue(Hitbox, MovementSpace)}). A step is allowed only if the enemy stays
 *       inside the world, on walkable terrain, and does not come closer than the configured
 *       spacing to another living enemy. Blocked enemies walk around the obstacle.</li>
 *   <li><b>Present.</b> Send an immutable copy of the wave state, including defeated enemies
 *       that are still fading out, to the output boundary.</li>
 * </ol>
 *
 * <p>Contact never defeats an enemy. Combat (Cast a Spell) defeats enemies with
 * {@link Enemy#defeat()}; this use case removes them afterwards.
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

        // Step 1: remove defeated enemies that have finished fading out.
        clearFinishedDefeats(wave);

        // Step 2: start a wave if one is due.
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

        // Step 3: every living enemy chases the player's position for this tick. The list holds
        // the live enemies, so each one sees where the earlier ones have already moved.
        List<Enemy> livingEnemies = wave.getActiveEnemies();
        for (Enemy enemy : livingEnemies) {
            enemy.pursue(playerHitbox, movementSpaceFor(enemy, livingEnemies));
        }

        // Step 4: present.
        presenter.prepareSuccessView(buildOutputData(wave));
    }

    /**
     * Counts one more tick for every defeated enemy and removes those that have been shown for
     * the whole defeat duration.
     */
    private void clearFinishedDefeats(EnemyWave wave) {
        for (Enemy enemy : wave.getDefeatedEnemies()) {
            enemy.advanceDefeatTimer();
            if (enemy.getTicksSinceDefeat() >= settings.getDefeatDurationTicks()) {
                wave.removeEnemy(enemy);
            }
        }
    }

    /**
     * Builds the movement rules for one enemy for this tick.
     *
     * @param mover  the enemy about to move
     * @param others the living enemies; {@code mover} itself is skipped
     * @return a space that allows a step only inside the world, on walkable terrain, and
     *     without crowding another living enemy
     */
    private MovementSpace movementSpaceFor(Enemy mover, List<Enemy> others) {
        return (current, next) -> isStandable(next)
                && keepsSpacing(mover, current, next, others);
    }

    /** An enemy may stand anywhere fully inside the world on walkable terrain. */
    private boolean isStandable(Hitbox area) {
        return area.isInsideWorld(worldWidth, worldHeight) && dataAccess.isWalkable(area);
    }

    /**
     * Returns whether a step keeps the mover from crowding the other living enemies.
     *
     * <p>A step is fine if, after it, the mover is at least the spacing away from every other
     * enemy. If it would be closer than that to one of them, it is still allowed as long as it
     * does not bring the two any closer than they already are, so enemies that start too close
     * can always slide apart instead of freezing.
     */
    private boolean keepsSpacing(Enemy mover, Hitbox current, Hitbox next, List<Enemy> others) {
        double spacing = settings.getEnemySpacing();
        for (Enemy other : others) {
            if (other == mover) {
                continue;
            }
            Hitbox otherHitbox = other.getHitbox();
            double separationAfter = next.separationFrom(otherHitbox);
            if (separationAfter >= spacing - Hitbox.CONTACT_TOLERANCE) {
                continue;
            }
            double separationBefore = current.separationFrom(otherHitbox);
            if (separationAfter < separationBefore - Hitbox.CONTACT_TOLERANCE) {
                return false;
            }
        }
        return true;
    }

    /**
     * Decides whether a wave should start this tick.
     *
     * <p>Before the first wave, the trigger is the player's first movement request (use-case
     * step 1; extension 1a covers no request). That trigger is remembered, so if the first wave
     * cannot be placed, it stays due and is retried on every later tick, even while the player
     * stands still (extension 2a). After the first wave, a new wave is due only when the current
     * one is cleared (step 8; extension 8a covers enemies remaining, alive or dying).
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
     * @return up to {@code enemiesPerWave} valid, spaced-apart spots
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
     * A spawn spot is valid if it is fully inside the world on walkable terrain, does not touch
     * the player, and is at least the enemy spacing away from every spot already chosen (and
     * never touching one, even when the spacing is zero).
     */
    private boolean isValidSpawnSpot(Hitbox candidate, Hitbox playerHitbox,
                                     List<Hitbox> alreadyChosen) {
        if (!isStandable(candidate)) {
            return false;
        }
        if (candidate.touches(playerHitbox)) {
            return false;
        }
        for (Hitbox other : alreadyChosen) {
            if (candidate.touches(other)) {
                return false;
            }
            if (candidate.separationFrom(other)
                    < settings.getEnemySpacing() - Hitbox.CONTACT_TOLERANCE) {
                return false;
            }
        }
        return true;
    }

    /** The player is a square; this converts it to a hitbox without changing Player itself. */
    private static Hitbox hitboxOf(Player player) {
        return new Hitbox(player.getX(), player.getY(), player.getSize(), player.getSize());
    }

    /** Copies the live entities, alive or fading out, into immutable output values. */
    private static GenerateMobWavesOutputData buildOutputData(EnemyWave wave) {
        List<EnemyOutputData> enemyData = new ArrayList<>();
        for (Enemy enemy : wave.getRemainingEnemies()) {
            enemyData.add(new EnemyOutputData(enemy.getX(), enemy.getY(), enemy.getWidth(),
                    enemy.getHeight(), enemy.isMoving(), enemy.isFacingLeft(),
                    enemy.isDefeated(), enemy.getTicksSinceDefeat()));
        }
        return new GenerateMobWavesOutputData(wave.getWaveNumber(), enemyData);
    }
}
