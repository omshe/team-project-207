package use_case.generate_mob_waves;

import entity.Hitbox;
import java.util.ArrayList;
import java.util.List;

/**
 * Proposes deterministic spawn spots just inside the world's edges.
 *
 * <p>Candidates come in this order: the middles of the left, right, top and bottom edges,
 * then the top-left, top-right, bottom-left and bottom-right corners, then more spots spread
 * along all four edges for bigger waves. With three enemies, the mobs appear at the left,
 * right and top edges, far from a player who starts in the centre. Because the spots are fixed,
 * tests and demos are repeatable.
 *
 * <p>The extra edge spots are one enemy size plus one margin apart, starting from the
 * corners. Some of them overlap the first eight; the interactor skips any candidate that
 * would touch or crowd a spot it has already chosen.
 */
public class EdgeSpawnPositionStrategy implements SpawnPositionStrategy {
    private final double edgeMargin;

    /**
     * Creates the strategy.
     *
     * @param edgeMargin distance between a candidate and the nearest world edge, in pixels;
     *                   must not be negative
     * @throws IllegalArgumentException if {@code edgeMargin} is negative
     */
    public EdgeSpawnPositionStrategy(double edgeMargin) {
        if (edgeMargin < 0) {
            throw new IllegalArgumentException("Edge margin must not be negative, got "
                    + edgeMargin);
        }
        this.edgeMargin = edgeMargin;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Always returns the same candidates for the same sizes, starting with the same eight
     * as before; the player's position is not used.
     */
    @Override
    public List<Hitbox> proposeCandidates(double enemyWidth, double enemyHeight,
                                          double worldWidth, double worldHeight,
                                          Hitbox playerHitbox) {
        double left = edgeMargin;
        double right = worldWidth - edgeMargin - enemyWidth;
        double top = edgeMargin;
        double bottom = worldHeight - edgeMargin - enemyHeight;
        double middleX = (worldWidth - enemyWidth) / 2;
        double middleY = (worldHeight - enemyHeight) / 2;

        List<Hitbox> candidates = new ArrayList<>();
        // Edge middles first: they are the farthest spots from a centred player.
        candidates.add(new Hitbox(left, middleY, enemyWidth, enemyHeight));
        candidates.add(new Hitbox(right, middleY, enemyWidth, enemyHeight));
        candidates.add(new Hitbox(middleX, top, enemyWidth, enemyHeight));
        candidates.add(new Hitbox(middleX, bottom, enemyWidth, enemyHeight));
        // Corners as fall-backs.
        candidates.add(new Hitbox(left, top, enemyWidth, enemyHeight));
        candidates.add(new Hitbox(right, top, enemyWidth, enemyHeight));
        candidates.add(new Hitbox(left, bottom, enemyWidth, enemyHeight));
        candidates.add(new Hitbox(right, bottom, enemyWidth, enemyHeight));
        // More spots along every edge, for waves bigger than eight.
        addEdgeRows(candidates, enemyWidth, enemyHeight, left, right, top, bottom);
        return candidates;
    }

    /**
     * Adds evenly spaced spots along the top and bottom edges, then along the left and right
     * edges, alternating between the two opposite edges so a growing wave stays spread out.
     */
    private void addEdgeRows(List<Hitbox> candidates, double enemyWidth, double enemyHeight,
                             double left, double right, double top, double bottom) {
        double horizontalStep = enemyWidth + edgeMargin;
        for (double spotX = left; spotX <= right; spotX += horizontalStep) {
            candidates.add(new Hitbox(spotX, top, enemyWidth, enemyHeight));
            candidates.add(new Hitbox(spotX, bottom, enemyWidth, enemyHeight));
        }
        double verticalStep = enemyHeight + edgeMargin;
        for (double spotY = top; spotY <= bottom; spotY += verticalStep) {
            candidates.add(new Hitbox(left, spotY, enemyWidth, enemyHeight));
            candidates.add(new Hitbox(right, spotY, enemyWidth, enemyHeight));
        }
    }
}
