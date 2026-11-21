package use_case.generate_mob_waves;

/**
 * Receives the result of each Generate Mob Waves tick. Implemented by the presenter.
 *
 * <p>For every call to {@link GenerateMobWavesInputBoundary#execute}, exactly one of these
 * methods is called once.
 */
public interface GenerateMobWavesOutputBoundary {
    /**
     * Presents the encounter after a normal tick: no wave was due, or a due wave started.
     *
     * @param outputData the wave number and every active enemy's position and movement
     */
    void prepareSuccessView(GenerateMobWavesOutputData outputData);

    /**
     * Presents a tick on which a wave was due but could not start because the whole wave did
     * not fit in the world (use-case extension 2a). No enemies were placed; the interactor will
     * try again on the next tick.
     *
     * @param outputData   the unchanged encounter state (no active enemies)
     * @param errorMessage a short explanation suitable for showing the player
     */
    void prepareFailView(GenerateMobWavesOutputData outputData, String errorMessage);
}
