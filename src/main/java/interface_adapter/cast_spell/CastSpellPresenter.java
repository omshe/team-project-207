package interface_adapter.cast_spell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import use_case.cast_spell.CastSpellOutputBoundary;
import use_case.cast_spell.CastSpellOutputData;
import use_case.cast_spell.ProjectileImpactOutputData;
import use_case.cast_spell.ProjectileOutputData;
import use_case.cast_spell.UpdateProjectilesOutputBoundary;
import use_case.cast_spell.UpdateProjectilesOutputData;

/** Converts casting results and simulation outputs into values for the view. */
public final class CastSpellPresenter
        implements CastSpellOutputBoundary, UpdateProjectilesOutputBoundary {
    private final CastSpellViewModel viewModel;

    /**
     * Creates a presenter shared by the launch and projectile simulation use cases.
     *
     * @param viewModel the destination for feedback and display values
     * @throws NullPointerException if viewModel is null
     */
    public CastSpellPresenter(CastSpellViewModel viewModel) {
        this.viewModel = Objects.requireNonNull(viewModel);
    }

    /** {@inheritDoc} */
    @Override
    public void prepareSuccessView(CastSpellOutputData outputData) {
        viewModel.setStatusMessage("Cast " + outputData.getSpellName() + ".");
    }

    /** {@inheritDoc} */
    @Override
    public void prepareFailView(String errorMessage) {
        viewModel.setStatusMessage(errorMessage);
    }

    /** {@inheritDoc} */
    @Override
    public void present(UpdateProjectilesOutputData outputData) {
        List<ProjectileDisplayData> projectiles = new ArrayList<>();
        for (ProjectileOutputData projectile : outputData.getProjectiles()) {
            projectiles.add(new ProjectileDisplayData(
                    projectile.getSpellId(),
                    projectile.getX() + projectile.getWidth() / 2.0,
                    projectile.getY() + projectile.getHeight() / 2.0,
                    projectile.getWidth(),
                    projectile.getHeight(),
                    Math.atan2(projectile.getVelocityY(), projectile.getVelocityX())));
        }

        List<ProjectileImpactDisplayData> impacts = new ArrayList<>();
        for (ProjectileImpactOutputData impact : outputData.getImpacts()) {
            impacts.add(new ProjectileImpactDisplayData(
                    impact.getSpellId(), impact.getX(), impact.getY()));
        }

        viewModel.updateProjectiles(outputData.getTick(), projectiles, impacts);
    }
}