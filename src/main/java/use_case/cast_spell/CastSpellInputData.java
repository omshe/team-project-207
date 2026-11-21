package use_case.cast_spell;

/**
 * An immutable request to launch an identified spell toward a fixed world position.
 */
public final class CastSpellInputData {
    private final String spellId;
    private final double targetWorldX;
    private final double targetWorldY;

    /**
     * Creates a request whose values will be validated by the casting use case.
     *
     * @param spellId the identifier supplied by the input adapter
     * @param targetWorldX the fixed horizontal aim coordinate in world pixels
     * @param targetWorldY the fixed vertical aim coordinate in world pixels
     */
    public CastSpellInputData(String spellId, double targetWorldX, double targetWorldY) {
        this.spellId = spellId;
        this.targetWorldX = targetWorldX;
        this.targetWorldY = targetWorldY;
    }

    /** Returns the requested spell identifier. */
    public String getSpellId() {
        return spellId;
    }

    /** Returns the fixed horizontal aim coordinate in world pixels. */
    public double getTargetWorldX() {
        return targetWorldX;
    }

    /** Returns the fixed vertical aim coordinate in world pixels. */
    public double getTargetWorldY() {
        return targetWorldY;
    }
}