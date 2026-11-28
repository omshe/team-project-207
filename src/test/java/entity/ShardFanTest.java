package entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks fan rotation, immutable settings and the absence of target-dependent direction. */
class ShardFanTest {
    private static final double TOLERANCE = 1e-9;
    private final ProjectileDefinition shard = new ProjectileDefinition(
            new CrystalBurstSpell(), "crystal_shard", 10, 8, 8);

    @Test
    void rotatesThreeEqualSpeedShardsForEveryIncomingDirection() {
        List<Double> angles = new ArrayList<>(List.of(-25.0, 0.0, 25.0));
        ShardFan fan = new ShardFan(shard, angles);
        angles.clear();
        for (double heading : new double[] {0, 45, 90, 180, -90}) {
            double incoming = Math.toRadians(heading);
            List<ProjectileSpawn> spawns = fan.createSpawns(
                    100, 120, 8 * Math.cos(incoming), 8 * Math.sin(incoming));
            assertEquals(3, spawns.size());
            for (int index = 0; index < spawns.size(); index++) {
                Projectile projectile = spawns.get(index).createProjectile();
                double expected = Math.toRadians(heading - 25 + index * 25);
                assertEquals(10 * Math.cos(expected), projectile.getVelocityX(), TOLERANCE);
                assertEquals(10 * Math.sin(expected), projectile.getVelocityY(), TOLERANCE);
                assertEquals(100, projectile.getHitbox().getCenterX(), TOLERANCE);
                assertEquals(120, projectile.getHitbox().getCenterY(), TOLERANCE);
                assertEquals("crystal_burst", projectile.getSpell().getId());
                assertEquals("crystal_shard", projectile.getProjectileTypeId());
            }
            assertThrows(UnsupportedOperationException.class, () -> spawns.clear());
        }
    }

    @Test
    void rejectsInvalidFanAndSpawnGeometry() {
        assertThrows(IllegalArgumentException.class, () -> new ShardFan(shard, List.of()));
        assertThrows(IllegalArgumentException.class,
                () -> new ShardFan(shard, List.of(Double.NaN)));
        assertThrows(IllegalArgumentException.class,
                () -> new ShardFan(shard, List.of(181.0)));
        ShardFan fan = new ShardFan(shard, List.of(0.0));
        assertThrows(IllegalArgumentException.class, () -> fan.createSpawns(0, 0, 0, 0));
        assertThrows(IllegalArgumentException.class,
                () -> fan.createSpawns(0, 0, Double.POSITIVE_INFINITY, 1));
        assertThrows(IllegalArgumentException.class,
                () -> fan.createSpawns(Double.NaN, 0, 1, 0));
    }
}
