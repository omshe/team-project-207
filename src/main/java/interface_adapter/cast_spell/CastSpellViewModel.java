package interface_adapter.cast_spell;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;

/**
 * Stores flight snapshots, casting feedback, and unconsumed impact events.
 * The game-loop thread publishes; one rendering consumer drains impacts.
 */
public final class CastSpellViewModel {
    /** Property name used for snapshot notifications. */
    public static final String STATE_PROPERTY = "state";

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private final List<ProjectileImpactDisplayData> pendingImpacts = new ArrayList<>();
    private CastSpellState state = CastSpellState.empty();

    /** Returns the latest immutable snapshot without consuming any impacts. */
    public synchronized CastSpellState getState() {
        return state;
    }

    /**
     * Replaces feedback while preserving flight state and queued impacts.
     * Call only on the game-loop thread.
     *
     * @param message the new player-facing message
     * @throws NullPointerException if message is null
     */
    public void setStatusMessage(String message) {
        CastSpellState before;
        CastSpellState after;
        synchronized (this) {
            before = state;
            after = new CastSpellState(
                    before.getAnimationTick(), message, before.getProjectiles());
            state = after;
        }
        changeSupport.firePropertyChange(STATE_PROPERTY, before, after);
    }

    /**
     * Publishes flight state and appends this tick's new impacts, preserving feedback.
     * Call only on the game-loop thread, once for each simulation output.
     *
     * @param tick the completed projectile simulation tick
     * @param projectiles current projectile display values
     * @param impacts new impacts from this tick only
     * @throws NullPointerException if either list or one of its elements is null
     */
    public void updateProjectiles(long tick, List<ProjectileDisplayData> projectiles,
                                  List<ProjectileImpactDisplayData> impacts) {
        List<ProjectileImpactDisplayData> newImpacts = List.copyOf(impacts);
        CastSpellState before;
        CastSpellState after;
        synchronized (this) {
            before = state;
            after = new CastSpellState(tick, before.getStatusMessage(), projectiles);
            pendingImpacts.addAll(newImpacts);
            state = after;
        }
        changeSupport.firePropertyChange(STATE_PROPERTY, before, after);
    }

    /**
     * Atomically takes all queued impacts in presentation order and clears the queue.
     * Call from one rendering consumer, which retains each event for its animation.
     * Later calls return only impacts published since the previous drain.
     * Draining before reading flight state ensures that state is at least as recent
     * as the consumed impacts. Draining does not fire a change notification.
     *
     * @return an unmodifiable list of newly consumed impact events
     */
    public synchronized List<ProjectileImpactDisplayData> drainImpacts() {
        List<ProjectileImpactDisplayData> result = List.copyOf(pendingImpacts);
        pendingImpacts.clear();
        return result;
    }

    /**
     * Registers a listener notified on the publishing game-loop thread.
     * A Swing listener must perform thread-safe work, such as requesting repaint.
     *
     * @param listener the observer to register
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Unregisters a listener when its view is no longer in use.
     *
     * @param listener the observer to remove
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(listener);
    }
}