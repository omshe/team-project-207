package use_case.cast_spell;

import java.util.List;

/**
 * An immutable snapshot of surviving projectiles and new impacts for one tick.
 */
public final class UpdateProjectilesOutputData {
    private final long tick;
    private final List<ProjectileOutputData> projectiles;
    private final List<ProjectileImpactOutputData> impacts;

    /**
     * Copies the collections for a completed simulation tick.
     *
     * @param tick the simulation tick number, beginning at one
     * @param projectiles snapshots of projectiles that remain active
     * @param impacts impacts generated during this tick
     * @throws NullPointerException if either list or one of its elements is null
     */
    public UpdateProjectilesOutputData(long tick,
                                       List<ProjectileOutputData> projectiles,
                                       List<ProjectileImpactOutputData> impacts) {
        this.tick = tick;
        this.projectiles = List.copyOf(projectiles);
        this.impacts = List.copyOf(impacts);
    }

    /** Returns the completed tick number. */
    public long getTick() {
        return tick;
    }

    /** Returns unmodifiable snapshots of the surviving projectiles. */
    public List<ProjectileOutputData> getProjectiles() {
        return projectiles;
    }

    /** Returns the unmodifiable impact events generated during this tick only. */
    public List<ProjectileImpactOutputData> getImpacts() {
        return impacts;
    }
}