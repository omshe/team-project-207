package entity;

/**
 * Defines a spell's identifier and display name.
 */
public interface Spell {

    /**
     * Returns the identifier used to look up this spell.
     *
     * @return the stable spell identifier
     */
    String getId();

    /**
     * Returns the name shown to the player.
     *
     * @return the spell's display name
     */
    String getName();
}
