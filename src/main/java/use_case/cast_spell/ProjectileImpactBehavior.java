package use_case.cast_spell;

import entity.ProjectileSpawn;
import java.util.List;

/** Strategy for the secondary projectiles produced by one consumed projectile. */
public interface ProjectileImpactBehavior {
    /**
     * Resolves a contact without mutating storage, entities or presentation.
     * The interactor owns consuming the parent and applying the enemy hit.
     *
     * @param context non-null immutable contact geometry
     * @return finite immutable non-null spawn list; empty means a terminal impact
     */
    List<ProjectileSpawn> resolve(ProjectileImpactContext context);
}
