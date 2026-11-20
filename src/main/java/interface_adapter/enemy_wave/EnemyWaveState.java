package interface_adapter.enemy_wave;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * An immutable snapshot of everything the view draws for the mob waves. A new one is made
 * every tick, so a snapshot the view is still drawing never changes underneath it.
 */
public class EnemyWaveState {
    private final String waveLabel;
    private final String statusMessage;
    private final List<EnemyDisplayData> enemies;
    private final long animationTick;

    /**
     * Creates a snapshot.
     *
     * @param waveLabel     heading such as "Wave 1", or an empty string before the first wave
     * @param statusMessage a message for the player, or an empty string when there is none
     * @param enemies       the enemies to draw; the list is copied defensively
     * @param animationTick number of simulation ticks presented so far, used by the view to
     *                      pick animation frames
     */
    public EnemyWaveState(String waveLabel, String statusMessage,
                          List<EnemyDisplayData> enemies, long animationTick) {
        this.waveLabel = waveLabel;
        this.statusMessage = statusMessage;
        this.enemies = Collections.unmodifiableList(new ArrayList<>(enemies));
        this.animationTick = animationTick;
    }

    /**
     * Returns the state shown before any tick has run: no label, no message, no enemies.
     *
     * @return an empty snapshot
     */
    public static EnemyWaveState empty() {
        return new EnemyWaveState("", "", new ArrayList<>(), 0);
    }

    /**
     * Returns the wave heading.
     *
     * @return for example "Wave 1", or an empty string before the first wave
     */
    public String getWaveLabel() {
        return waveLabel;
    }

    /**
     * Returns the message for the player.
     *
     * @return the message, or an empty string when there is none
     */
    public String getStatusMessage() {
        return statusMessage;
    }

    /**
     * Returns the enemies to draw.
     *
     * @return an unmodifiable list of immutable enemy display values
     */
    public List<EnemyDisplayData> getEnemies() {
        return enemies;
    }

    /**
     * Returns the number of simulation ticks presented so far.
     *
     * @return the tick count used for animation timing
     */
    public long getAnimationTick() {
        return animationTick;
    }
}
