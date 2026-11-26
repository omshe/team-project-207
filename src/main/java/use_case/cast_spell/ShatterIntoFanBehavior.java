package use_case.cast_spell;

import entity.ProjectileSpawn;
import entity.ShardFan;
import java.util.List;
import java.util.Objects;

/** Resolves a core impact into the configured fan of independent secondary spawns. */
public final class ShatterIntoFanBehavior implements ProjectileImpactBehavior {
    private final ShardFan fan;

    /**
     * Receives domain fan settings from application wiring.
     *
     * @param fan non-null fan geometry and shard definition
     */
    public ShatterIntoFanBehavior(ShardFan fan) {
        this.fan = Objects.requireNonNull(fan);
    }

    /** {@inheritDoc} */
    @Override
    public List<ProjectileSpawn> resolve(ProjectileImpactContext context) {
        return fan.createSpawns(context.getCenterX(), context.getCenterY(),
                context.getVelocityX(), context.getVelocityY());
    }
}
