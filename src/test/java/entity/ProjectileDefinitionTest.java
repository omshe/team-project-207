package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** Checks projectile definitions and preservation of fixed flight state. */
class ProjectileDefinitionTest {
    @Test
    void typeAndFixedVelocitySurviveMovement() {
        Spell spell = new FireballSpell();
        ProjectileDefinition definition = new ProjectileDefinition(spell, "test_type", 5, 8, 6);
        Projectile projectile = new Projectile(definition, 10, 20, 3, 4);
        for (int tick = 0; tick < 10; tick++) {
            projectile.move();
        }
        assertSame(spell, projectile.getSpell());
        assertEquals("test_type", projectile.getProjectileTypeId());
        assertEquals(40, projectile.getX());
        assertEquals(60, projectile.getY());
        assertEquals(3, projectile.getVelocityX());
        assertEquals(4, projectile.getVelocityY());
        assertEquals(8, projectile.getHitbox().getWidth());
        assertEquals(6, projectile.getHitbox().getHeight());
    }

    @Test
    void rejectsInvalidDefinitions() {
        Spell spell = new FireballSpell();
        for (double invalid : new double[] {0, -1, Double.NaN, Double.POSITIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class,
                    () -> new ProjectileDefinition(spell, "type", invalid, 8, 8));
            assertThrows(IllegalArgumentException.class,
                    () -> new ProjectileDefinition(spell, "type", 8, invalid, 8));
            assertThrows(IllegalArgumentException.class,
                    () -> new ProjectileDefinition(spell, "type", 8, 8, invalid));
        }
        assertThrows(IllegalArgumentException.class,
                () -> new ProjectileDefinition(spell, " ", 8, 8, 8));
        assertThrows(NullPointerException.class,
                () -> new ProjectileDefinition(null, "type", 8, 8, 8));
    }
}
