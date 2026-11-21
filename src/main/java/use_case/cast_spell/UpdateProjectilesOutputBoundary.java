package use_case.cast_spell;

/**
 * Receives projectile snapshots and newly generated impacts after each tick.
 */
public interface UpdateProjectilesOutputBoundary {

    /**
     * Presents one completed tick. Impact events belong to this tick only.
     * The presenter must deliver each event once for subsequent visual playback.
     *
     * @param outputData immutable surviving projectiles and new impact events
     */
    void present(UpdateProjectilesOutputData outputData);
}