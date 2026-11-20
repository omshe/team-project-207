package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class HitboxTest {
    private static final double DELTA = 1e-9;

    @Test
    void rejectsNonPositiveSize() {
        assertThrows(IllegalArgumentException.class, () -> new Hitbox(0, 0, 0, 10));
        assertThrows(IllegalArgumentException.class, () -> new Hitbox(0, 0, 10, -1));
    }

    @Test
    void exposesEdgesAndCentre() {
        Hitbox box = new Hitbox(10, 20, 30, 40);
        assertEquals(10, box.getX(), DELTA);
        assertEquals(20, box.getY(), DELTA);
        assertEquals(30, box.getWidth(), DELTA);
        assertEquals(40, box.getHeight(), DELTA);
        assertEquals(40, box.getRight(), DELTA);
        assertEquals(60, box.getBottom(), DELTA);
        assertEquals(25, box.getCenterX(), DELTA);
        assertEquals(40, box.getCenterY(), DELTA);
    }

    @Test
    void sharedEdgeCountsAsTouching() {
        Hitbox left = new Hitbox(0, 0, 10, 10);
        Hitbox right = new Hitbox(10, 0, 10, 10);
        Hitbox below = new Hitbox(0, 10, 10, 10);
        assertTrue(left.touches(right));
        assertTrue(right.touches(left));
        assertTrue(left.touches(below));
    }

    @Test
    void sharedCornerCountsAsTouching() {
        Hitbox box = new Hitbox(0, 0, 10, 10);
        Hitbox diagonal = new Hitbox(10, 10, 10, 10);
        assertTrue(box.touches(diagonal));
    }

    @Test
    void overlapAndCoincidentCountAsTouching() {
        Hitbox box = new Hitbox(0, 0, 10, 10);
        assertTrue(box.touches(new Hitbox(5, 5, 10, 10)));
        assertTrue(box.touches(new Hitbox(0, 0, 10, 10)));
    }

    @Test
    void anyGapMeansNotTouching() {
        Hitbox box = new Hitbox(0, 0, 10, 10);
        assertFalse(box.touches(new Hitbox(10.01, 0, 10, 10)));
        assertFalse(box.touches(new Hitbox(0, 10.01, 10, 10)));
        // Lined up on one axis but apart on the other.
        assertFalse(box.touches(new Hitbox(5, 30, 10, 10)));
    }

    @Test
    void roundingErrorWithinToleranceStillTouches() {
        Hitbox box = new Hitbox(0, 0, 10, 10);
        assertTrue(box.touches(new Hitbox(10 + Hitbox.CONTACT_TOLERANCE / 2, 0, 10, 10)));
    }

    @Test
    void gapsArePositiveApartZeroTouchingNegativeOverlapping() {
        Hitbox box = new Hitbox(0, 0, 10, 10);
        assertEquals(5, box.horizontalGapTo(new Hitbox(15, 0, 10, 10)), DELTA);
        assertEquals(5, box.horizontalGapTo(new Hitbox(-15, 0, 10, 10)), DELTA);
        assertEquals(0, box.horizontalGapTo(new Hitbox(10, 0, 10, 10)), DELTA);
        assertEquals(-4, box.horizontalGapTo(new Hitbox(6, 0, 10, 10)), DELTA);
        assertEquals(7, box.verticalGapTo(new Hitbox(0, 17, 10, 10)), DELTA);
        assertEquals(7, box.verticalGapTo(new Hitbox(0, -17, 10, 10)), DELTA);
    }

    @Test
    void insideWorldIncludesTheEdgesOnly() {
        assertTrue(new Hitbox(0, 0, 100, 50).isInsideWorld(100, 50));
        assertFalse(new Hitbox(-1, 0, 10, 10).isInsideWorld(100, 50));
        assertFalse(new Hitbox(0, -1, 10, 10).isInsideWorld(100, 50));
        assertFalse(new Hitbox(91, 0, 10, 10).isInsideWorld(100, 50));
        assertFalse(new Hitbox(0, 41, 10, 10).isInsideWorld(100, 50));
    }
}
