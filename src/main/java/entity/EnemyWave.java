package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The state of the mob-wave encounter: which wave number the player is on and which enemies of
 * that wave are still active.
 *
 * <p>The encounter goes through these states:
 * <ol>
 *   <li><b>Not started</b>: wave number 0 and no enemies. Once the player triggers the
 *       encounter ({@link #triggerEncounter()}), the first wave is due until it starts.</li>
 *   <li><b>Active</b>: a wave has started and at least one of its enemies remains.</li>
 *   <li><b>Cleared</b>: a wave has started and all of its enemies have been removed. The next
 *       wave may then start, which makes the encounter active again.</li>
 * </ol>
 *
 * <p>Invariant: at most one wave is active at a time. {@link #startNextWave(List)} refuses to
 * start a wave while another is active.
 *
 * <p><b>Removal seam for combat.</b> {@link #removeEnemy(Enemy)} is how a future combat
 * feature (for example a spell hitting a mob) reports that an enemy was defeated. Nothing in
 * the current game calls it; touching the player does not remove an enemy.
 */
public class EnemyWave {
    private final List<Enemy> activeEnemies = new ArrayList<>();
    private int waveNumber;
    private boolean encounterTriggered;

    /**
     * Records that the player has done what starts the encounter (their first movement).
     *
     * <p>Once triggered, the encounter stays triggered even if the first wave cannot be placed
     * yet, so the first wave keeps being due until it actually starts. Calling this again has no
     * further effect.
     */
    public void triggerEncounter() {
        encounterTriggered = true;
    }

    /**
     * Returns whether the encounter has been triggered.
     *
     * @return {@code true} once {@link #triggerEncounter()} has been called or a wave has started
     */
    public boolean isEncounterTriggered() {
        return encounterTriggered || hasStarted();
    }

    /**
     * Returns whether the first wave has ever started.
     *
     * @return {@code true} once {@link #startNextWave(List)} has succeeded at least once
     */
    public boolean hasStarted() {
        return waveNumber > 0;
    }

    /**
     * Returns whether a wave is in progress.
     *
     * @return {@code true} if a wave has started and has at least one enemy left
     */
    public boolean isActive() {
        return hasStarted() && !activeEnemies.isEmpty();
    }

    /**
     * Returns whether the current wave has been cleared.
     *
     * @return {@code true} if a wave has started and none of its enemies remain
     */
    public boolean isCleared() {
        return hasStarted() && activeEnemies.isEmpty();
    }

    /**
     * Starts the next wave with the given enemies and increases the wave number by one.
     *
     * @param enemies the new wave's enemies; the list is copied, so later changes to it do not
     *                affect this wave
     * @throws IllegalStateException    if a wave is already active
     * @throws IllegalArgumentException if {@code enemies} is empty
     */
    public void startNextWave(List<Enemy> enemies) {
        if (isActive()) {
            throw new IllegalStateException("Cannot start a wave while wave " + waveNumber
                    + " still has enemies");
        }
        if (enemies.isEmpty()) {
            throw new IllegalArgumentException("A wave needs at least one enemy");
        }
        activeEnemies.addAll(enemies);
        waveNumber = waveNumber + 1;
    }

    /**
     * Removes a defeated enemy from the current wave (the seam for future combat).
     *
     * <p>Enemies are matched by identity, so pass the same object returned by
     * {@link #getActiveEnemies()}.
     *
     * @param enemy the enemy to remove
     * @return {@code true} if it was active and is now removed; {@code false} if it was not in
     *     the current wave
     */
    public boolean removeEnemy(Enemy enemy) {
        return activeEnemies.remove(enemy);
    }

    /**
     * Returns the current wave number.
     *
     * @return 0 before the first wave, then 1, 2, ... for each wave started
     */
    public int getWaveNumber() {
        return waveNumber;
    }

    /**
     * Returns the enemies still active in the current wave, for use-case code that updates
     * them.
     *
     * <p>The returned list is a read-only copy: adding or removing through it is impossible,
     * so callers must use {@link #removeEnemy(Enemy)}. The enemies themselves are the live,
     * mutable entities, so this list must never be handed to the view; outer layers receive
     * immutable copies of their values instead.
     *
     * @return an unmodifiable snapshot of the active enemies, in spawn order
     */
    public List<Enemy> getActiveEnemies() {
        return Collections.unmodifiableList(new ArrayList<>(activeEnemies));
    }
}
