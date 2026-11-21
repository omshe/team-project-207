package interface_adapter.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import use_case.cast_spell.CastSpellOutputData;
import use_case.cast_spell.ProjectileImpactOutputData;
import use_case.cast_spell.ProjectileOutputData;
import use_case.cast_spell.UpdateProjectilesOutputData;

/** Checks display conversion, feedback, and impact delivery through the presenter. */
class CastSpellPresenterTest {
    private final CastSpellViewModel viewModel = new CastSpellViewModel();
    private final CastSpellPresenter presenter = new CastSpellPresenter(viewModel);

    /** Flight display uses the hitbox center and rotation derived from velocity. */
    @ParameterizedTest
    @CsvSource({
            "3, 4, 0.9272952180016122",
            "0, 1, 1.5707963267948966",
            "-1, 0, 3.141592653589793",
            "0, -1, -1.5707963267948966",
            "0, 0, 0"
    })
    void convertsFlightGeometryAndOrientation(double velocityX, double velocityY,
                                              double expectedAngle) {
        presenter.present(new UpdateProjectilesOutputData(12,
                List.of(new ProjectileOutputData(
                        "fireball", 10.25, 20.5, 6, 8, velocityX, velocityY)),
                List.of()));

        CastSpellState state = viewModel.getState();
        ProjectileDisplayData projectile = state.getProjectiles().get(0);
        assertEquals(12, state.getAnimationTick());
        assertEquals("fireball", projectile.getSpellId());
        assertEquals(13.25, projectile.getCenterX());
        assertEquals(24.5, projectile.getCenterY());
        assertEquals(6, projectile.getWidth());
        assertEquals(8, projectile.getHeight());
        assertEquals(expectedAngle, projectile.getAngleRadians(), 1e-12);
        assertTrue(viewModel.drainImpacts().isEmpty());
    }

    /** Feedback survives simulation updates and changes with the next cast result. */
    @Test
    void feedbackPersistsUntilAnotherCastResult() {
        presenter.prepareSuccessView(new CastSpellOutputData("fireball", "Fireball"));
        assertEquals("Cast Fireball.", viewModel.getState().getStatusMessage());

        presenter.prepareFailView("That spell is unavailable.");
        presenter.present(new UpdateProjectilesOutputData(1, List.of(), List.of()));
        assertEquals("That spell is unavailable.", viewModel.getState().getStatusMessage());

        presenter.prepareSuccessView(new CastSpellOutputData("fireball", "Fireball"));
        assertEquals("Cast Fireball.", viewModel.getState().getStatusMessage());
    }

    /** Impact coordinates already describe centers and must not be offset again. */
    @Test
    void convertsImpactEventsInOrder() {
        presenter.present(new UpdateProjectilesOutputData(1, List.of(), List.of(
                new ProjectileImpactOutputData("fireball", 40.25, 50.5),
                new ProjectileImpactOutputData("other", 70.75, 80.5))));

        List<ProjectileImpactDisplayData> impacts = viewModel.drainImpacts();
        assertEquals(2, impacts.size());
        assertEquals("fireball", impacts.get(0).getSpellId());
        assertEquals(40.25, impacts.get(0).getCenterX());
        assertEquals(50.5, impacts.get(0).getCenterY());
        assertEquals("other", impacts.get(1).getSpellId());
        assertEquals(70.75, impacts.get(1).getCenterX());
        assertEquals(80.5, impacts.get(1).getCenterY());
    }

    /** A later empty snapshot removes flight visuals without altering old snapshots. */
    @Test
    void snapshotsAreIndependentAndExpiredProjectilesDisappear() {
        presenter.present(new UpdateProjectilesOutputData(1,
                List.of(new ProjectileOutputData("fireball", 10, 20, 4, 4, 3, 0)),
                List.of()));
        CastSpellState earlier = viewModel.getState();

        presenter.present(new UpdateProjectilesOutputData(2, List.of(), List.of()));
        CastSpellState later = viewModel.getState();

        assertNotSame(earlier, later);
        assertEquals(1, earlier.getProjectiles().size());
        assertEquals(12, earlier.getProjectiles().get(0).getCenterX());
        assertTrue(later.getProjectiles().isEmpty());
        assertEquals(2, later.getAnimationTick());
    }

    /** Every simulation output produces one complete snapshot notification. */
    @Test
    void simulationNotifiesOnceWithThePublishedSnapshot() {
        List<PropertyChangeEvent> events = new ArrayList<>();
        viewModel.addPropertyChangeListener(event -> events.add(event));
        final CastSpellState before = viewModel.getState();

        presenter.present(new UpdateProjectilesOutputData(1, List.of(), List.of()));

        assertEquals(1, events.size());
        assertEquals(CastSpellViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
        assertSame(before, events.get(0).getOldValue());
        assertSame(viewModel.getState(), events.get(0).getNewValue());
    }

    /** Empty ticks preserve pending impacts but cannot replay a consumed event. */
    @Test
    void slowerRenderingReceivesEachImpactOnce() {
        presenter.present(new UpdateProjectilesOutputData(1, List.of(),
                List.of(new ProjectileImpactOutputData("fireball", 40, 50))));
        presenter.present(new UpdateProjectilesOutputData(2, List.of(), List.of()));

        assertEquals(1, viewModel.drainImpacts().size());

        presenter.present(new UpdateProjectilesOutputData(3, List.of(), List.of()));
        assertTrue(viewModel.drainImpacts().isEmpty());
    }

    /** Wiring requires a destination for presentation output. */
    @Test
    void nullViewModelIsRejected() {
        assertThrows(NullPointerException.class, () -> new CastSpellPresenter(null));
    }
}
