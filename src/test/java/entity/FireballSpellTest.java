package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Tests the fireball spell's identifier and display name.
 */
class FireballSpellTest {

    /**
     * Verifies that the spell exposes the expected lookup identifier.
     */
    @Test
    void getIdReturnsFireballIdentifier() {
        Spell spell = new FireballSpell();

        assertEquals("fireball", spell.getId());
    }

    /**
     * Verifies that the spell exposes the expected display name.
     */
    @Test
    void getNameReturnsFireballDisplayName() {
        Spell spell = new FireballSpell();

        assertEquals("Fireball", spell.getName());
    }
}