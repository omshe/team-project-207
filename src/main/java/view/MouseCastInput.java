package view;

import interface_adapter.cast_spell.CastSpellController;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;
import javax.swing.SwingUtilities;

/**
 * Supplies a known spell and clicked aim point for development before recognition exists.
 * Attach only to the game surface, whose current coordinates map directly to world pixels.
 */
public final class MouseCastInput extends MouseAdapter {
    private final CastSpellController controller;
    private final String spellId;

    /**
     * Creates the development mouse adapter.
     *
     * @param controller queues requests for processing on the game-loop thread
     * @param spellId the development spell identifier, supplied by application wiring
     * @throws NullPointerException if controller or spellId is null
     */
    public MouseCastInput(CastSpellController controller, String spellId) {
        this.controller = Objects.requireNonNull(controller);
        this.spellId = Objects.requireNonNull(spellId);
    }

    /**
     * Queues one request per left-button press and requests keyboard focus for the surface.
     * Other buttons are ignored. Holding, dragging, releasing, and click notifications
     * do not enqueue further requests because only the press handler is overridden.
     * Called on Swing's event thread; neither use case runs on this thread.
     *
     * @param event the mouse press relative to the game surface
     */
    @Override
    public void mousePressed(MouseEvent event) {
        if (SwingUtilities.isLeftMouseButton(event)) {
            event.getComponent().requestFocusInWindow();
            controller.queueCast(spellId, event.getX(), event.getY());
        }
    }
}
