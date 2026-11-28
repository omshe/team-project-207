package use_case.update_game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import entity.Player;
import entity.TestTerrain;
import entity.TileMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Checks one tick of player movement using a fake presenter and in-memory terrain (no Swing, no
 * files): movement, terrain blocking, clamping, facing and moving state. Covers Use Case 1's main
 * scenario and each extension. Also guards that adding mob waves did not change player movement.
 */
class UpdateGameInteractorTest {
    private static final TileMap OPEN = TestTerrain.openGround(30, 20);

    /** Water fills column 2 (x 64..96) of a 128 x 96 map. */
    private static final TileMap WATER_COLUMN = TestTerrain.fromRows(
            "1101",
            "1101",
            "1101");

    /** In-memory arena that a test can swap between ticks. */
    private static class InMemoryTerrain implements UpdateGameDataAccessInterface {
        private TileMap current;

        InMemoryTerrain(TileMap current) {
            this.current = current;
        }

        @Override
        public TileMap getCurrentMap() {
            return current;
        }
    }

    private static class RecordingPresenter implements UpdateGameOutputBoundary {
        private UpdateGameOutputData last;

        @Override
        public void present(UpdateGameOutputData outputData) {
            last = outputData;
        }
    }

    private RecordingPresenter presenter;
    private UpdateGameInteractor interactor;

    @BeforeEach
    void setUp() {
        presenter = new RecordingPresenter();
        interactor = new UpdateGameInteractor(new Player(100, 100, 48, 4),
                new InMemoryTerrain(OPEN), 960, 640, presenter);
    }

    @Test
    void movesThePlayerAndPresentsItsPosition() {
        interactor.execute(new UpdateGameInputData(1, 0));

        assertEquals(104, presenter.last.getPlayerX(), 1e-9);
        assertEquals(100, presenter.last.getPlayerY(), 1e-9);
        assertEquals(48, presenter.last.getPlayerSize());
    }

    @Test
    void movingRightIsMovingAndFacesRight() {
        interactor.execute(new UpdateGameInputData(1, 0));

        assertTrue(presenter.last.isPlayerMoving());
        assertFalse(presenter.last.isPlayerFacingLeft());
    }

    @Test
    void movingLeftFacesLeft() {
        interactor.execute(new UpdateGameInputData(-1, 0));

        assertTrue(presenter.last.isPlayerFacingLeft());
    }

    @Test
    void movingStraightUpKeepsThePreviousFacing() {
        interactor.execute(new UpdateGameInputData(-1, 0));
        interactor.execute(new UpdateGameInputData(0, -1));

        assertTrue(presenter.last.isPlayerFacingLeft());
        assertTrue(presenter.last.isPlayerMoving());
    }

    @Test
    void noInputMeansNotMoving() {
        interactor.execute(new UpdateGameInputData(1, 0));
        interactor.execute(new UpdateGameInputData(0, 0));

        assertFalse(presenter.last.isPlayerMoving());
        assertEquals(104, presenter.last.getPlayerX(), 1e-9);
    }

    @Test
    void keepsThePlayerInsideTheTopLeftOfTheWorld() {
        UpdateGameInteractor edge = new UpdateGameInteractor(
                new Player(0, 0, 48, 4), new InMemoryTerrain(OPEN), 960, 640, presenter);

        edge.execute(new UpdateGameInputData(-1, -1));

        assertEquals(0, presenter.last.getPlayerX(), 0);
        assertEquals(0, presenter.last.getPlayerY(), 0);
    }

    @Test
    void keepsThePlayerInsideTheRightOfTheWorld() {
        UpdateGameInteractor edge = new UpdateGameInteractor(
                new Player(958, 0, 48, 4), new InMemoryTerrain(OPEN), 960, 640, presenter);

        edge.execute(new UpdateGameInputData(1, -1));

        assertEquals(960 - 48, presenter.last.getPlayerX(), 1e-9);
        assertEquals(0, presenter.last.getPlayerY(), 1e-9);
    }

    @Test
    void stopsAtTheWaterEdgeAndStaysThere() {
        UpdateGameInteractor nearWater = new UpdateGameInteractor(
                new Player(46, 32, 16, 4), new InMemoryTerrain(WATER_COLUMN), 960, 640,
                presenter);

        nearWater.execute(new UpdateGameInputData(1, 0));
        assertEquals(48, presenter.last.getPlayerX(), 1e-9);

        nearWater.execute(new UpdateGameInputData(1, 0));
        assertEquals(48, presenter.last.getPlayerX(), 1e-9);
        assertTrue(presenter.last.isPlayerMoving());
        assertFalse(presenter.last.isPlayerFacingLeft());
    }

    @Test
    void turnsTowardsWaterEvenWhenBlocked() {
        UpdateGameInteractor nearWater = new UpdateGameInteractor(
                new Player(96, 32, 16, 4), new InMemoryTerrain(WATER_COLUMN), 960, 640,
                presenter);

        nearWater.execute(new UpdateGameInputData(-1, 0));

        assertEquals(96, presenter.last.getPlayerX(), 1e-9);
        assertTrue(presenter.last.isPlayerFacingLeft());
    }

    @Test
    void slidesAlongWaterWhenOnlyOneDirectionIsBlocked() {
        UpdateGameInteractor nearWater = new UpdateGameInteractor(
                new Player(48, 32, 16, 4), new InMemoryTerrain(WATER_COLUMN), 960, 640,
                presenter);

        nearWater.execute(new UpdateGameInputData(1, -1));

        assertEquals(48, presenter.last.getPlayerX(), 1e-9);
        assertEquals(32 - 4 / Math.sqrt(2), presenter.last.getPlayerY(), 1e-9);
    }

    @Test
    void stopsAtTheArenaEdgeInsideLargerWorld() {
        UpdateGameInteractor smallArena = new UpdateGameInteractor(
                new Player(110, 0, 16, 4), new InMemoryTerrain(WATER_COLUMN), 960, 640,
                presenter);

        smallArena.execute(new UpdateGameInputData(1, 0));

        assertEquals(112, presenter.last.getPlayerX(), 1e-9);
    }

    @Test
    void readsTheCurrentArenaEveryTick() {
        InMemoryTerrain terrain = new InMemoryTerrain(WATER_COLUMN);
        UpdateGameInteractor switching = new UpdateGameInteractor(
                new Player(44, 32, 16, 4), terrain, 960, 640, presenter);

        switching.execute(new UpdateGameInputData(1, 0));
        switching.execute(new UpdateGameInputData(1, 0));
        assertEquals(48, presenter.last.getPlayerX(), 1e-9);

        terrain.current = OPEN;
        switching.execute(new UpdateGameInputData(1, 0));
        assertEquals(52, presenter.last.getPlayerX(), 1e-9);
    }
}
