package use_case.cast_spell;

/**
 * Receives one success or failure result for each casting request.
 */
public interface CastSpellOutputBoundary {

    /**
     * Presents success after one projectile has been stored.
     *
     * @param outputData the launched spell's identity
     */
    void prepareSuccessView(CastSpellOutputData outputData);

    /**
     * Presents a rejected request. No projectile was stored for that request.
     *
     * @param errorMessage the reason casting failed
     */
    void prepareFailView(String errorMessage);
}