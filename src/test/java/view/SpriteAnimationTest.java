package view;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Checks that an animation holds each frame for its tick count and loops (uses the wizard walk
 * sheet: 8 frames).
 */
class SpriteAnimationTest {
    private static final String WALK = "/sprites/wizard/Wizard-Walk.png";
    private static final int TICKS_PER_FRAME = 6;

    @Test
    void eachFrameStaysForItsTickCount() {
        SpriteAnimation walk = new SpriteAnimation(WALK, 100, TICKS_PER_FRAME);

        assertSame(walk.frameAt(0), walk.frameAt(TICKS_PER_FRAME - 1));
        assertNotSame(walk.frameAt(0), walk.frameAt(TICKS_PER_FRAME));
    }

    @Test
    void loopsBackToTheFirstFrameAfterTheLastOne() {
        SpriteAnimation walk = new SpriteAnimation(WALK, 100, TICKS_PER_FRAME);
        int oneLoop = 8 * TICKS_PER_FRAME;

        assertNotSame(walk.frameAt(0), walk.frameAt(oneLoop - 1));
        assertSame(walk.frameAt(0), walk.frameAt(oneLoop));
    }

    @Test
    void missingSheetFailsWithItsPath() {
        IllegalStateException missing = assertThrows(IllegalStateException.class,
                () -> new SpriteAnimation(
                        "/sprites/wizard/does-not-exist.png", 100, TICKS_PER_FRAME));

        assertTrue(missing.getMessage().contains("does-not-exist"));
    }
}
