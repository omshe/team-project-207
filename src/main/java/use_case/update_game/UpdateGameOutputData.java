package use_case.update_game;

/** What the view needs to know after a tick. */
public class UpdateGameOutputData {
    private final double playerX;
    private final double playerY;
    private final int playerSize;
    private final boolean playerMoving;
    private final boolean playerFacingLeft;

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
