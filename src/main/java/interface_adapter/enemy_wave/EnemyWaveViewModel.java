package interface_adapter.enemy_wave;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Holds the latest {@link EnemyWaveState} for the view and tells listeners when it changes
 * (observer pattern, via {@link PropertyChangeSupport}).
 *
 * <p>Threading: the presenter calls {@link #setState} on the game-loop thread, and the view
 * reads on Swing's thread, so the reference is {@code volatile}. Because each state is
 * immutable, a reader always sees a complete snapshot. Listeners are notified on the thread
 * that set the state (the game-loop thread), so a listener must only do thread-safe work, such
 * as requesting a repaint. This view model is separate from the player's so the two presenters
 * can never overwrite each other's fields.
 */
public class EnemyWaveViewModel {
    /** Property name used in change events. */
    public static final String STATE_PROPERTY = "state";

    private final PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
    private volatile EnemyWaveState state;

    /**
     * Creates the view model with an empty state.
     */
    public EnemyWaveViewModel() {
        this.state = EnemyWaveState.empty();
    }

    /**
     * Returns the latest snapshot.
     *
     * @return the current state; never {@code null}
     */
    public EnemyWaveState getState() {
        return state;
    }

    /**
     * Publishes a new snapshot and notifies every listener with the old and new states.
     *
     * @param state the new state
     */
    public void setState(EnemyWaveState state) {
        EnemyWaveState oldState = this.state;
        this.state = state;
        changeSupport.firePropertyChange(STATE_PROPERTY, oldState, state);
    }

    /**
     * Registers a listener to be told about each new snapshot.
     *
     * @param listener called after every {@link #setState} with a different snapshot, on the
     *                 thread that set it
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
}
