package use_case.generate_mob_waves;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Hitbox;
import java.util.List;
import java.util.Random;
import org.junit.jupiter.api.Test;

/** Tests the random outskirts spawn strategy with seeded randomness, so runs repeat exactly. */
class RandomEdgeSpawnPositionStrategyTest {
    private static final Hitbox CENTRED_PLAYER = new Hitbox(456, 296, 48, 48);

    private static List<Hitbox> propose(long seed, Hitbox player) {
        return new RandomEdgeSpawnPositionStrategy(new Random(seed), 16, 64, 192, 200)
                .proposeCandidates(56, 34, 960, 640, player);
    }

    /** Distance from the box to the nearest world edge. */
    private static double distanceToNearestEdge(Hitbox box) {
        return Math.min(Math.min(box.getX(), 960 - box.getRight()),
                Math.min(box.getY(), 640 - box.getBottom()));
    }

    @Test
    void everySpotIsInsideTheOutskirtsBandAndAwayFromThePlayer() {
        for (long seed = 0; seed < 50; seed++) {
            for (Hitbox spot : propose(seed, CENTRED_PLAYER)) {
                assertTrue(spot.isInsideWorld(960, 640));
                assertEquals(56, spot.getWidth(), 0);
                assertEquals(34, spot.getHeight(), 0);
                double fromEdge = distanceToNearestEdge(spot);
                assertTrue(fromEdge >= 16 - 1e-9 && fromEdge <= 80 + 1e-9,
                        "not on the outskirts: " + fromEdge);
                double deltaX = spot.getCenterX() - CENTRED_PLAYER.getCenterX();
                double deltaY = spot.getCenterY() - CENTRED_PLAYER.getCenterY();
                assertTrue(Math.sqrt(deltaX * deltaX + deltaY * deltaY) >= 192);
            }
        }
    }

    @Test
    void spotsAreNotPredeterminedButRepeatForTheSameSeed() {
        List<Hitbox> first = propose(1, CENTRED_PLAYER);
        List<Hitbox> again = propose(1, CENTRED_PLAYER);
        List<Hitbox> other = propose(2, CENTRED_PLAYER);
        assertEquals(first.get(0).getX(), again.get(0).getX(), 0);
        assertEquals(first.get(0).getY(), again.get(0).getY(), 0);
        assertNotEquals(first.get(0).getX(), other.get(0).getX());
        // One generator gives different spots on each request (each new wave).
        RandomEdgeSpawnPositionStrategy strategy =
                new RandomEdgeSpawnPositionStrategy(new Random(3), 16, 64, 192, 200);
        Hitbox waveOne = strategy.proposeCandidates(56, 34, 960, 640, CENTRED_PLAYER).get(0);
        Hitbox waveTwo = strategy.proposeCandidates(56, 34, 960, 640, CENTRED_PLAYER).get(0);
        assertNotEquals(waveOne.getX() + waveOne.getY(), waveTwo.getX() + waveTwo.getY());
    }

    @Test
    void spotsSpreadOverAllFourEdges() {
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        for (Hitbox spot : propose(4, CENTRED_PLAYER)) {
            double fromEdge = distanceToNearestEdge(spot);
            if (spot.getY() == fromEdge) {
                top++;
            } else if (640 - spot.getBottom() == fromEdge) {
                bottom++;
            } else if (spot.getX() == fromEdge) {
                left++;
            } else {
                right++;
            }
        }
        assertTrue(top > 0 && bottom > 0 && left > 0 && right > 0,
                top + " " + bottom + " " + left + " " + right);
    }

    @Test
    void playerNearAnEdgeGetsNoSpotsBesideThem() {
        Hitbox playerAtLeftEdge = new Hitbox(0, 296, 48, 48);
        for (Hitbox spot : propose(5, playerAtLeftEdge)) {
            double deltaX = spot.getCenterX() - playerAtLeftEdge.getCenterX();
            double deltaY = spot.getCenterY() - playerAtLeftEdge.getCenterY();
            assertTrue(Math.sqrt(deltaX * deltaX + deltaY * deltaY) >= 192);
        }
    }

    @Test
    void rejectsOutOfRangeSettings() {
        Random random = new Random(0);
        assertThrows(IllegalArgumentException.class,
                () -> new RandomEdgeSpawnPositionStrategy(random, -1, 64, 192, 200));
        assertThrows(IllegalArgumentException.class,
                () -> new RandomEdgeSpawnPositionStrategy(random, 16, -1, 192, 200));
        assertThrows(IllegalArgumentException.class,
                () -> new RandomEdgeSpawnPositionStrategy(random, 16, 64, -1, 200));
        assertThrows(IllegalArgumentException.class,
                () -> new RandomEdgeSpawnPositionStrategy(random, 16, 64, 192, 0));
    }
}
