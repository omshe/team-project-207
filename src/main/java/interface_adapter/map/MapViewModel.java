package interface_adapter.map;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Stores the latest immutable map state and notifies listeners when the presenter publishes it. */
public final class MapViewModel {
    private volatile MapState state = new MapState();
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public MapState getState() {
        return state;
    }

    public void setState(MapState state) {
        this.state = state;
    }

    /** Publishes the current state, including repeated errors, on the calling thread. */
    public void firePropertyChanged() {
        support.firePropertyChange("state", null, state);
    }

    /**
     * Registers a listener for state notifications on the publishing thread.
     *
     * @param listener observer to notify when the presenter publishes state
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a previously registered state listener.
     *
     * @param listener observer whose registration should be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
}
