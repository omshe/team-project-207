package entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Creates a configured fan using only the incoming direction, never enemy positions. */
public final class ShardFan {
    private final ProjectileDefinition shard;
    private final List<Double> anglesRadians;

    /**
     * Creates immutable fan settings. List order determines stable shard order.
     *
     * @param shard definition of every secondary projectile
     * @param anglesDegrees non-empty finite offsets from the incoming direction
     */
    public ShardFan(ProjectileDefinition shard, List<Double> anglesDegrees) {
        this.shard = Objects.requireNonNull(shard);
        List<Double> angles = List.copyOf(anglesDegrees);
        if (angles.isEmpty()) {
            throw new IllegalArgumentException("A shard fan needs at least one direction");
        }
        List<Double> radians = new ArrayList<>();
        for (double angle : angles) {
            if (!Double.isFinite(angle) || angle < -180 || angle > 180) {
                throw new IllegalArgumentException("Fan angles must be finite within [-180, 180]");
            }
            radians.add(Math.toRadians(angle));
        }
        anglesRadians = List.copyOf(radians);
    }

    /**
     * Calculates fixed velocities at the collision centre, with equal configured speeds.
     *
     * @param centerX finite collision centre X
     * @param centerY finite collision centre Y
     * @param incomingX finite nonzero incoming velocity component pair
     * @param incomingY finite nonzero incoming velocity component pair
     * @return immutable finite spawns, independent of targets or later movement
     * @throws IllegalArgumentException if the incoming direction is not representable
     */
    public List<ProjectileSpawn> createSpawns(double centerX, double centerY,
                                               double incomingX, double incomingY) {
        double length = Math.hypot(incomingX, incomingY);
        if (length == 0 || !Double.isFinite(length)) {
            throw new IllegalArgumentException("A fan needs a finite nonzero incoming direction");
        }
        double unitX = incomingX / length;
        double unitY = incomingY / length;
        List<ProjectileSpawn> result = new ArrayList<>();
        for (double angle : anglesRadians) {
            double cosine = Math.cos(angle);
            double sine = Math.sin(angle);
            result.add(new ProjectileSpawn(shard, centerX, centerY,
                    (unitX * cosine - unitY * sine) * shard.getSpeed(),
                    (unitX * sine + unitY * cosine) * shard.getSpeed()));
        }
        return List.copyOf(result);
    }
}
