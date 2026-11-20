package interface_adapter.enemy_wave;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.generate_mob_waves.GenerateMobWavesOutputData;

class EnemyWaveViewModelTest {

    @Test
    void notifiesListenersWithOldAndNewState() {
        EnemyWaveViewModel viewModel = new EnemyWaveViewModel();
        List<PropertyChangeEvent> events = new ArrayList<>();
        viewModel.addPropertyChangeListener(event -> events.add(event));
        final EnemyWaveState before = viewModel.getState();
        EnemyWaveState after = new EnemyWaveState("Wave 1", "", new ArrayList<>(), 1);

        viewModel.setState(after);

        assertEquals(1, events.size());
        assertEquals(EnemyWaveViewModel.STATE_PROPERTY, events.get(0).getPropertyName());
        assertSame(before, events.get(0).getOldValue());
        assertSame(after, events.get(0).getNewValue());
        assertSame(after, viewModel.getState());
    }

    @Test
    void presenterPublishingNotifiesOncePerTick() {
        EnemyWaveViewModel viewModel = new EnemyWaveViewModel();
        EnemyWavePresenter presenter = new EnemyWavePresenter(viewModel);
        List<PropertyChangeEvent> events = new ArrayList<>();
        viewModel.addPropertyChangeListener(event -> events.add(event));

        presenter.prepareSuccessView(new GenerateMobWavesOutputData(1, new ArrayList<>()));
        presenter.prepareFailView(new GenerateMobWavesOutputData(1, new ArrayList<>()), "x");

        assertEquals(2, events.size());
        assertSame(viewModel.getState(), events.get(1).getNewValue());
    }
}
