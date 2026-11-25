package use_case.generate_mob_waves;

import entity.Hitbox;
import java.util.List;

/**
 * Proposes where a wave's enemies could be placed (Strategy pattern).
 *
 * <p>The interactor asks a strategy for candidate spots and then applies the placement rules
 * itself: a candidate is used only if it is fully inside the world, on walkable ground, does
 * not touch the player, and is spaced apart from the enemies already placed. A strategy
 * therefore only decides <em>where to look</em>, never <em>what counts as valid</em>. That
 * keeps the rules in one place while letting new placement styles (random spots, spawn points
 * read from a map, spots around the player in a larger scrolling world) be added as new
 * classes without editing the interactor.
 *
 * <p>Contract: the returned list is finite, so the interactor's search always ends. It may
 * contain invalid or overlapping candidates; those are simply skipped. It may be empty.
 */
public interface SpawnPositionStrategy {
    /**
     * Proposes candidate hitboxes for new enemies, in order of preference.
     *
     * @param enemyWidth   the width of each enemy, in pixels
     * @param enemyHeight  the height of each enemy, in pixels
     * @param worldWidth   the world's width, in pixels
     * @param worldHeight  the world's height, in pixels
     * @param playerHitbox where the player is now (a strategy may use it, for example, to
     *                     spawn around the player)
     * @return a finite list of candidate hitboxes, each {@code enemyWidth x enemyHeight}
     */
    List<Hitbox> proposeCandidates(double enemyWidth, double enemyHeight,
                                   double worldWidth, double worldHeight,
                                   Hitbox playerHitbox);
}
