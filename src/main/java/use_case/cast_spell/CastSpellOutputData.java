package use_case.cast_spell;

/**
 * An immutable description of the spell that was successfully launched.
 */
public final class CastSpellOutputData {
    private final String spellId;
    private final String spellName;

    /**
     * Creates a successful launch result.
     *
     * @param spellId the launched spell's identifier
     * @param spellName the launched spell's display name
     */
    public CastSpellOutputData(String spellId, String spellName) {
        this.spellId = spellId;
        this.spellName = spellName;
    }

    /** @return the launched spell's identifier */
    public String getSpellId() {
        return spellId;
    }

    /** @return the launched spell's display name */
    public String getSpellName() {
        return spellName;
    }
}
