package interface_adapter.map;

import use_case.load_map.LoadMapInputBoundary;
import use_case.load_map.LoadMapInputData;

/** Wraps a map name in a request and passes it to the load-map input boundary. */
public final class MapController {
    private final LoadMapInputBoundary loadMap;

    /**
     * Connects map requests to the use case.
     *
     * @param loadMap input boundary that handles map-loading requests
     */
    public MapController(LoadMapInputBoundary loadMap) {
        this.loadMap = loadMap;
    }

    /**
     * Requests a map; success or failure is reported through the use case's output boundary.
     *
     * @param mapName requested map name; null or blank names are handled by the use case
     */
    public void load(String mapName) {
        loadMap.execute(new LoadMapInputData(mapName));
    }
}
