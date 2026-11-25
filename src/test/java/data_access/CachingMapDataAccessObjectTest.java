package data_access;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import entity.DirtTileType;
import entity.TileMap;
import entity.TileType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import use_case.load_map.MapDataAccessInterface;
import use_case.load_map.MapLoadingException;

/** Checks that the caching map source reads each map once and never remembers failures. */
class CachingMapDataAccessObjectTest {

    /** A map source that records every request and fails while {@code failing} is set. */
    private static final class CountingMapSource implements MapDataAccessInterface {
        private final List<String> requests = new ArrayList<>();
        private boolean failing;

        @Override
        public TileMap load(String mapName) throws MapLoadingException {
            requests.add(mapName);
            if (failing) {
                throw new MapLoadingException("Map resource not found: maps/" + mapName);
            }
            TileType dirt = new DirtTileType();
            return new TileMap(new TileType[][] {{dirt}}, 32);
        }
    }

    @Test
    void readsEachMapOnceAndReturnsTheSameInstanceAfterwards() throws MapLoadingException {
        CountingMapSource source = new CountingMapSource();
        CachingMapDataAccessObject maps = new CachingMapDataAccessObject(source);

        TileMap first = maps.load("level1.txt");
        TileMap second = maps.load("level1.txt");

        assertSame(first, second);
        assertEquals(List.of("level1.txt"), source.requests);
    }

    @Test
    void loadsDifferentMapNamesSeparately() throws MapLoadingException {
        CountingMapSource source = new CountingMapSource();
        CachingMapDataAccessObject maps = new CachingMapDataAccessObject(source);

        TileMap level1 = maps.load("level1.txt");
        TileMap level2 = maps.load("level2.txt");

        assertNotSame(level1, level2);
        assertEquals(List.of("level1.txt", "level2.txt"), source.requests);
    }

    @Test
    void passesFailuresOnAndRetriesOnTheNextRequest() throws MapLoadingException {
        CountingMapSource source = new CountingMapSource();
        CachingMapDataAccessObject maps = new CachingMapDataAccessObject(source);
        source.failing = true;

        MapLoadingException error = assertThrows(MapLoadingException.class,
                () -> maps.load("missing.txt"));
        source.failing = false;
        maps.load("missing.txt");

        assertEquals("Map resource not found: maps/missing.txt", error.getMessage());
        assertEquals(List.of("missing.txt", "missing.txt"), source.requests);
    }

    @Test
    void rejectsMissingSource() {
        assertThrows(IllegalArgumentException.class, () -> new CachingMapDataAccessObject(null));
    }
}
