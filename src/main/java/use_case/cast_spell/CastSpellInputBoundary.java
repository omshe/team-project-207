package use_case.cast_spell;

/**
 * Entry point for launching a spell, called on the game-loop thread.
 */
public interface CastSpellInputBoundary {

    /**
     * Processes one request. Success stores one projectile and presents success.
     * Invalid request values store nothing and present failure. Each request
     * produces exactly one output-boundary call.
     *
     * @param inputData the non-null spell and fixed world aim point
     * @throws NullPointerException if inputData is null
     */
    void execute(CastSpellInputData inputData);
}
