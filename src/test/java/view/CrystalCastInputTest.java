package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.cast_spell.CastSpellController;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import use_case.cast_spell.CastSpellInputData;

/** Checks cursor capture, repeat suppression and focus safety without an on-screen window. */
class CrystalCastInputTest {
    private final List<CastSpellInputData> requests = new ArrayList<>();
    private int updateCalls;
    private final CastSpellController controller = new CastSpellController(
            request -> requests.add(request), () -> updateCalls++);
    private PointerSurface surface;
    private CrystalCastInput input;

    /** Creates the adapter on Swing's thread with a deterministic pointer source. */
    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            surface = new PointerSurface();
            surface.setSize(960, 640);
            input = new CrystalCastInput(controller, surface, "crystal_burst");
            surface.addKeyListener(input);
            surface.addFocusListener(input);
        });
    }

    @Test
    void freshPressCapturesCurrentCursorAndOnlyQueuesUntilTheGameTick() throws Exception {
        press(KeyEvent.VK_E);
        SwingUtilities.invokeAndWait(() -> surface.pointer.setLocation(500, 400));
        assertTrue(requests.isEmpty());
        assertEquals(0, updateCalls);
        controller.tick();
        assertEquals(1, requests.size());
        assertEquals("crystal_burst", requests.get(0).getSpellId());
        assertEquals(120, requests.get(0).getTargetWorldX());
        assertEquals(80, requests.get(0).getTargetWorldY());
        assertEquals(1, surface.pointerReads);
    }

    @Test
    void heldKeyRepeatsRequireReleaseBeforeAnotherCast() throws Exception {
        for (int repeat = 0; repeat < 20; repeat++) {
            press(KeyEvent.VK_E);
            controller.tick();
        }
        assertEquals(1, requests.size());
        assertEquals(1, surface.pointerReads);
        release(KeyEvent.VK_W);
        press(KeyEvent.VK_E);
        controller.tick();
        assertEquals(1, requests.size());
        release(KeyEvent.VK_E);
        SwingUtilities.invokeAndWait(() -> surface.pointer = new Point(300, 350));
        press(KeyEvent.VK_E);
        controller.tick();
        assertEquals(2, requests.size());
        assertEquals(300, requests.get(1).getTargetWorldX());
        assertEquals(350, requests.get(1).getTargetWorldY());
    }

    @Test
    void movementOverlayKeysAndForeignEventsDoNotQueueCrystal() throws Exception {
        for (int keyCode : new int[] {KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S,
                KeyEvent.VK_D, KeyEvent.VK_UP, KeyEvent.VK_RIGHT, KeyEvent.VK_F3}) {
            press(keyCode);
            release(keyCode);
        }
        SwingUtilities.invokeAndWait(() -> input.keyPressed(new KeyEvent(
                new JPanel(), KeyEvent.KEY_PRESSED, 0, 0, KeyEvent.VK_E,
                KeyEvent.CHAR_UNDEFINED)));
        controller.tick();
        assertTrue(requests.isEmpty());
        assertEquals(0, surface.pointerReads);
    }

    @Test
    void noPointerConsumesThePressWithoutRetargetingWhenTheCursorReturns() throws Exception {
        SwingUtilities.invokeAndWait(() -> surface.pointer = null);
        press(KeyEvent.VK_E);
        SwingUtilities.invokeAndWait(() -> surface.pointer = new Point(120, 80));
        press(KeyEvent.VK_E);
        controller.tick();
        assertTrue(requests.isEmpty());
        release(KeyEvent.VK_E);
        press(KeyEvent.VK_E);
        controller.tick();
        assertEquals(1, requests.size());
    }

    @Test
    void outsideCoordinatesAreIgnoredWhileBothSurfaceEdgesRemainValid() throws Exception {
        for (Point point : List.of(new Point(-1, 0), new Point(0, -1),
                new Point(960, 0), new Point(0, 640))) {
            SwingUtilities.invokeAndWait(() -> surface.pointer = point);
            press(KeyEvent.VK_E);
            release(KeyEvent.VK_E);
        }
        controller.tick();
        assertTrue(requests.isEmpty());
        for (Point point : List.of(new Point(0, 0), new Point(959, 639))) {
            SwingUtilities.invokeAndWait(() -> surface.pointer = point);
            press(KeyEvent.VK_E);
            release(KeyEvent.VK_E);
        }
        controller.tick();
        assertEquals(2, requests.size());
        assertEquals(0, requests.get(0).getTargetWorldX());
        assertEquals(959, requests.get(1).getTargetWorldX());
        assertEquals(639, requests.get(1).getTargetWorldY());
    }

    @Test
    void focusReturnPreservesHeldKeyUntilRelease() throws Exception {
        press(KeyEvent.VK_E);
        controller.tick();
        focus(false);
        press(KeyEvent.VK_E);
        focus(true);
        press(KeyEvent.VK_E);
        controller.tick();
        assertEquals(1, requests.size());
        release(KeyEvent.VK_E);
        press(KeyEvent.VK_E);
        controller.tick();
        assertEquals(2, requests.size());
    }

    @Test
    void actualLostFocusSuppressesInputEvenBeforeItsNotificationArrives() throws Exception {
        SwingUtilities.invokeAndWait(() -> surface.ownsFocus = false);
        press(KeyEvent.VK_E);
        controller.tick();
        assertTrue(requests.isEmpty());
        assertEquals(0, surface.pointerReads);
        focus(true);
        press(KeyEvent.VK_E);
        controller.tick();
        assertTrue(requests.isEmpty());
        release(KeyEvent.VK_E);
        press(KeyEvent.VK_E);
        controller.tick();
        assertEquals(1, requests.size());
    }

    @Test
    void unavailableNativePointerIsIgnoredSafely() throws Exception {
        for (RuntimeException error : List.of(new HeadlessException(),
                new IllegalComponentStateException(), new SecurityException())) {
            SwingUtilities.invokeAndWait(() -> surface.pointerError = error);
            press(KeyEvent.VK_E);
            release(KeyEvent.VK_E);
        }
        controller.tick();
        assertTrue(requests.isEmpty());
    }

    @Test
    void nullDependenciesAreRejected() {
        assertThrows(NullPointerException.class,
                () -> new CrystalCastInput(null, surface, "crystal_burst"));
        assertThrows(NullPointerException.class,
                () -> new CrystalCastInput(controller, null, "crystal_burst"));
        assertThrows(NullPointerException.class,
                () -> new CrystalCastInput(controller, surface, null));
    }

    private void press(int keyCode) throws Exception {
        SwingUtilities.invokeAndWait(() -> input.keyPressed(key(KeyEvent.KEY_PRESSED, keyCode)));
    }

    private void release(int keyCode) throws Exception {
        SwingUtilities.invokeAndWait(() -> input.keyReleased(key(KeyEvent.KEY_RELEASED, keyCode)));
    }

    private KeyEvent key(int eventId, int keyCode) {
        return new KeyEvent(surface, eventId, 0, 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }

    private void focus(boolean ownsFocus) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            surface.ownsFocus = ownsFocus;
            if (ownsFocus) {
                input.focusGained(new FocusEvent(surface, FocusEvent.FOCUS_GAINED));
            } else {
                input.focusLost(new FocusEvent(surface, FocusEvent.FOCUS_LOST));
            }
        });
    }

    /** Supplies current pointer and focus state while exercising the production input adapter. */
    private static final class PointerSurface extends JPanel {
        private Point pointer = new Point(120, 80);
        private boolean ownsFocus = true;
        private int pointerReads;
        private RuntimeException pointerError;

        @Override
        public Point getMousePosition() {
            pointerReads++;
            if (pointerError != null) {
                throw pointerError;
            }
            return pointer;
        }

        @Override
        public boolean isFocusOwner() {
            return ownsFocus;
        }
    }
}
