package view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import interface_adapter.cast_spell.CastSpellViewModel;
import interface_adapter.cast_spell.ProjectileDisplayData;
import interface_adapter.cast_spell.ProjectileImpactDisplayData;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

/** Checks flight selection and retained impact playback without opening a window. */
class ProjectileLayerTest {
    private final CastSpellViewModel viewModel = new CastSpellViewModel();
    private final RecordingSprite sprite = new RecordingSprite();
    private final ProjectileLayer layer = new ProjectileLayer(
            viewModel, Map.of("fireball", sprite));

    /** Each spell is drawn with its registered sprite and the current animation tick. */
    @Test
    void flightUsesTheLatestSnapshotAndRegisteredVisuals() {
        RecordingSprite otherSprite = new RecordingSprite();
        ProjectileLayer registered = new ProjectileLayer(viewModel,
                Map.of("fireball", sprite, "other", otherSprite));

        ProjectileDisplayData fireball =
                new ProjectileDisplayData("fireball", 10, 20, 24, 16, 0);
        ProjectileDisplayData other =
                new ProjectileDisplayData("other", 30, 40, 24, 16, 1);

        viewModel.updateProjectiles(12, List.of(fireball, other), List.of());
        paint(registered);

        assertEquals(List.of(fireball), sprite.flights);
        assertEquals(List.of(other), otherSprite.flights);
        assertEquals(List.of(12L), sprite.ticks);
        assertTrue(sprite.frames.isEmpty());
    }

    /** Impact frames are held for five ticks, then removed after the final frame. */
    @Test
    void impactPlaysOnceAndExpiresAfterItsFinalFrame() {
        ProjectileImpactDisplayData impact = impact();
        viewModel.updateProjectiles(1, List.of(), List.of(impact));

        paint(layer);
        paint(layer);
        advanceAndPaint(6);
        advanceAndPaint(11);
        advanceAndPaint(16);
        advanceAndPaint(21);

        assertEquals(List.of(0, 0, 1, 2), sprite.frames);
        assertSame(impact, sprite.impacts.get(0));
        assertTrue(viewModel.drainImpacts().isEmpty());
    }

    /** Slow repaints stretch playback rather than skipping the final circular frame. */
    @Test
    void delayedRenderingStillShowsEveryImpactFrame() {
        viewModel.updateProjectiles(1, List.of(), List.of(impact()));

        paint(layer);
        advanceAndPaint(100);
        advanceAndPaint(200);
        advanceAndPaint(300);

        assertEquals(List.of(0, 1, 2), sprite.frames);
    }

    /** Identical locations still represent separate queued impact animations. */
    @Test
    void multipleImpactsSurviveUntilRenderingConsumesThem() {
        viewModel.updateProjectiles(1, List.of(), List.of(impact()));
        viewModel.updateProjectiles(2, List.of(), List.of(impact()));

        paint(layer);
        advanceAndPaint(7);

        assertEquals(List.of(0, 0, 1, 1), sprite.frames);
    }

    /** An empty flight snapshot removes flight without inventing an impact event. */
    @Test
    void disappearanceWithoutAnEventDoesNotStartImpactPlayback() {
        viewModel.updateProjectiles(1,
                List.of(new ProjectileDisplayData("fireball", 10, 20, 24, 16, 0)),
                List.of());

        paint(layer);
        advanceAndPaint(2);

        assertEquals(1, sprite.flights.size());
        assertTrue(sprite.frames.isEmpty());
    }

    /** Publishing presentation data can request repaint without running rendering there. */
    @Test
    void stateChangesNotifyTheRegisteredAction() {
        List<String> calls = new ArrayList<>();
        layer.whenStateChanges(() -> calls.add("repaint"));

        viewModel.setStatusMessage("Cast Fireball.");
        viewModel.updateProjectiles(1, List.of(), List.of());

        assertEquals(List.of("repaint", "repaint"), calls);
        assertTrue(sprite.flights.isEmpty());
        assertTrue(sprite.frames.isEmpty());
    }

    /** Missing visual registration is a clear application-wiring error. */
    @Test
    void unregisteredVisualFailsClearly() {
        viewModel.updateProjectiles(1,
                List.of(new ProjectileDisplayData("unknown", 10, 20, 24, 16, 0)),
                List.of());

        assertThrows(IllegalStateException.class, () -> paint(layer));
    }

    /** Advances only presentation time; the layer cannot mutate game entities. */
    private void advanceAndPaint(long tick) {
        viewModel.updateProjectiles(tick, List.of(), List.of());
        paint(layer);
    }

    /** Creates an impact event at a fixed location. */
    private static ProjectileImpactDisplayData impact() {
        return new ProjectileImpactDisplayData("fireball", 50, 60);
    }

    /** Paints into an off-screen image and releases its graphics context. */
    private static void paint(ProjectileLayer layer) {
        BufferedImage image = new BufferedImage(200, 200, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        try {
            layer.paint(graphics, image.getHeight());
        } finally {
            graphics.dispose();
        }
    }

    /** Records draw requests so playback can be checked independently of image pixels. */
    private static final class RecordingSprite implements ProjectileSprite {
        private final List<ProjectileDisplayData> flights = new ArrayList<>();
        private final List<Long> ticks = new ArrayList<>();
        private final List<ProjectileImpactDisplayData> impacts = new ArrayList<>();
        private final List<Integer> frames = new ArrayList<>();

        @Override
        public void drawFlight(Graphics2D graphics, ProjectileDisplayData projectile,
                               long tick) {
            flights.add(projectile);
            ticks.add(tick);
        }

        @Override
        public void drawImpact(Graphics2D graphics, ProjectileImpactDisplayData impact,
                               int frame) {
            if (frame < 0 || frame >= getImpactFrameCount()) {
                throw new IndexOutOfBoundsException();
            }
            impacts.add(impact);
            frames.add(frame);
        }

        @Override
        public int getImpactFrameCount() {
            return 3;
        }
    }
}
