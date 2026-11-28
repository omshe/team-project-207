package data_access;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.TestTerrain;
import entity.TileMap;
import org.junit.jupiter.api.Test;

/** Checks that movement gets the very arena startup handed in. */
class InMemoryUpdateGameDataAccessObjectTest {
    @Test
    void returnsTheArenaItWasGivenEveryTime() {
        TileMap arena = TestTerrain.openGround(4, 3);
        InMemoryUpdateGameDataAccessObject dataAccess =
                new InMemoryUpdateGameDataAccessObject(arena);

        assertSame(arena, dataAccess.getCurrentMap());
        assertSame(arena, dataAccess.getCurrentMap());
    }

    @Test
    void rejectsNullArena() {
        assertThrows(IllegalArgumentException.class,
                () -> new InMemoryUpdateGameDataAccessObject(null));
    }
}
