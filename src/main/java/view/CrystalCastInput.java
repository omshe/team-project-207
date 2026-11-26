package view;

import interface_adapter.cast_spell.CastSpellController;
import java.awt.Component;
import java.awt.HeadlessException;
import java.awt.IllegalComponentStateException;
import java.awt.Point;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Objects;

/** Captures one cursor-aimed Crystal Burst request per E press on Swing's event thread. */
public final class CrystalCastInput extends KeyAdapter implements FocusListener {
    private final CastSpellController controller;
    private final Component surface;
    private final String spellId;
    private boolean focused;
    private boolean pressed;

    /**
     * Creates the E-key adapter; register it as both key and focus listener on the surface.
     * The current game surface uses world pixels directly, as MouseCastInput does.
     *
     * @param controller queues immutable requests without executing game rules
     * @param surface game surface whose current pointer position supplies the aim
     * @param spellId spell identifier selected by application wiring
     * @throws NullPointerException if a dependency is null
     */
    public CrystalCastInput(CastSpellController controller, Component surface, String spellId) {
        this.controller = Objects.requireNonNull(controller);
        this.surface = Objects.requireNonNull(surface);
        this.spellId = Objects.requireNonNull(spellId);
        focused = surface.isFocusOwner();
    }

    /**
     * Captures the pointer only on the first E press; repeat events never enqueue more casts.
     * A missing, inaccessible or out-of-surface pointer consumes this press without casting.
     * Invalid or coincident gameplay aim remains the casting interactor's responsibility.
     *
     * @param event key input from the registered game surface
     */
    @Override
    public void keyPressed(KeyEvent event) {
        if (event.getComponent() != surface || event.getKeyCode() != KeyEvent.VK_E || pressed) {
            return;
        }
        pressed = true;
        if (!focused || !surface.isFocusOwner()) {
            return;
        }
        Point pointer;
        try {
            pointer = surface.getMousePosition();
        } catch (HeadlessException | IllegalComponentStateException | SecurityException exception) {
            return;
        }
        if (pointer != null && surface.contains(pointer)) {
            controller.queueCast(spellId, pointer.x, pointer.y);
        }
    }

    /**
     * Rearms E after a release; unrelated keys cannot clear the repeat latch.
     *
     * @param event key release from the registered game surface
     */
    @Override
    public void keyReleased(KeyEvent event) {
        if (event.getComponent() == surface && event.getKeyCode() == KeyEvent.VK_E) {
            pressed = false;
        }
    }

    /**
     * Resumes accepting input without casting or clearing a possibly held E key.
     *
     * @param event focus notification for the registered game surface
     */
    @Override
    public void focusGained(FocusEvent event) {
        if (event.getComponent() == surface) {
            focused = true;
        }
    }

    /**
     * Suspends input and keeps the E latch until release, preventing a cast on focus return.
     * If a release happened outside the surface, release E once after focus returns to rearm.
     * Already queued legitimate requests are not removed from the shared casting queue.
     *
     * @param event focus notification for the registered game surface
     */
    @Override
    public void focusLost(FocusEvent event) {
        if (event.getComponent() == surface) {
            focused = false;
        }
    }
}
