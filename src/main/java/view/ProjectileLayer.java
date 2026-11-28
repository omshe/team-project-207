package view;

import interface_adapter.cast_spell.CastSpellState;
import interface_adapter.cast_spell.CastSpellViewModel;
import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Draws current projectiles, retained impact animations, and casting feedback.
 * Playback belongs to the view and never changes entities. Paint on Swing's thread.
 */
public final class ProjectileLayer {
    private static final int TICKS_PER_IMPACT_FRAME = 5;

    private final CastSpellViewModel viewModel;
    private final Map<String, ProjectileSprite> sprites;
    private final List<ImpactPlayback> activeImpacts = new ArrayList<>();

    /**
     * Creates a layer with spell visuals registered by identifier.
     *
     * @param viewModel the casting presentation data
     * @param sprites available visuals, supplied by application wiring
     * @throws NullPointerException if a dependency or map entry is null
     */
    public ProjectileLayer(CastSpellViewModel viewModel,
                           Map<String, ProjectileSprite> sprites) {
        this.viewModel = Objects.requireNonNull(viewModel);
        this.sprites = Map.copyOf(sprites);
    }

    /**
     * Registers a thread-safe action, normally a repaint request.
     * The action runs on the publishing game-loop thread.
     *
     * @param action what to do when presentation state changes
     */
    public void whenStateChanges(Runnable action) {
        Objects.requireNonNull(action);
        viewModel.addPropertyChangeListener(event -> action.run());
    }

    /**
     * Draws flight, consumes new impacts, and advances visual playback.
     * Each impact starts at its first frame when consumed. At most one frame is
     * advanced per paint, so a delayed repaint cannot skip the final circular frame.
     * Slow rendering may lengthen playback. Repaints within one tick do not advance it.
     *
     * @param graphics the drawing surface
     * @param panelHeight the game surface height, used to place feedback at the bottom
     * @throws IllegalStateException if a displayed spell has no registered sprite
     */
    public void paint(Graphics2D graphics, int panelHeight) {
        List<ProjectileImpactDisplayData> newImpacts = viewModel.drainImpacts();
        CastSpellState state = viewModel.getState();

        for (ProjectileImpactDisplayData impact : newImpacts) {
            activeImpacts.add(new ImpactPlayback(
                    impact, spriteFor(impact.getProjectileTypeId()), state.getAnimationTick()));
        }

        for (ProjectileDisplayData projectile : state.getProjectiles()) {
            spriteFor(projectile.getProjectileTypeId()).drawFlight(
                    graphics, projectile, state.getAnimationTick());
        }

        Iterator<ImpactPlayback> iterator = activeImpacts.iterator();
        while (iterator.hasNext()) {
            ImpactPlayback playback = iterator.next();

            if (state.getAnimationTick() - playback.frameStartedTick
                    >= TICKS_PER_IMPACT_FRAME) {
                playback.frame++;
                playback.frameStartedTick = state.getAnimationTick();
            }

            if (playback.frame >= playback.sprite.getImpactFrameCount()) {
                iterator.remove();
            } else {
                playback.sprite.drawImpact(graphics, playback.impact, playback.frame);
            }
        }

        Graphics2D copy = (Graphics2D) graphics.create();
        try {
            copy.setColor(Color.ORANGE);
            copy.drawString(state.getStatusMessage(), 12, panelHeight - 12);
        } finally {
            copy.dispose();
        }
    }

    /** Fails clearly for a visual registration mistake in application wiring. */
    private ProjectileSprite spriteFor(String spellId) {
        ProjectileSprite sprite = sprites.get(spellId);
        if (sprite == null) {
            throw new IllegalStateException("No projectile sprite registered for " + spellId);
        }
        return sprite;
    }

    /** Mutable playback state owned solely by the rendering thread. */
    private static final class ImpactPlayback {
        private final ProjectileImpactDisplayData impact;
        private final ProjectileSprite sprite;
        private int frame;
        private long frameStartedTick;

        private ImpactPlayback(ProjectileImpactDisplayData impact, ProjectileSprite sprite,
                               long frameStartedTick) {
            this.impact = impact;
            this.sprite = sprite;
            this.frameStartedTick = frameStartedTick;
        }
    }
}
