package interface_adapter.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Checks snapshot publication and reliable delivery of queued impact events. */
class CastSpellViewModelTest {
    private final CastSpellViewModel viewModel = new CastSpellViewModel();

    /** A new session has no feedback, projectiles, or pending impacts. */
    @Test
    void startsEmpty() {
        assertEquals(0, viewModel.getState().getAnimationTick());
        assertEquals("", viewModel.getState().getStatusMessage());
        assertTrue(viewModel.getState().getProjectiles().isEmpty());
        assertTrue(viewModel.drainImpacts().isEmpty());
    }

    /** Simulation and feedback publications preserve each other's values. */
    @Test
    void feedbackAndFlightUpdatesPreserveOtherState() {
        ProjectileDisplayData projectile =
                new ProjectileDisplayData("fireball", 10, 20, 4, 4, 0);
        ProjectileImpactDisplayData impact =
                new ProjectileImpactDisplayData("fireball", 30, 40);
        viewModel.setStatusMessage("First message");
        viewModel.updateProjectiles(5, List.of(projectile), List.of(impact));

        assertEquals("First message", viewModel.getState().getStatusMessage());
        CastSpellState before = viewModel.getState();
        viewModel.setStatusMessage("Second message");

        assertEquals("First message", before.getStatusMessage());
        assertEquals("Second message", viewModel.getState().getStatusMessage());
        assertEquals(5, viewModel.getState().getAnimationTick());
        assertSame(projectile, viewModel.getState().getProjectiles().get(0));
        assertSame(impact, viewModel.drainImpacts().get(0));
    }

    /** Distinct impacts at the same location are retained in publication order. */
    @Test
    void impactsAccumulateUntilDrainedAndAreNotDeduplicated() {
        ProjectileImpactDisplayData first =
                new ProjectileImpactDisplayData("fireball", 30, 40);
        ProjectileImpactDisplayData second =
                new ProjectileImpactDisplayData("fireball", 30, 40);
        viewModel.updateProjectiles(1, List.of(), List.of(first));
        viewModel.updateProjectiles(2, List.of(), List.of());
        viewModel.updateProjectiles(3, List.of(), List.of(second));
        viewModel.getState();
        viewModel.getState();

        List<ProjectileImpactDisplayData> consumed = viewModel.drainImpacts();

        assertEquals(2, consumed.size());
        assertSame(first, consumed.get(0));
        assertSame(second, consumed.get(1));
        assertTrue(viewModel.drainImpacts().isEmpty());
        assertEquals(3, viewModel.getState().getAnimationTick());
    }

    /** Neither caller list changes nor later drains can alter delivered events. */
    @Test
    void impactCollectionsAreCopiedAndReadOnly() {
        ProjectileImpactDisplayData impact =
                new ProjectileImpactDisplayData("fireball", 30, 40);
        List<ProjectileImpactDisplayData> supplied = new ArrayList<>(List.of(impact));
        viewModel.updateProjectiles(1, List.of(), supplied);
        supplied.clear();

        List<ProjectileImpactDisplayData> consumed = viewModel.drainImpacts();
        assertThrows(UnsupportedOperationException.class, () -> consumed.clear());
        viewModel.updateProjectiles(2, List.of(), List.of(impact));
        viewModel.drainImpacts();
        assertEquals(1, consumed.size());
        assertSame(impact, consumed.get(0));
    }

    /** Notifications describe publication, and draining is silent. */
    @Test
    void listenersObservePublicationAndCanBeRemoved() {
        List<PropertyChangeEvent> events = new ArrayList<>();
        PropertyChangeListener listener = event -> events.add(event);
        viewModel.addPropertyChangeListener(listener);
        CastSpellState before = viewModel.getState();

        viewModel.setStatusMessage("Message");
        CastSpellState after = viewModel.getState();
        viewModel.drainImpacts();

        assertEquals(1, events.size());
        assertEquals(CastSpellViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
        assertSame(before, events.get(0).getOldValue());
        assertSame(after, events.get(0).getNewValue());

        viewModel.removePropertyChangeListener(listener);
        viewModel.updateProjectiles(1, List.of(), List.of());
        assertEquals(1, events.size());
    }

    /** Invalid publications must leave both the snapshot and event queue unchanged. */
    @Test
    void invalidPublicationDoesNotPartiallyUpdateState() {
        ProjectileImpactDisplayData impact =
                new ProjectileImpactDisplayData("fireball", 30, 40);
        viewModel.updateProjectiles(1, List.of(), List.of(impact));
        CastSpellState before = viewModel.getState();

        assertThrows(NullPointerException.class, () -> viewModel.setStatusMessage(null));
        assertThrows(NullPointerException.class,
                () -> viewModel.updateProjectiles(2, null, List.of(impact)));
        assertThrows(NullPointerException.class,
                () -> viewModel.updateProjectiles(2, List.of(), null));
        List<ProjectileImpactDisplayData> invalid = new ArrayList<>();
        invalid.add(null);
        assertThrows(NullPointerException.class,
                () -> viewModel.updateProjectiles(2, List.of(), invalid));

        assertSame(before, viewModel.getState());
        assertEquals(List.of(impact), viewModel.drainImpacts());
    }

    /** A publishing thread and rendering consumer cannot lose or duplicate events. */
    @Test
    void concurrentPublishingAndDrainingPreserveEveryImpact() throws Exception {
        List<ProjectileImpactDisplayData> expected = new ArrayList<>();
        for (int i = 0; i < 200; i++) {
            expected.add(new ProjectileImpactDisplayData("fireball", i, 40));
        }
        ExecutorService threads = Executors.newFixedThreadPool(2);
        try {
            Future<?> producer = threads.submit(() -> {
                for (int i = 0; i < expected.size(); i++) {
                    viewModel.updateProjectiles(i + 1, List.of(), List.of(expected.get(i)));
                }
            });
            Future<List<ProjectileImpactDisplayData>> consumer = threads.submit(() -> {
                List<ProjectileImpactDisplayData> received = new ArrayList<>();
                for (int i = 0; i < 200; i++) {
                    received.addAll(viewModel.drainImpacts());
                }
                return received;
            });

            producer.get(5, TimeUnit.SECONDS);
            List<ProjectileImpactDisplayData> received = consumer.get(5, TimeUnit.SECONDS);
            received.addAll(viewModel.drainImpacts());

            assertEquals(expected, received);
            assertEquals(200, viewModel.getState().getAnimationTick());
            assertTrue(viewModel.drainImpacts().isEmpty());
        } finally {
            threads.shutdownNow();
        }
    }
}