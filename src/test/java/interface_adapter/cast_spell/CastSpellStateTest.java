package interface_adapter.cast_spell;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/** Checks the initial display state and snapshot immutability. */
class CastSpellStateTest {

    /** Before simulation there are no flight visuals or feedback. */
    @Test
    void initialStateIsEmpty() {
        CastSpellState state = CastSpellState.empty();

        assertEquals(0, state.getAnimationTick());
        assertEquals("", state.getStatusMessage());
        assertTrue(state.getProjectiles().isEmpty());
    }

    /** Later changes to the supplied list cannot change an existing snapshot. */
    @Test
    void projectileListIsCopiedAndReadOnly() {
        ProjectileDisplayData projectile =
                new ProjectileDisplayData("fireball", 10, 20, 4, 6, 0);
        List<ProjectileDisplayData> supplied = new ArrayList<>(List.of(projectile));
        CastSpellState state = new CastSpellState(7, "Cast Fireball.", supplied);

        supplied.clear();

        assertEquals(7, state.getAnimationTick());
        assertEquals("Cast Fireball.", state.getStatusMessage());
        assertEquals(1, state.getProjectiles().size());
        assertSame(projectile, state.getProjectiles().get(0));
        assertThrows(UnsupportedOperationException.class,
                () -> state.getProjectiles().clear());
    }

    /** Snapshots require a message and a list without null elements. */
    @Test
    void nullSnapshotValuesAreRejected() {
        assertThrows(NullPointerException.class,
                () -> new CastSpellState(0, null, List.of()));
        assertThrows(NullPointerException.class,
                () -> new CastSpellState(0, "", null));
        List<ProjectileDisplayData> projectiles = new ArrayList<>();
        projectiles.add(null);
        assertThrows(NullPointerException.class,
                () -> new CastSpellState(0, "", projectiles));
    }
}
