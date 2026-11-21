package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.cast_spell.CastSpellController;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import use_case.cast_spell.CastSpellInputData;

/**
 * Checks development mouse input without opening a window or changing real game state.
 */
class MouseCastInputTest {
    private final List<CastSpellInputData> requests = new ArrayList<>();
    private int updateCalls;
    private final CastSpellController controller = new CastSpellController(
            request -> requests.add(request), () -> updateCalls++);
    private final MouseCastInput input = new MouseCastInput(controller, "fireball");
    private JPanel surface;

    /** Registers the adapter on an off-screen surface using Swing's event thread. */
    @BeforeEach
    void setUp() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            surface = new JPanel();
            surface.setSize(960, 640);
            surface.addMouseListener(input);
            surface.addMouseMotionListener(input);
        });
    }

    /** Mouse input queues the captured coordinates until the game thread processes them. */
    @Test
    void leftPressQueuesOneRequestWithoutRunningUseCases() throws Exception {
        send(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1, 120, 80, 0);

        assertTrue(requests.isEmpty());
        assertEquals(0, updateCalls);

        controller.tick();

        assertEquals(1, requests.size());
        CastSpellInputData request = requests.get(0);
        assertEquals("fireball", request.getSpellId());
        assertEquals(120, request.getTargetWorldX());
        assertEquals(80, request.getTargetWorldY());
        assertEquals(1, updateCalls);
    }

    /** Middle and right presses must not create casting requests. */
    @ParameterizedTest
    @ValueSource(ints = {MouseEvent.BUTTON2, MouseEvent.BUTTON3})
    void otherMouseButtonsAreIgnored(int button) throws Exception {
        send(MouseEvent.MOUSE_PRESSED, button, 120, 80, 0);

        controller.tick();

        assertTrue(requests.isEmpty());
        assertEquals(1, updateCalls);
    }

    /** Holding, dragging, releasing, and clicking cannot repeat the original cast. */
    @Test
    void onePressDoesNotRepeatAcrossOtherMouseEventsOrTicks() throws Exception {
        send(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1, 120, 80, 0);
        controller.tick();

        for (int i = 0; i < 3; i++) {
            send(MouseEvent.MOUSE_DRAGGED, MouseEvent.NOBUTTON,
                    200 + i, 150, InputEvent.BUTTON1_DOWN_MASK);
            controller.tick();
        }
        send(MouseEvent.MOUSE_RELEASED, MouseEvent.BUTTON1, 250, 150, 0);
        send(MouseEvent.MOUSE_CLICKED, MouseEvent.BUTTON1, 250, 150, 0);
        send(MouseEvent.MOUSE_MOVED, MouseEvent.NOBUTTON, 300, 200, 0);
        controller.tick();

        assertEquals(1, requests.size());
        assertEquals(120, requests.get(0).getTargetWorldX());
        assertEquals(80, requests.get(0).getTargetWorldY());
        assertEquals(5, updateCalls);
    }

    /** Each separate left-button press supplies its own captured aim point. */
    @Test
    void separatePressesProduceSeparateRequestsInOrder() throws Exception {
        send(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1, 100, 150, 0);
        send(MouseEvent.MOUSE_RELEASED, MouseEvent.BUTTON1, 100, 150, 0);
        send(MouseEvent.MOUSE_CLICKED, MouseEvent.BUTTON1, 100, 150, 0);
        send(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1, 300, 350, 0);
        send(MouseEvent.MOUSE_RELEASED, MouseEvent.BUTTON1, 300, 350, 0);
        send(MouseEvent.MOUSE_CLICKED, MouseEvent.BUTTON1, 300, 350, 0);

        controller.tick();
        controller.tick();

        assertEquals(2, requests.size());
        assertEquals(100, requests.get(0).getTargetWorldX());
        assertEquals(150, requests.get(0).getTargetWorldY());
        assertEquals(300, requests.get(1).getTargetWorldX());
        assertEquals(350, requests.get(1).getTargetWorldY());
    }

    /** The application supplies spell selection; the mouse adapter forwards it unchanged. */
    @Test
    void spellIdentifierComesFromApplicationConfiguration() throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            surface.removeMouseListener(input);
            surface.addMouseListener(new MouseCastInput(controller, "another-spell"));
        });
        send(MouseEvent.MOUSE_PRESSED, MouseEvent.BUTTON1, 0, 0, 0);

        controller.tick();

        assertEquals(1, requests.size());
        assertEquals("another-spell", requests.get(0).getSpellId());
        assertEquals(0, requests.get(0).getTargetWorldX());
        assertEquals(0, requests.get(0).getTargetWorldY());
    }

    /** Both dependencies must be supplied when the input adapter is wired. */
    @Test
    void nullConfigurationIsRejected() {
        assertThrows(NullPointerException.class,
                () -> new MouseCastInput(null, "fireball"));
        assertThrows(NullPointerException.class,
                () -> new MouseCastInput(controller, null));
    }

    /** Dispatches one mouse event on Swing's event thread, then waits for completion. */
    private void send(int eventId, int button, int x, int y, int modifiers)
            throws Exception {
        SwingUtilities.invokeAndWait(() -> surface.dispatchEvent(new MouseEvent(
                surface, eventId, 0, modifiers, x, y, 1, false, button)));
    }
}
