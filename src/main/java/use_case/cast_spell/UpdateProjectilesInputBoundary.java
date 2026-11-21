package use_case.cast_spell;

/**
 * Entry point for advancing projectile simulation by one game tick.
 */
public interface UpdateProjectilesInputBoundary {

    /**
     * Advances stored projectiles, removes expired ones, and presents one result.
     * Call once per tick on the game-loop thread, after enemy movement.
     */
    void execute();
}