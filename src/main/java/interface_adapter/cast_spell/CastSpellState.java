package interface_adapter.cast_spell;

import java.util.List;
import java.util.Objects;

/** Immutable snapshot of current flight visuals and casting feedback. */
public final class CastSpellState {
    private final long animationTick;
    private final String statusMessage;
    private final List<ProjectileDisplayData> projectiles;

    /**
     * Creates a snapshot with a defensive copy of its projectile list.
     * Impact events are stored separately by the ViewModel until consumed.
     *
     * @param animationTick the completed projectile simulation tick
     * @param statusMessage feedback for the player, or an empty string
     * @param projectiles the current flying projectiles
     * @throws NullPointerException if the message, list, or a list element is null
     */
    public CastSpellState(long animationTick, String statusMessage,
                          List<ProjectileDisplayData> projectiles) {
        this.animationTick = animationTick;
        this.statusMessage = Objects.requireNonNull(statusMessage);
        this.projectiles = List.copyOf(projectiles);
    }

    /** @return the snapshot shown before casting or simulation has run */
    public static CastSpellState empty() {
        return new CastSpellState(0, "", List.of());
    }

    /** @return the completed simulation tick used for flight animation */
    public long getAnimationTick() {
        return animationTick;
    }

    /** @return feedback for the player, or an empty string */
    public String getStatusMessage() {
        return statusMessage;
    }

    /** @return an unmodifiable list of immutable projectile display values */
    public List<ProjectileDisplayData> getProjectiles() {
        return projectiles;
    }
}