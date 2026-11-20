package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnemyTest {
    private static final double DELTA = 1e-9;
    private static final double SPEED = 2;

    /** A 10 x 10 enemy whose top-left corner is at (x, y). */
    private static Enemy enemyAt(double x, double y) {
        return new Enemy(x, y, 10, 10, SPEED);
    }

    /** A 10 x 10 target whose top-left corner is at (x, y). */
    private static Hitbox targetAt(double x, double y) {
        return new Hitbox(x, y, 10, 10);
    }

    private static double distanceMoved(Enemy enemy, double startX, double startY) {
        double movedX = enemy.getX() - startX;
        double movedY = enemy.getY() - startY;
        return Math.sqrt(movedX * movedX + movedY * movedY);
    }

    @Test
    void rejectsInvalidSizeOrSpeed() {
        assertThrows(IllegalArgumentException.class, () -> new Enemy(0, 0, 0, 10, 1));
        assertThrows(IllegalArgumentException.class, () -> new Enemy(0, 0, 10, 0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Enemy(0, 0, 10, 10, -1));
    }

    @Test
    void startsStillFacingRightWithGivenValues() {
        Enemy enemy = new Enemy(1, 2, 30, 20, 3);
        assertEquals(1, enemy.getX(), DELTA);
        assertEquals(2, enemy.getY(), DELTA);
        assertEquals(30, enemy.getWidth(), DELTA);
        assertEquals(20, enemy.getHeight(), DELTA);
        assertEquals(3, enemy.getSpeed(), DELTA);
        assertFalse(enemy.isMoving());
        assertFalse(enemy.isFacingLeft());
        assertEquals(30, enemy.getHitbox().getWidth(), DELTA);
    }

    @Test
    void chasesRightLeftUpAndDownAtFullSpeed() {
        Enemy right = enemyAt(0, 100);
        right.pursue(targetAt(200, 100));
        assertEquals(2, right.getX(), DELTA);
        assertEquals(100, right.getY(), DELTA);

        Enemy left = enemyAt(200, 100);
        left.pursue(targetAt(0, 100));
        assertEquals(198, left.getX(), DELTA);

        Enemy up = enemyAt(100, 200);
        up.pursue(targetAt(100, 0));
        assertEquals(198, up.getY(), DELTA);

        Enemy down = enemyAt(100, 0);
        down.pursue(targetAt(100, 200));
        assertEquals(2, down.getY(), DELTA);
    }

    @Test
    void diagonalChaseIsNoFasterThanStraight() {
        Enemy diagonal = enemyAt(0, 0);
        diagonal.pursue(targetAt(300, 300));
        assertEquals(SPEED, distanceMoved(diagonal, 0, 0), DELTA);
        // Equal x and y components for a 45-degree chase.
        assertEquals(diagonal.getX(), diagonal.getY(), DELTA);

        Enemy straight = enemyAt(0, 0);
        straight.pursue(targetAt(300, 0));
        assertEquals(distanceMoved(straight, 0, 0), distanceMoved(diagonal, 0, 0), DELTA);
    }

    @Test
    void gapSmallerThanOneStepClosesExactlyWithoutOvershoot() {
        // Enemy's right edge at 10, target's left edge at 11: a 1-pixel gap, speed 2.
        Enemy enemy = enemyAt(0, 0);
        Hitbox target = targetAt(11, 0);
        enemy.pursue(target);

        assertEquals(1, enemy.getX(), DELTA);
        assertTrue(enemy.isMoving());
        assertTrue(enemy.getHitbox().touches(target));
        assertEquals(0, enemy.getHitbox().horizontalGapTo(target), 1e-9);
    }

    @Test
    void diagonalApproachStopsAtFirstContactOnBothAxes() {
        // Gaps: 3 px on x, 1 px on y. Contact needs both closed, so x decides.
        Enemy enemy = enemyAt(0, 0);
        Hitbox target = targetAt(13, 11);
        for (int tick = 0; tick < 10; tick++) {
            enemy.pursue(target);
            Hitbox current = enemy.getHitbox();
            double largestGap = Math.max(current.horizontalGapTo(target),
                    current.verticalGapTo(target));
            assertTrue(largestGap >= -Hitbox.CONTACT_TOLERANCE, "overshot on tick " + tick);
        }
        assertTrue(enemy.getHitbox().touches(target));
    }

    @Test
    void stopsWhileTouchingWithoutJitter() {
        Enemy enemy = enemyAt(0, 0);
        Hitbox target = targetAt(10, 0);   // already sharing an edge
        for (int tick = 0; tick < 5; tick++) {
            enemy.pursue(target);
            assertEquals(0, enemy.getX(), 0);
            assertEquals(0, enemy.getY(), 0);
            assertFalse(enemy.isMoving());
        }
    }

    @Test
    void cornerTouchStops() {
        Enemy enemy = enemyAt(0, 0);
        enemy.pursue(targetAt(10, 10));
        assertFalse(enemy.isMoving());
        assertEquals(0, enemy.getX(), 0);
    }

    @Test
    void coincidentPositionsDoNotMoveOrDivideByZero() {
        Enemy enemy = enemyAt(50, 50);
        enemy.pursue(targetAt(50, 50));
        assertEquals(50, enemy.getX(), 0);
        assertEquals(50, enemy.getY(), 0);
        assertFalse(enemy.isMoving());
    }

    @Test
    void overlappingTargetIsNotPushed() {
        Enemy enemy = enemyAt(50, 50);
        enemy.pursue(targetAt(55, 52));
        assertEquals(50, enemy.getX(), 0);
        assertFalse(enemy.isMoving());
    }

    @Test
    void resumesChaseAfterTargetMovesAway() {
        Enemy enemy = enemyAt(0, 0);
        enemy.pursue(targetAt(10, 0));
        assertFalse(enemy.isMoving());

        enemy.pursue(targetAt(100, 0));
        assertTrue(enemy.isMoving());
        assertEquals(2, enemy.getX(), DELTA);
    }

    @Test
    void retargetsToTheTargetsCurrentPosition() {
        Enemy enemy = enemyAt(100, 100);
        enemy.pursue(targetAt(300, 100));
        assertEquals(102, enemy.getX(), DELTA);

        enemy.pursue(targetAt(102, 300));
        assertEquals(102, enemy.getX(), DELTA);
        assertEquals(102, enemy.getY(), DELTA);
    }

    @Test
    void facingFollowsHorizontalMovementAndVerticalKeepsIt() {
        Enemy enemy = enemyAt(100, 100);
        enemy.pursue(targetAt(0, 100));
        assertTrue(enemy.isFacingLeft());

        enemy.pursue(targetAt(enemy.getX(), 300));   // straight down
        assertTrue(enemy.isFacingLeft());

        enemy.pursue(targetAt(400, enemy.getY()));
        assertFalse(enemy.isFacingLeft());
    }

    @Test
    void zeroSpeedEnemyNeverMoves() {
        Enemy enemy = new Enemy(0, 0, 10, 10, 0);
        enemy.pursue(targetAt(100, 0));
        assertEquals(0, enemy.getX(), 0);
        assertFalse(enemy.isMoving());
    }

    @Test
    void longChaseEndsTouchingAndNeverOverlaps() {
        Enemy enemy = new Enemy(0, 0, 56, 34, SPEED);
        Hitbox target = new Hitbox(700, 433, 48, 48);
        for (int tick = 0; tick < 1000; tick++) {
            enemy.pursue(target);
            Hitbox current = enemy.getHitbox();
            double largestGap = Math.max(current.horizontalGapTo(target),
                    current.verticalGapTo(target));
            assertTrue(largestGap >= -Hitbox.CONTACT_TOLERANCE);
        }
        assertTrue(enemy.getHitbox().touches(target));
        assertFalse(enemy.isMoving());
    }
}
