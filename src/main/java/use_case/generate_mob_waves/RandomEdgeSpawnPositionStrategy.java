package use_case.generate_mob_waves;

import entity.Hitbox;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Proposes random spawn spots on the outskirts of the world: in a band just inside its edges,
 * and not too close to the player.
 *
 * <p>Each candidate is made in three steps:
 * <ol>
 *   <li>Pick an edge, with longer edges picked more often, so spots spread evenly around the
 *       whole border.</li>
 *   <li>Pick a random position along that edge.</li>
 *   <li>Pick a random distance in from the edge, between {@code edgeMargin} and
 *       {@code edgeMargin + bandDepth}.</li>
 * </ol>
 * Candidates whose centre is closer than {@code minPlayerDistance} to the player's centre are
 * dropped, so a wave never appears right beside a player standing near an edge.
 *
 * <p>This strategy only decides <em>where to look</em>. The interactor still keeps only spots
 * that are inside the world, on walkable ground, not touching the player and spaced apart from
 * each other. If too few candidates pass, the wave waits and the next tick asks again, with
 * fresh random spots.
 *
 * <p>The random number generator is passed in, so a test can use a seeded one and get the same
 * spots every run.
 */
public class RandomEdgeSpawnPositionStrategy implements SpawnPositionStrategy {
    private final Random random;
    private final double edgeMargin;
    private final double bandDepth;
    private final double minPlayerDistance;
    private final int candidateCount;

    /**
     * Creates the strategy.
     *
     * @param random            where the randomness comes from
     * @param edgeMargin        smallest distance between a spot and its edge, in pixels; must
     *                          not be negative
     * @param bandDepth         how much further in than {@code edgeMargin} a spot may be, in
     *                          pixels; must not be negative
     * @param minPlayerDistance smallest distance between a spot's centre and the player's
     *                          centre, in pixels; must not be negative
     * @param candidateCount    how many spots to try per request; must be at least 1
     * @throws IllegalArgumentException if a value is out of range
     */
    public RandomEdgeSpawnPositionStrategy(Random random, double edgeMargin, double bandDepth,
                                           double minPlayerDistance, int candidateCount) {
        if (edgeMargin < 0 || bandDepth < 0 || minPlayerDistance < 0) {
            throw new IllegalArgumentException("Margin, band depth and player distance must "
                    + "not be negative");
        }
        if (candidateCount < 1) {
            throw new IllegalArgumentException("Need at least one candidate, got "
                    + candidateCount);
        }
        this.random = random;
        this.edgeMargin = edgeMargin;
        this.bandDepth = bandDepth;
        this.minPlayerDistance = minPlayerDistance;
        this.candidateCount = candidateCount;
    }

    /**
     * {@inheritDoc}
     *
     * <p>Returns up to {@code candidateCount} random spots in random order; spots too close to
     * the player are left out. Every spot lies inside the world when the world is big enough to
     * hold an enemy at the edge margin.
     */
    @Override
    public List<Hitbox> proposeCandidates(double enemyWidth, double enemyHeight,
                                          double worldWidth, double worldHeight,
                                          Hitbox playerHitbox) {
        List<Hitbox> candidates = new ArrayList<>();
        for (int attempt = 0; attempt < candidateCount; attempt++) {
            Hitbox spot = randomSpotNearAnEdge(enemyWidth, enemyHeight, worldWidth, worldHeight);
            if (isFarEnoughFromPlayer(spot, playerHitbox)) {
                candidates.add(spot);
            }
        }
        return candidates;
    }

    /** Makes one random spot in the band along a randomly chosen edge. */
    private Hitbox randomSpotNearAnEdge(double enemyWidth, double enemyHeight,
                                        double worldWidth, double worldHeight) {
        // Ranges for the spot's top-left corner that keep the whole spot inside the margin.
        double lowestLeft = edgeMargin;
        double highestLeft = Math.max(lowestLeft, worldWidth - edgeMargin - enemyWidth);
        double lowestTop = edgeMargin;
        double highestTop = Math.max(lowestTop, worldHeight - edgeMargin - enemyHeight);
        double inset = edgeMargin + random.nextDouble() * bandDepth;

        // Pick a point along the whole border, so each edge is chosen in proportion to its length.
        double horizontalEdge = highestLeft - lowestLeft;
        double verticalEdge = highestTop - lowestTop;
        double borderPoint = random.nextDouble() * 2 * (horizontalEdge + verticalEdge);

        double spotLeft;
        double spotTop;
        if (borderPoint < horizontalEdge) {
            // Top edge.
            spotLeft = lowestLeft + borderPoint;
            spotTop = inset;
        } else if (borderPoint < 2 * horizontalEdge) {
            // Bottom edge.
            spotLeft = lowestLeft + borderPoint - horizontalEdge;
            spotTop = worldHeight - inset - enemyHeight;
        } else if (borderPoint < 2 * horizontalEdge + verticalEdge) {
            // Left edge.
            spotLeft = inset;
            spotTop = lowestTop + borderPoint - 2 * horizontalEdge;
        } else {
            // Right edge.
            spotLeft = worldWidth - inset - enemyWidth;
            spotTop = lowestTop + borderPoint - 2 * horizontalEdge - verticalEdge;
        }
        return new Hitbox(clamp(spotLeft, lowestLeft, highestLeft),
                clamp(spotTop, lowestTop, highestTop), enemyWidth, enemyHeight);
    }

    private boolean isFarEnoughFromPlayer(Hitbox spot, Hitbox playerHitbox) {
        double deltaX = spot.getCenterX() - playerHitbox.getCenterX();
        double deltaY = spot.getCenterY() - playerHitbox.getCenterY();
        return Math.sqrt(deltaX * deltaX + deltaY * deltaY) >= minPlayerDistance;
    }

    private static double clamp(double value, double lowest, double highest) {
        return Math.max(lowest, Math.min(highest, value));
    }
}
