package use_case.cast_spell;

import entity.Enemy;
import entity.Hitbox;
import entity.Projectile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Advances projectiles and reports the earliest enemy contact before world exit.
 *
 * <p>A projectile that reaches an enemy defeats it (a one-hit kill for the MVP demo) and
 * then disappears. Only the first enemy along its path is hit, and enemies already defeated
 * are ignored, so they never absorb a second projectile.
 */
public final class UpdateProjectilesInteractor
        implements UpdateProjectilesInputBoundary {

    private final UpdateProjectilesDataAccessInterface dataAccess;
    private final UpdateProjectilesOutputBoundary presenter;
    private final double worldWidth;
    private final double worldHeight;
    private long tick;

    /**
     * Creates a simulation using the same storage as projectile launching.
     *
     * @param dataAccess shared projectiles and enemies
     * @param presenter receives the result of each tick
     * @param worldWidth positive, finite world width in pixels
     * @param worldHeight positive, finite world height in pixels
     * @throws NullPointerException if a collaborator is null
     * @throws IllegalArgumentException if either world dimension is not positive and finite
     */
    public UpdateProjectilesInteractor(
            UpdateProjectilesDataAccessInterface dataAccess,
            UpdateProjectilesOutputBoundary presenter,
            double worldWidth,
            double worldHeight) {
        this.dataAccess = Objects.requireNonNull(dataAccess);
        this.presenter = Objects.requireNonNull(presenter);

        if (!Double.isFinite(worldWidth) || worldWidth <= 0
                || !Double.isFinite(worldHeight) || worldHeight <= 0) {
            throw new IllegalArgumentException(
                    "World dimensions must be positive and finite");
        }

        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        List<ProjectileOutputData> survivors = new ArrayList<>();
        List<ProjectileImpactOutputData> impacts = new ArrayList<>();
        List<Enemy> enemies = dataAccess.getEnemies();

        for (Projectile projectile : dataAccess.getProjectiles()) {
            Hitbox start = projectile.getHitbox();

            if (!start.isInsideWorld(worldWidth, worldHeight)) {
                dataAccess.removeProjectile(projectile);
                continue;
            }

            projectile.move();

            Hitbox end = projectile.getHitbox();
            double deltaX = end.getX() - start.getX();
            double deltaY = end.getY() - start.getY();

            double exitTime = Math.min(
                    axisExit(start.getX(), deltaX,
                            0, worldWidth - start.getWidth()),
                    axisExit(start.getY(), deltaY,
                            0, worldHeight - start.getHeight()));

            double contactTime = Double.POSITIVE_INFINITY;
            Enemy hitEnemy = null;

            for (Enemy enemy : enemies) {
                // An enemy defeated earlier this tick is no longer a target.
                if (enemy.isDefeated()) {
                    continue;
                }
                double enemyContactTime =
                        firstContact(start, deltaX, deltaY, enemy.getHitbox());
                if (enemyContactTime < contactTime) {
                    contactTime = enemyContactTime;
                    hitEnemy = enemy;
                }
            }

            if (hitEnemy != null && contactTime <= 1 && contactTime <= exitTime) {
                dataAccess.removeProjectile(projectile);
                hitEnemy.defeat();

                impacts.add(new ProjectileImpactOutputData(
                        projectile.getSpell().getId(),
                        start.getCenterX() + deltaX * contactTime,
                        start.getCenterY() + deltaY * contactTime));
                continue;
            }

            if (!end.isInsideWorld(worldWidth, worldHeight)) {
                dataAccess.removeProjectile(projectile);
                continue;
            }

            survivors.add(new ProjectileOutputData(
                    projectile.getSpell().getId(),
                    end.getX(),
                    end.getY(),
                    end.getWidth(),
                    end.getHeight(),
                    deltaX,
                    deltaY));
        }

        tick++;
        presenter.present(new UpdateProjectilesOutputData(
                tick, survivors, impacts));
    }

    /**
     * Returns first contact in the interval [0, 1], or infinity when the path misses.
     */
    private static double firstContact(
            Hitbox start, double deltaX, double deltaY, Hitbox enemy) {
        double tolerance = Hitbox.CONTACT_TOLERANCE;

        // Expand the enemy by the projectile's size, then follow the projectile's
        // top-left corner as a point along this tick's segment.
        double left = enemy.getX() - start.getWidth() - tolerance;
        double right = enemy.getRight() + tolerance;
        double top = enemy.getY() - start.getHeight() - tolerance;
        double bottom = enemy.getBottom() + tolerance;

        double entry = Math.max(0, Math.max(
                axisEntry(start.getX(), deltaX, left, right),
                axisEntry(start.getY(), deltaY, top, bottom)));

        double exit = Math.min(1, Math.min(
                axisExit(start.getX(), deltaX, left, right),
                axisExit(start.getY(), deltaY, top, bottom)));

        if (entry <= exit) {
            return entry;
        }

        return Double.POSITIVE_INFINITY;
    }

    /**
     * Returns the first time a moving point enters an axis interval.
     */
    private static double axisEntry(
            double position, double movement, double lower, double upper) {
        if (movement == 0) {
            if (position < lower || position > upper) {
                return Double.POSITIVE_INFINITY;
            }
            return Double.NEGATIVE_INFINITY;
        }

        return Math.min(
                (lower - position) / movement,
                (upper - position) / movement);
    }

    /**
     * Returns the last time a moving point remains in an axis interval.
     */
    private static double axisExit(
            double position, double movement, double lower, double upper) {
        if (movement == 0) {
            return Double.POSITIVE_INFINITY;
        }

        return Math.max(
                (lower - position) / movement,
                (upper - position) / movement);
    }
}