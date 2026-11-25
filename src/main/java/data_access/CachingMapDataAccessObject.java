package data_access;

import entity.TileMap;
import java.util.HashMap;
import java.util.Map;
import use_case.load_map.MapDataAccessInterface;
import use_case.load_map.MapLoadingException;

/**
 * Remembers maps that another map source has already loaded (Decorator pattern).
 *
 * <p>Startup needs the same map twice: once to draw it, and once so enemies can check which
 * tiles are walkable. Wrapping the real source in this class means the file is read only the
 * first time, and every later request for that name gets the very same {@link TileMap}. Both
 * users therefore always see identical terrain.
 *
 * <p>Only successful loads are remembered. A failed load is not, so asking again retries it.
 */
public final class CachingMapDataAccessObject implements MapDataAccessInterface {
    private final MapDataAccessInterface source;
    private final Map<String, TileMap> loadedMaps = new HashMap<>();

    /**
     * Wraps a map source.
     *
     * @param source where maps are loaded from the first time they are requested
     * @throws IllegalArgumentException if the source is null
     */
    public CachingMapDataAccessObject(MapDataAccessInterface source) {
        if (source == null) {
            throw new IllegalArgumentException("Map source must not be null.");
        }
        this.source = source;
    }

    /**
     * Returns the remembered map for this name, loading it from the source the first time.
     *
     * @param mapName non-blank name identifying the requested map
     * @return the loaded map; the same instance every time for the same name
     * @throws MapLoadingException if the source cannot load the map
     */
    @Override
    public TileMap load(String mapName) throws MapLoadingException {
        TileMap remembered = loadedMaps.get(mapName);
        if (remembered != null) {
            return remembered;
        }
        TileMap loaded = source.load(mapName);
        loadedMaps.put(mapName, loaded);
        return loaded;
    }
}
