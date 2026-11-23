package entity;

/**
 * A basic enemy (mob) that chases a target and stops when it reaches it.
 *
 * <p>An enemy has a position (top-left corner of its hitbox, in pixels), a fixed hitbox size
 * and a speed in <b>pixels per tick</b>. It knows nothing about sprites, the screen or input;
 * it only records whether it moved on its last tick and which horizontal way it last moved, so
 * outer layers can choose an animation.
 *
 * <p><b>Pursuit contract</b> ({@link #pursue(Hitbox, MovementSpace)}): each call moves the
 * enemy at most {@code speed} pixels toward the target's centre. The step length is the same
 * in every direction, so diagonal chasing is not faster. The enemy never moves past the point
 * where its hitbox first touches the target (see {@link Hitbox#touches(Hitbox)}), so it cannot
 * overshoot even when the remaining gap is smaller than one step. While touching, it stays
 * still; once the target moves away, the next call resumes the chase.
 *
 * <p><b>Going around obstacles (wall following).</b> Every step must be allowed by the given
 * {@link MovementSpace}. If the straight step is not allowed, the enemy starts <em>following
 * the obstacle</em>, like walking along a wall with one hand on it:
 * <ol>
 *   <li>Before that, a blocked step is not wasted: the enemy moves as far along it as is
 *       allowed, so it ends up flush against the obstacle. Being flush is what lets it tell
 *       a real corner (the wall has ended) from a small gap.</li>
 *   <li>It picks a walking direction along one world axis, perpendicular to its main heading
 *       and on the side of the target where possible, and remembers on which side the
 *       obstacle (the "wall") is. Obstacles (tiles and hitboxes) are axis-aligned, so walking
 *       along the axes slides cleanly along their edges.</li>
 *   <li>While following, it turns toward the wall when the wall ends (going round an outside
 *       corner), keeps walking while the wall continues, and turns away from the wall when
 *       something blocks the way ahead (an inside corner). The wall only counts as ended when
 *       there is room for two whole steps toward it; a smaller gap is closed instead. Without
 *       that margin, a gap left over from the last step looks like a corner and the enemy
 *       turns in circles.</li>
 *   <li>It stops following and heads straight for the target again only when the straight
 *       step is free <em>and</em> either it is now closer to the target than when it got
 *       blocked, or the target is no longer on the wall's side. This rule is what stops it
 *       from stepping back into the same corner over and over.</li>
 * </ol>
 * If it cannot move in any of these ways, it stops following and waits this tick. This is a
 * simple local rule, not full pathfinding: routes can be longer than the shortest one, an
 * enemy may circle an obstacle when the target is unreachable, and a dead end can still trap
 * it.
 */
public class Enemy {
    /** Halvings used to find the longest allowed part of a blocked step. */
    private static final int STEP_SEARCH_ROUNDS = 12;
    /** Shorter partial steps than this, in pixels, count as not moving. */
    private static final double MIN_PARTIAL_STEP = 1e-3;

    private double left;
    private double top;
    private final double width;
    private final double height;
    private final double speed;
    private boolean moving;
    private boolean facingLeft;
    /** Which way to start following when the target gives no hint: +1 or -1 along the axis. */
    private int preferredSide = 1;
    /** Whether the enemy is currently following an obstacle's edge. */
    private boolean followingObstacle;
    /** The world-axis direction (-1, 0 or 1 on each axis) it walks while following. */
    private int walkX;
    private int walkY;
    /** The world-axis direction (-1, 0 or 1 on each axis) of the obstacle while following. */
    private int wallX;
    private int wallY;
    /** Distance to the target's centre when it started following. */
    private double distanceWhenBlocked;

