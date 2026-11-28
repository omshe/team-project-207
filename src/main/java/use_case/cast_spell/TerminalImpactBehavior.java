package use_case.cast_spell;

import entity.ProjectileSpawn;
import java.util.List;
import java.util.Objects;

/** Resolves an ordinary impact into the defined terminal result: no secondary spawns. */
public final class TerminalImpactBehavior implements ProjectileImpactBehavior {
    /** {@inheritDoc} */
    @Override
    public List<ProjectileSpawn> resolve(ProjectileImpactContext context) {
        Objects.requireNonNull(context);
        return List.of();
    }
}
