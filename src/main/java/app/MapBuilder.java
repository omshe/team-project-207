package app;

import data_access.FileMapDataAccessObject;
import entity.DirtTileType;
import entity.TileTypeRegistry;
import entity.WaterTileType;
import interface_adapter.map.MapController;
import interface_adapter.map.MapPresenter;
import interface_adapter.map.MapViewModel;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import use_case.load_map.LoadMapInteractor;
import use_case.load_map.MapDataAccessInterface;
import view.MapLayer;
import view.MapRenderer;
import view.TileSprites;

/** Assembles and prepares the map display before the application starts its game loop. */
public final class MapBuilder {
    /**
     * Returns the terrain types every map in this game is built from. Startup code that loads
     * maps uses this one definition, so all map readers agree on what each tile ID means.
     *
     * @return a registry of water and dirt
     */
    public static TileTypeRegistry standardTileTypes() {
        return new TileTypeRegistry(List.of(new WaterTileType(), new DirtTileType()));
    }

    /**
     * Loads the map from its classpath file and prepares it to draw; see
     * {@link #build(MapDataAccessInterface, String, int, Map, Consumer)}.
     *
     * @param mapName name of the map resource to load
     * @param tileSize positive width and height of each tile in pixels
     * @param imagePaths non-null mapping of terrain IDs to absolute classpath image paths
     * @param showError non-null callback that displays a startup failure
     * @return the prepared layer, or null if map loading or image preparation fails
     */
    public MapLayer build(String mapName, int tileSize, Map<Integer, String> imagePaths,
                          Consumer<String> showError) {
        return build(new FileMapDataAccessObject(standardTileTypes(), tileSize), mapName,
                tileSize, imagePaths, showError);
    }

    /**
     * Loads the map through its controller and prepares its background on Swing's event thread.
     * Returns null after reporting a failure once, so startup can stop before opening a game.
     * Resource paths and a positive tile size are application configuration.
     *
     * <p>The map source is passed in so startup can share one loaded map between drawing and
     * other users, such as the enemies' walkability checks.
     *
     * @param mapSource where the map is loaded from
     * @param mapName name of the map resource to load
     * @param tileSize positive width and height of each tile in pixels
     * @param imagePaths non-null mapping of terrain IDs to absolute classpath image paths
     * @param showError non-null callback that displays a startup failure
     * @return the prepared layer, or null if map loading or image preparation fails
     */
    public MapLayer build(MapDataAccessInterface mapSource, String mapName, int tileSize,
                          Map<Integer, String> imagePaths, Consumer<String> showError) {
        MapViewModel viewModel = new MapViewModel();
        MapPresenter presenter = new MapPresenter(viewModel);
        LoadMapInteractor loadMap = new LoadMapInteractor(mapSource, presenter);
        MapController controller = new MapController(loadMap);
        controller.load(mapName);
        if (!viewModel.getState().getErrorMessage().isEmpty()) {
            showError.accept(viewModel.getState().getErrorMessage());
            return null;
        }

        TileSprites sprites;
        try {
            sprites = new TileSprites(imagePaths, tileSize);
        } catch (IOException error) {
            showError.accept(error.getMessage());
            return null;
        }
        MapRenderer renderer = new MapRenderer(sprites);
        renderer.render(viewModel.getState());
        if (!renderer.getErrorMessage().isEmpty()) {
            showError.accept(renderer.getErrorMessage());
            return null;
        }
        return new MapLayer(viewModel, renderer);
    }
}
