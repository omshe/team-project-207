package use_case.update_game;

/** What the view needs to know after a tick. */
public class UpdateGameOutputData {
    private final double playerX;
    private final double playerY;
    private final int playerSize;
    private final boolean playerMoving;
    private final boolean playerFacingLeft;

    /**
     * Creates the result of one tick.
     *
     * @param playerX          the player's x (top-left corner), in pixels
     * @param playerY          the player's y (top-left corner), in pixels
     * @param playerSize       width and height of the player box, in pixels
     * @param playerMoving     whether the player was asked to move this tick
     * @param playerFacingLeft whether the player faces left
     */
    public UpdateGameOutputData(double playerX, double playerY, int playerSize,
                                boolean playerMoving, boolean playerFacingLeft) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerSize = playerSize;
        this.playerMoving = playerMoving;
        this.playerFacingLeft = playerFacingLeft;
    }

    public double getPlayerX() {
        return playerX;
    }

    public double getPlayerY() {
        return playerY;
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public boolean isPlayerMoving() {
        return playerMoving;
    }

    public boolean isPlayerFacingLeft() {
        return playerFacingLeft;
    }
}
