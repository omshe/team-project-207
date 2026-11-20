package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** Regression tests: the player's existing movement and clamping must keep working. */
class PlayerTest {
    private static final double DELTA = 1e-9;

    @Test
    void movesAtFullSpeedStraight() {
        Player player = new Player(100, 100, 48, 4);
        player.move(1, 0);
        assertEquals(104, player.getX(), DELTA);
        player.move(0, -1);
        assertEquals(96, player.getY(), DELTA);
        assertEquals(48, player.getSize());
    }

    @Test
    void diagonalIsNormalized() {
        Player player = new Player(100, 100, 48, 4);
        player.move(1, 1);
        double movedX = player.getX() - 100;
        double movedY = player.getY() - 100;
        assertEquals(4, Math.sqrt(movedX * movedX + movedY * movedY), DELTA);
    }

    @Test
    void noInputMeansNoMovement() {
        Player player = new Player(100, 100, 48, 4);
        player.move(0, 0);
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
}
