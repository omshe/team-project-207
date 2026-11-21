package view;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.event.KeyEvent;
import javax.swing.JPanel;
import org.junit.jupiter.api.Test;

/** Checks that WASD and the arrow keys are recorded as held and released. No window is opened. */
class KeyboardInputTest {
    private final JPanel source = new JPanel();
    private final KeyboardInput keyboard = new KeyboardInput();

    @Test
    void wasdKeysAreHeldUntilReleased() {
        press(KeyEvent.VK_W);
        press(KeyEvent.VK_A);
        press(KeyEvent.VK_S);
        press(KeyEvent.VK_D);
        assertTrue(keyboard.isUp());
        assertTrue(keyboard.isLeft());
        assertTrue(keyboard.isDown());
        assertTrue(keyboard.isRight());

        release(KeyEvent.VK_W);
        assertFalse(keyboard.isUp());
        assertTrue(keyboard.isDown());
    }

    @Test
    void arrowKeysWorkTheSameAsWasd() {
        press(KeyEvent.VK_UP);
        press(KeyEvent.VK_LEFT);
        assertTrue(keyboard.isUp());
        assertTrue(keyboard.isLeft());

        release(KeyEvent.VK_LEFT);
        assertFalse(keyboard.isLeft());
    }

    @Test
    void otherKeysAreIgnored() {
        press(KeyEvent.VK_SPACE);

        assertFalse(keyboard.isUp());
        assertFalse(keyboard.isDown());
        assertFalse(keyboard.isLeft());
        assertFalse(keyboard.isRight());
    }

    private void press(int keyCode) {
        keyboard.keyPressed(event(KeyEvent.KEY_PRESSED, keyCode));
    }

    private void release(int keyCode) {
        keyboard.keyReleased(event(KeyEvent.KEY_RELEASED, keyCode));
    }

    private KeyEvent event(int id, int keyCode) {
        return new KeyEvent(source, id, 0, 0, keyCode, KeyEvent.CHAR_UNDEFINED);
    }
}