    /**
     * Creates an enemy that starts still and facing right.
     *
     * @param left starting left edge, in pixels
     * @param top starting top edge, in pixels
     * @param width hitbox width, in pixels; must be positive
     * @param height hitbox height, in pixels; must be positive
     * @param speed largest distance moved per tick, in pixels; must not be negative
     * @throws IllegalArgumentException if the size is not positive or the speed is negative
     */
    public Enemy(double left, double top, double width, double height, double speed) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Enemy size must be positive, got " + width + " x " + height);
        }
        if (speed < 0) {
            throw new IllegalArgumentException("Enemy speed must not be negative, got " + speed);
        }
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
        this.speed = speed;
    }

    /**
     * Moves one tick toward the target with nothing in the way.
     *
     * <p>Same as {@link #pursue(Hitbox, MovementSpace)} with a space that allows every step.
     *
     * @param target the hitbox to chase, normally the player's, at its position for this tick
     */
    public void pursue(Hitbox target) {
        pursue(target, (current, next) -> true);
    }

    /**
     * Moves one tick toward the target, going around anything the space forbids (see the class
     * contract).
     *
     * <p>Afterwards {@link #isMoving()} reports whether the enemy actually moved this tick, and
     * {@link #isFacingLeft()} reports the horizontal direction of its latest movement.
     *
     * @param target the hitbox to chase, normally the player's, at its position for this tick
     * @param space  decides which steps are allowed
     */
    public void pursue(Hitbox target, MovementSpace space) {
        moving = false;
        Hitbox current = getHitbox();
        double deltaX = target.getCenterX() - current.getCenterX();
        double deltaY = target.getCenterY() - current.getCenterY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        // Already in contact: stay put. Zero distance (coincident centres) is checked
        // explicitly so the division below can never divide by zero.
        if (distance == 0 || current.touches(target)) {
            return;
        }

        // Unit vector from this enemy's centre to the target's centre.
        double directionX = deltaX / distance;
        double directionY = deltaY / distance;

        // 1. The straight step, shortened so it stops exactly at first contact.
        double straightStep = Math.min(speed, distanceUntilContact(current, target,
                directionX, directionY));
        if (straightStep <= 0) {
            return;
        }

        // 2. Already going round an obstacle: keep following it until it is safe to leave.
        if (followingObstacle) {
            followObstacle(current, directionX, directionY, straightStep, distance, target,
                    space);
            return;
        }

        // 3. Free to go straight (a full step means the way is clear).
        if (tryStep(current, directionX, directionY, straightStep, target, space)) {
            return;
        }

        // A blocked straight step first closes the gap to whatever is in the way.
        if (tryPartialStep(current, directionX, directionY, straightStep, target, space)) {
            return;
        }

        // 4. Blocked: start walking around whatever is in the way.
        startFollowing(current, directionX, directionY, distance, target, space);
    }

    /**
     * Starts following an obstacle: walks perpendicular to the main heading, preferring the
     * side the target is on, and remembers that the obstacle lies in the main heading's
     * direction. If neither side is free, it tries moving along the main heading's axis alone,
     * without starting to follow.
     */
    private void startFollowing(Hitbox current, double directionX, double directionY,
                                double distance, Hitbox target, MovementSpace space) {
        boolean headingSideways = Math.abs(directionX) >= Math.abs(directionY);
        int headingX = 0;
        int headingY = 0;
        double minorComponent;
        if (headingSideways) {
            headingX = (int) Math.signum(directionX);
            minorComponent = directionY;
        } else {
            headingY = (int) Math.signum(directionY);
            minorComponent = directionX;
        }

        // Go round on the side the target is on; with no hint, use the remembered side.
        int firstSide = preferredSide;
        if (minorComponent > 0) {
            firstSide = 1;
        } else if (minorComponent < 0) {
            firstSide = -1;
        }

        int[] sides = {firstSide, -firstSide};
        for (int side : sides) {
            // The walking direction is perpendicular to the heading: swap the axes.
            int stepX = Math.abs(headingY) * side;
            int stepY = Math.abs(headingX) * side;
            if (tryStep(current, stepX, stepY, speed, target, space)) {
                preferredSide = side;
                followingObstacle = true;
                walkX = stepX;
                walkY = stepY;
                wallX = headingX;
                wallY = headingY;
                distanceWhenBlocked = distance;
                return;
            }
        }

        // Nothing sideways: try the main heading's axis on its own (a partial straight step).
        double headingComponent = Math.abs(directionX) * Math.abs(headingX)
                + Math.abs(directionY) * Math.abs(headingY);
        tryStep(current, headingX, headingY, speed * headingComponent, target, space);
    }

    /**
     * One tick of following an obstacle's edge (see the class contract for the rules).
     */
    private void followObstacle(Hitbox current, double directionX, double directionY,
                                double straightStep, double distance, Hitbox target,
                                MovementSpace space) {
        // Leave only when going straight cannot lead straight back into the same corner.
        boolean closerThanWhenBlocked =
                distance < distanceWhenBlocked - Hitbox.CONTACT_TOLERANCE;
        boolean targetAwayFromWall = directionX * wallX + directionY * wallY <= 0;
        if ((closerThanWhenBlocked || targetAwayFromWall)
                && tryStep(current, directionX, directionY, straightStep, target, space)) {
            followingObstacle = false;
            return;
        }

        // Outside corner: the wall has really ended (there is room for two whole steps toward
        // it, more than any leftover gap), so turn toward where it was.
        if (canStep(current, wallX, wallY, 2 * speed, target, space)
                && tryStep(current, wallX, wallY, speed, target, space)) {
            final int oldWalkX = walkX;
            final int oldWalkY = walkY;
            walkX = wallX;
            walkY = wallY;
            wallX = -oldWalkX;
            wallY = -oldWalkY;
            return;
        }

        // A small gap to the wall: close it first so the corner check above stays reliable.
        if (tryPartialStep(current, wallX, wallY, speed, target, space)) {
            return;
        }

        // The wall continues: keep walking along it, up to anything ahead.
        if (tryStep(current, walkX, walkY, speed, target, space)
                || tryPartialStep(current, walkX, walkY, speed, target, space)) {
            return;
        }

        // Inside corner: something blocks the way ahead, so turn away from the wall.
        if (tryStep(current, -wallX, -wallY, speed, target, space)
                || tryPartialStep(current, -wallX, -wallY, speed, target, space)) {
            final int oldWalkX = walkX;
            final int oldWalkY = walkY;
            walkX = -wallX;
            walkY = -wallY;
            wallX = oldWalkX;
            wallY = oldWalkY;
            return;
        }

        // Boxed in: give up following; the next tick starts afresh.
        followingObstacle = false;
    }

    /**
     * Returns whether a step would be allowed, without moving.
     */
    private boolean canStep(Hitbox current, double directionX, double directionY,
                            double length, Hitbox target, MovementSpace space) {
        Hitbox next = new Hitbox(left + directionX * length, top + directionY * length,
                width, height);
        return !next.overlaps(target) && space.canOccupy(current, next);
    }

    /**
     * Moves the longest allowed part of a blocked step, found by repeatedly halving the
     * interval between "allowed" and "blocked". This brings the enemy flush against an
     * obstacle instead of stopping up to a whole step short of it.
     *
     * @return {@code true} if the enemy moved at least {@link #MIN_PARTIAL_STEP}
     */
    private boolean tryPartialStep(Hitbox current, double directionX, double directionY,
                                   double length, Hitbox target, MovementSpace space) {
        double allowed = 0;
        double blocked = length;
        for (int round = 0; round < STEP_SEARCH_ROUNDS; round++) {
            double middle = (allowed + blocked) / 2;
            if (canStep(current, directionX, directionY, middle, target, space)) {
                allowed = middle;
            } else {
                blocked = middle;
            }
        }
        if (allowed < MIN_PARTIAL_STEP) {
            return false;
        }
        return tryStep(current, directionX, directionY, allowed, target, space);
    }

    /**
     * Moves by {@code length} along the given unit direction if that step neither overlaps the
     * target nor is forbidden by the space. Callers always pass a non-zero direction and a
     * positive length.
     *
     * @return {@code true} if the enemy moved
     */
    private boolean tryStep(Hitbox current, double directionX, double directionY,
                            double length, Hitbox target, MovementSpace space) {
        Hitbox next = new Hitbox(left + directionX * length, top + directionY * length,
                width, height);
        if (next.overlaps(target) || !space.canOccupy(current, next)) {
            return false;
        }
        left = next.getX();
        top = next.getY();
        moving = true;
        updateFacing(directionX);
        return true;
    }

    /**
     * Returns how far this enemy can travel along the direction before first touching the
     * target.
     *
     * <p>Moving toward the target's centre shrinks the gap on each axis at a constant rate
     * (the absolute value of that axis' direction component). The boxes touch once
     * <em>both</em> gaps reach zero, so contact happens at the later of the two per-axis times.
     * An axis whose gap is already closed contributes zero.
     */
    private static double distanceUntilContact(Hitbox current, Hitbox target,
                                               double directionX, double directionY) {
        double gapX = current.horizontalGapTo(target);
        double gapY = current.verticalGapTo(target);

        double distanceToCloseX = 0;
        if (gapX > 0) {
            // A positive gap means the centres differ on this axis, so directionX is not zero.
            distanceToCloseX = gapX / Math.abs(directionX);
        }
        double distanceToCloseY = 0;
        if (gapY > 0) {
            distanceToCloseY = gapY / Math.abs(directionY);
        }
        return Math.max(distanceToCloseX, distanceToCloseY);
    }

    /** Remembers the horizontal direction of the latest movement; purely vertical moves keep it. */
    private void updateFacing(double directionX) {
        if (directionX < 0) {
            facingLeft = true;
        } else if (directionX > 0) {
            facingLeft = false;
        }
    }

    /**
     * Returns this enemy's current hitbox.
     *
     * @return a new immutable hitbox at the current position
     */
    public Hitbox getHitbox() {
        return new Hitbox(left, top, width, height);
    }

    /**
     * Returns the left edge.
     *
     * @return left edge, in pixels
     */
    public double getX() {
        return left;
    }

    /**
     * Returns the top edge.
     *
     * @return top edge, in pixels
     */
    public double getY() {
        return top;
    }

    /**
     * Returns the hitbox width.
     *
     * @return width, in pixels
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the hitbox height.
     *
     * @return height, in pixels
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the speed.
     *
     * @return largest distance moved per tick, in pixels
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Returns whether the enemy moved during its latest pursue call.
     *
     * @return {@code true} if it moved; {@code false} if it was touching the target, was
     *     blocked in every direction, or has not pursued yet
     */
    public boolean isMoving() {
        return moving;
    }

    /**
     * Returns whether the enemy's latest horizontal movement was to the left.
     *
     * @return {@code true} if facing left; {@code false} if facing right (the initial facing)
     */
    public boolean isFacingLeft() {
        return facingLeft;
    }
}
