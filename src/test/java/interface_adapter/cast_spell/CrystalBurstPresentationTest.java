package interface_adapter.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.cast_spell.ProjectileImpactOutputData;
import use_case.cast_spell.ProjectileOutputData;
import use_case.cast_spell.UpdateProjectilesOutputData;

/** Checks distinct types and retained events without exposing entities to the view. */
class CrystalBurstPresentationTest {
    @Test
    void newbornShardsUseActualVelocityAndImpactsAreRetainedOnce() {
        CastSpellViewModel model = new CastSpellViewModel();
        CastSpellPresenter presenter = new CastSpellPresenter(model);
        presenter.present(new UpdateProjectilesOutputData(1,
                List.of(new ProjectileOutputData("crystal_burst", "crystal_shard",
                        20, 30, 8, 8, 0, 10)),
                List.of(new ProjectileImpactOutputData("crystal_burst", "crystal_core", 24, 34))));
        presenter.present(new UpdateProjectilesOutputData(2,
                List.of(new ProjectileOutputData("crystal_burst", "crystal_shard",
                        20, 40, 8, 8, 0, 10)), List.of()));
        ProjectileDisplayData display = model.getState().getProjectiles().get(0);
        assertEquals("crystal_burst", display.getSpellId());
        assertEquals("crystal_shard", display.getProjectileTypeId());
        assertEquals(Math.PI / 2, display.getAngleRadians());
        List<ProjectileImpactDisplayData> impacts = model.drainImpacts();
        assertEquals(1, impacts.size());
        assertEquals("crystal_core", impacts.get(0).getProjectileTypeId());
        assertTrue(model.drainImpacts().isEmpty());
    }
}
