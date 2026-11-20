package use_case.generate_mob_waves;

/**
 * Entry point of the Generate Mob Waves use case. Implemented by the interactor and called by
 * the enemy-wave controller once per simulation tick, <b>after</b> the player has moved.
 */
public interface GenerateMobWavesInputBoundary {
    /**
     * Advances the mob-wave encounter by one tick.
     *
     * <p>Depending on the encounter's state this may start a wave, move every active enemy
     * toward the player, or start the next wave once the current one has been cleared. The
     * result is always delivered through the output boundary, exactly once per call: either a
     * success view or a fail view. Nothing is returned to the caller.
     *
     * @param inputData the player's movement request for this tick
     */
    void execute(GenerateMobWavesInputData inputData);
}
