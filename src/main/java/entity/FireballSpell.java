package entity;

/**
 * Defines the identity of the fireball spell.
 */
public final class FireballSpell implements Spell {

    /**
     * Creates a fireball spell.
     */
    public FireballSpell() {
    }

    /** {@inheritDoc} */
    @Override
    public String getId() {
        return "fireball";
    }

    /** {@inheritDoc} */
    @Override
    public String getName() {
        return "Fireball";
    }
}
