package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Tests the strict overlap check and the separation distance between hitboxes. */
class HitboxSpacingTest {
    private final Hitbox box = new Hitbox(0, 0, 10, 10);

    @Test
    void sharedEdgesAndCornersTouchButDoNotOverlap() {
        Hitbox edge = new Hitbox(10, 0, 10, 10);
        Hitbox corner = new Hitbox(10, 10, 10, 10);
        assertTrue(box.touches(edge));
        assertFalse(box.overlaps(edge));
        assertFalse(box.overlaps(corner));
    }

    @Test
    void sharedInteriorOverlaps() {
        assertTrue(box.overlaps(new Hitbox(9, 9, 10, 10)));
        assertTrue(box.overlaps(new Hitbox(2, 2, 2, 2)));
    }

    @Test
    void separationIsTheLargerAxisGap() {
        assertEquals(5, box.separationFrom(new Hitbox(15, 3, 10, 10)), 0);
        assertEquals(7, box.separationFrom(new Hitbox(12, 17, 10, 10)), 0);
        assertEquals(0, box.separationFrom(new Hitbox(10, 0, 10, 10)), 0);
        assertTrue(box.separationFrom(new Hitbox(5, 5, 10, 10)) < 0);
    }
}
