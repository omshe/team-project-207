package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Checks how far a hitbox may move across terrain: it stops flush against water and the map
 * edge, may share their edges, and may leave blocked tiles it already overlaps.
 */
class TileMapMoveLimitTest {
    private static final double DELTA = 1e-9;

    /** A 128 x 96 map with one water tile spanning x 64..96 and y 32..64. */
    private final TileMap map = TestTerrain.fromRows(
            "1111",
            "1101",
            "1111");

    @Test
    void clearPathAllowsTheWholeMove() {
        Hitbox box = new Hitbox(0, 0, 16, 16);

        assertEquals(4, map.limitHorizontalMove(box, 4), DELTA);
        assertEquals(4, map.limitVerticalMove(box, 4), DELTA);
    }

    @Test
    void zeroMoveStaysZero() {
        Hitbox box = new Hitbox(0, 0, 16, 16);

        assertEquals(0, map.limitHorizontalMove(box, 0), 0);
        assertEquals(0, map.limitVerticalMove(box, 0), 0);
    }

    @Test
    void movingRightStopsFlushAgainstWater() {
        Hitbox box = new Hitbox(40, 32, 16, 16);

        assertEquals(8, map.limitHorizontalMove(box, 12), DELTA);
    }

    @Test
    void movingLeftStopsFlushAgainstWater() {
        Hitbox box = new Hitbox(100, 32, 16, 16);

        assertEquals(-4, map.limitHorizontalMove(box, -8), DELTA);
    }

    @Test
    void movingDownStopsFlushAgainstWater() {
        Hitbox box = new Hitbox(64, 0, 16, 16);

        assertEquals(16, map.limitVerticalMove(box, 20), DELTA);
    }

    @Test
    void movingUpStopsFlushAgainstWater() {
        Hitbox box = new Hitbox(64, 70, 16, 16);

        assertEquals(-6, map.limitVerticalMove(box, -10), DELTA);
    }

    @Test
    void alreadyFlushAgainstWaterCannotMoveIntoIt() {
        assertEquals(0, map.limitHorizontalMove(new Hitbox(48, 32, 16, 16), 4), 0);
        assertEquals(0, map.limitHorizontalMove(new Hitbox(96, 32, 16, 16), -4), 0);
        assertEquals(0, map.limitVerticalMove(new Hitbox(64, 16, 16, 16), 4), 0);
        assertEquals(0, map.limitVerticalMove(new Hitbox(64, 64, 16, 16), -4), 0);
    }

    @Test
    void sharingAnEdgeWithWaterDoesNotBlockMovingAlongIt() {
        // The box's bottom edge lies exactly on the water's top edge.
        Hitbox box = new Hitbox(40, 16, 16, 16);

        assertEquals(40, map.limitHorizontalMove(box, 40), DELTA);
    }

    @Test
    void boxSpanningTwoRowsIsBlockedByWaterInEitherRow() {
        Hitbox box = new Hitbox(40, 20, 16, 16);

        assertEquals(8, map.limitHorizontalMove(box, 40), DELTA);
    }

    @Test
    void longMoveStopsAtTheFirstBlockedTile() {
        Hitbox box = new Hitbox(0, 32, 16, 16);

        assertEquals(48, map.limitHorizontalMove(box, 200), DELTA);
    }

    @Test
    void mapEdgesBlockLikeWater() {
        assertEquals(0, map.limitHorizontalMove(new Hitbox(100, 0, 28, 16), 4), 0);
        assertEquals(-2, map.limitHorizontalMove(new Hitbox(2, 0, 16, 16), -4), DELTA);
        assertEquals(-1, map.limitVerticalMove(new Hitbox(0, 1, 16, 16), -4), DELTA);
        assertEquals(2, map.limitVerticalMove(new Hitbox(0, 78, 16, 16), 4), DELTA);
    }

    @Test
    void moverAlreadyOverlappingWaterCanWalkOffIt() {
        Hitbox box = new Hitbox(70, 40, 16, 16);

        assertEquals(10, map.limitHorizontalMove(box, 10), DELTA);
        assertEquals(-10, map.limitHorizontalMove(box, -10), DELTA);
        assertEquals(-10, map.limitVerticalMove(box, -10), DELTA);
    }

    @Test
    void roundingNearAnEdgeNeverMovesBackwards() {
        // Already a hair past flush (within the contact tolerance): stay put, don't back off.
        Hitbox overlapping = new Hitbox(48 + 5e-7, 32, 16, 16);
        // A hair short of flush: may close the tiny gap but no further.
        Hitbox nearlyFlush = new Hitbox(48 - 1e-9, 32, 16, 16);

        assertEquals(0.0, map.limitHorizontalMove(overlapping, 4), 0);
        assertEquals(1e-9, map.limitHorizontalMove(nearlyFlush, 4), 1e-12);
    }
}
