package entity;

/**
 * Decides whether a moving enemy may step into a new position.
 *
 * <p>{@link Enemy} chooses <em>where it would like to go</em> (straight at its target, or a
 * detour around something in the way). This interface answers <em>whether it may go there</em>.
 * The rules behind the answer, such as terrain, other enemies and the world's edges, belong to
 * the use case. The entity layer defines the interface and the use case implements it, so the
 * entity never depends on maps or waves (Dependency Inversion).
 */
@FunctionalInterface
public interface MovementSpace {
    /**
     * Returns whether an enemy currently at {@code current} may move to {@code next}.
     *
     * @param current the enemy's hitbox before the step
     * @param next    the hitbox it would have after the step
     * @return {@code true} if the step is allowed
     */
    boolean canOccupy(Hitbox current, Hitbox next);
}
