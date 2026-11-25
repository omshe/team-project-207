package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests how an enemy is defeated: it stops for good and counts ticks since its defeat. */
class EnemyDefeatTest {
    private static final Hitbox TARGET = new Hitbox(300, 100, 48, 48);

    @Test
    void newEnemyIsNotDefeated() {
        Enemy enemy = new Enemy(0, 100, 48, 48, 2);
        assertFalse(enemy.isDefeated());
        assertEquals(0, enemy.getTicksSinceDefeat());
        enemy.advanceDefeatTimer();
        assertEquals(0, enemy.getTicksSinceDefeat());
    }

    @Test
    void defeatedEnemyNeverMovesAgainAndCountsTicks() {
        Enemy enemy = new Enemy(0, 100, 48, 48, 2);
        enemy.pursue(TARGET);
        assertTrue(enemy.isMoving());

        enemy.defeat();
        assertTrue(enemy.isDefeated());
        assertFalse(enemy.isMoving());
        enemy.pursue(TARGET);
        assertEquals(2, enemy.getX(), 0);
        assertFalse(enemy.isMoving());

        enemy.advanceDefeatTimer();
        enemy.advanceDefeatTimer();
        assertEquals(2, enemy.getTicksSinceDefeat());
        enemy.defeat();
        assertEquals(2, enemy.getTicksSinceDefeat(), "defeating again must not restart it");
    }
}
