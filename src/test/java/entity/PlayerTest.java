package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * The player's own rules: movement speed, terrain blocking, clamping, facing and whether it is
 * moving.
 */
class PlayerTest {
    private static final double DELTA = 1e-9;
    private static final TileMap OPEN = TestTerrain.openGround(30, 20);

    /** Water fills column 2 (x 64..96) of a 128 x 96 map. */
    private static final TileMap WATER_COLUMN = TestTerrain.fromRows(
            "1101",
            "1101",
            "1101");

    @Test
    void movesAtFullSpeedStraight() {
        Player player = new Player(100, 100, 48, 4);
        player.move(1, 0, OPEN);
        assertEquals(104, player.getX(), DELTA);
        player.move(0, -1, OPEN);
        assertEquals(96, player.getY(), DELTA);
        assertEquals(48, player.getSize());
    }

    @Test
    void diagonalIsNormalized() {
        Player player = new Player(100, 100, 48, 4);
        player.move(1, 1, OPEN);
        double movedX = player.getX() - 100;
        double movedY = player.getY() - 100;
        assertEquals(4, Math.sqrt(movedX * movedX + movedY * movedY), DELTA);
    }

    @Test
    void noInputMeansNoMovement() {
        Player player = new Player(100, 100, 48, 4);
        player.move(0, 0, OPEN);
        assertEquals(100, player.getX(), 0);
        assertEquals(100, player.getY(), 0);
    }

    @Test
    void clampsToWorldEdges() {
        Player player = new Player(-10, 700, 48, 4);
        player.clampTo(960, 640);
        assertEquals(0, player.getX(), 0);
        assertEquals(592, player.getY(), 0);
    }

    @Test
    void clampsToTheRightAndTopEdges() {
        Player player = new Player(1000, -5, 48, 4);
        player.clampTo(960, 640);
        assertEquals(912, player.getX(), 0);
        assertEquals(0, player.getY(), 0);
    }

    @Test
    void startsFacingRightAndNotMoving() {
        Player player = new Player(100, 100, 48, 4);
        assertFalse(player.isFacingLeft());
        assertFalse(player.isMoving());
    }

    @Test
    void sidewaysMovementSetsFacingAndVerticalMovementKeepsIt() {
        Player player = new Player(100, 100, 48, 4);

        player.move(-1, 0, OPEN);
        assertTrue(player.isFacingLeft());

        player.move(0, 1, OPEN);
        assertTrue(player.isFacingLeft());

        player.move(1, -1, OPEN);
        assertFalse(player.isFacingLeft());
    }

    @Test
    void isMovingFollowsTheMostRecentTick() {
        Player player = new Player(100, 100, 48, 4);

        player.move(0, -1, OPEN);
        assertTrue(player.isMoving());

        player.move(0, 0, OPEN);
        assertFalse(player.isMoving());
    }

    @Test
    void standingStillKeepsTheFacing() {
        Player player = new Player(100, 100, 48, 4);
        player.move(-1, 0, OPEN);
        player.move(0, 0, OPEN);
        assertTrue(player.isFacingLeft());
    }

    @Test
    void stopsFlushAgainstWater() {
        Player player = new Player(42, 32, 16, 4);

        player.move(1, 0, WATER_COLUMN);
        assertEquals(46, player.getX(), DELTA);
        player.move(1, 0, WATER_COLUMN);
        assertEquals(48, player.getX(), DELTA);
        player.move(1, 0, WATER_COLUMN);
        assertEquals(48, player.getX(), DELTA);
    }

    @Test
    void slidesAlongWaterWhenMovingDiagonallyIntoIt() {
        Player player = new Player(48, 32, 16, 4);

        player.move(1, 1, WATER_COLUMN);

        assertEquals(48, player.getX(), DELTA);
        assertEquals(32 + 4 / Math.sqrt(2), player.getY(), DELTA);
    }

    @Test
    void stillTurnsAndCountsAsMovingWhenBlocked() {
        Player player = new Player(96, 32, 16, 4);

        player.move(-1, 0, WATER_COLUMN);

        assertEquals(96, player.getX(), DELTA);
        assertTrue(player.isFacingLeft());
        assertTrue(player.isMoving());
    }

    @Test
    void stopsAtTheMapEdge() {
        Player player = new Player(2, 2, 16, 4);

        player.move(-1, 0, OPEN);
        player.move(0, -1, OPEN);

        assertEquals(0, player.getX(), DELTA);
        assertEquals(0, player.getY(), DELTA);
    }

    @Test
    void hitboxIsThePlayerBox() {
        Player player = new Player(10, 20, 48, 4);

        Hitbox box = player.getHitbox();

        assertEquals(10, box.getX(), 0);
        assertEquals(20, box.getY(), 0);
        assertEquals(48, box.getWidth(), 0);
        assertEquals(48, box.getHeight(), 0);
    }
}
