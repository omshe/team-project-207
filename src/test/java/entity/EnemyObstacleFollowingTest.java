package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests how an enemy walks around things its movement space forbids. */
class EnemyObstacleFollowingTest {
    private static final Hitbox TARGET = new Hitbox(300, 100, 48, 48);

    @Test
    void anOpenSpaceGivesTheSameStepAsPlainPursuit() {
        Enemy plain = new Enemy(0, 0, 48, 48, 2);
        Enemy spaced = new Enemy(0, 0, 48, 48, 2);
        plain.pursue(TARGET);
        spaced.pursue(TARGET, (current, next) -> true);
        assertEquals(plain.getX(), spaced.getX(), 0);
        assertEquals(plain.getY(), spaced.getY(), 0);
    }

    @Test
    void waitsWhenEveryStepIsForbidden() {
        Enemy enemy = new Enemy(0, 100, 48, 48, 2);
        enemy.pursue(TARGET, (current, next) -> false);
        assertEquals(0, enemy.getX(), 0);
        assertEquals(100, enemy.getY(), 0);
        assertFalse(enemy.isMoving());
    }

    @Test
    void sidestepsAlongWallAndKeepsToOneSideUntilPastIt() {
        // A wall straight ahead, from y = 40 to y = 210.
        Hitbox wall = new Hitbox(100, 40, 40, 170);
        Enemy enemy = new Enemy(52, 100, 48, 48, 2);
        MovementSpace space = (current, next) -> !next.overlaps(wall);

        double previousY = enemy.getY();
        int direction = 0;
        for (int tick = 0; tick < 400 && !enemy.getHitbox().touches(TARGET); tick++) {
            enemy.pursue(TARGET, space);
            assertFalse(enemy.getHitbox().overlaps(wall));
            assertFalse(enemy.getHitbox().overlaps(TARGET));
            // While still beside the wall, it moves vertically in one direction only.
            if (enemy.getHitbox().getRight() <= wall.getX() + 1e-6) {
                int step = (int) Math.signum(enemy.getY() - previousY);
                if (direction == 0) {
                    direction = step;
                }
                assertTrue(step == 0 || step == direction, "changed sides at tick " + tick);
            }
            previousY = enemy.getY();
        }
        assertTrue(enemy.getHitbox().touches(TARGET));
    }

    @Test
    void switchesSideWhenThePreferredSideIsBlocked() {
        // Wall ahead plus a ceiling just above: the enemy must go down.
        Hitbox wall = new Hitbox(100, 0, 40, 200);
        Hitbox ceiling = new Hitbox(0, 0, 100, 100);
        Enemy enemy = new Enemy(52, 100, 48, 48, 2);
        enemy.pursue(TARGET, (current, next) -> !next.overlaps(wall) && !next.overlaps(ceiling));
        assertEquals(102, enemy.getY(), 1e-9);
        assertEquals(52, enemy.getX(), 1e-9);
    }

    @Test
    void slidesAlongOneAxisWhenOnlyThatIsFree() {
        // Moving mainly up toward a target above-right; the straight step and both
        // horizontal sidesteps are forbidden, but moving up alone is allowed.
        Hitbox target = new Hitbox(120, 0, 48, 48);
        Enemy enemy = new Enemy(100, 300, 48, 48, 2);
        enemy.pursue(target, (current, next) -> next.getX() == current.getX());
        assertEquals(100, enemy.getX(), 0);
        assertTrue(enemy.getY() < 300);
        assertTrue(enemy.isMoving());
    }

    @Test
    void sidestepNeverOverlapsTheTarget() {
        // Diagonal approach where the straight step is forbidden; a sidestep would cut into
        // the target's corner, so it must be refused.
        Hitbox target = new Hitbox(100, 100, 48, 48);
        Enemy enemy = new Enemy(51, 147, 48, 48, 2);
        enemy.pursue(target, (current, next) -> next.getX() <= current.getX());
        assertFalse(enemy.getHitbox().overlaps(target));
    }
}
