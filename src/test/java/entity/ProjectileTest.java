package entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests projectile construction, movement, hitbox geometry, and validation.
 */
class ProjectileTest {

    private static final double TOLERANCE = 1e-9;

    /**
     * Verifies that construction preserves the supplied spell and position.
     */
    @Test
    void constructorPreservesSpellAndPosition() {
        Spell spell = new FireballSpell();
        Projectile projectile = new Projectile(
                spell, 100, 200, 3, -4, 10, 8);

        assertSame(spell, projectile.getSpell());
        assertEquals(100, projectile.getX(), TOLERANCE);
        assertEquals(200, projectile.getY(), TOLERANCE);
    }

    /**
     * Verifies that movement updates both position and hitbox geometry.
     */
    @Test
    void moveUpdatesPositionAndHitbox() {
        Projectile projectile = new Projectile(
                new FireballSpell(), 100, 200, 3, -4, 10, 8);

        projectile.move();

        assertEquals(103, projectile.getX(), TOLERANCE);
        assertEquals(196, projectile.getY(), TOLERANCE);

        Hitbox hitbox = projectile.getHitbox();
        assertEquals(103, hitbox.getX(), TOLERANCE);
        assertEquals(196, hitbox.getY(), TOLERANCE);
        assertEquals(10, hitbox.getWidth(), TOLERANCE);
        assertEquals(8, hitbox.getHeight(), TOLERANCE);
    }

    /**
     * Verifies that repeated ticks preserve the original movement direction.
     */
    @Test
    void repeatedMovementKeepsFixedVelocity() {
        Projectile projectile = new Projectile(
                new FireballSpell(), 100, 200, 3, -4, 10, 8);

        projectile.move();
        projectile.move();

        assertEquals(106, projectile.getX(), TOLERANCE);
        assertEquals(192, projectile.getY(), TOLERANCE);
    }

    /**
     * Verifies that a previously obtained hitbox remains an immutable snapshot.
     */
    @Test
    void movingDoesNotChangePreviousHitbox() {
        Projectile projectile = new Projectile(
                new FireballSpell(), 100, 200, 3, -4, 10, 8);
        Hitbox previousHitbox = projectile.getHitbox();

        projectile.move();

        assertEquals(100, previousHitbox.getX(), TOLERANCE);
        assertEquals(200, previousHitbox.getY(), TOLERANCE);
    }

    /**
     * Verifies that a projectile requires an associated spell.
     */
    @Test
    void constructorRejectsNullSpell() {
        assertThrows(NullPointerException.class,
                () -> new Projectile(null, 100, 200, 3, -4, 10, 8));
    }

    /**
     * Verifies that both hitbox dimensions must be positive and finite.
     */
    @Test
    void constructorRejectsInvalidDimensions() {
        Spell spell = new FireballSpell();

        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(spell, 100, 200, 3, -4, 0, 8));
        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(spell, 100, 200, 3, -4, 10, -1));
        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(spell, 100, 200, 3, -4, Double.NaN, 8));
        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(
                        spell, 100, 200, 3, -4, 10, Double.POSITIVE_INFINITY));
    }

    /**
     * Verifies that position and velocity values must be finite.
     */
    @Test
    void constructorRejectsNonFinitePositionAndVelocity() {
        Spell spell = new FireballSpell();

        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(spell, Double.NaN, 200, 3, -4, 10, 8));
        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(
                        spell, 100, Double.POSITIVE_INFINITY, 3, -4, 10, 8));
        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(
                        spell, 100, 200, Double.POSITIVE_INFINITY, -4, 10, 8));
        assertThrows(IllegalArgumentException.class,
                () -> new Projectile(spell, 100, 200, 3, Double.NaN, 10, 8));
    }
}