package interface_adapter.game;

/** An immutable snapshot of everything the view draws. A new one is made every tick. */
public class GameState {
    private final int playerX;
    private final int playerY;
    private final int playerSize;
    private final PlayerAnimation playerAnimation;
    private final boolean playerFacingLeft;
    private final int playerAnimationTick;

    /**
     * Creates a snapshot.
     *
     * @param playerX             the player's x on screen, in whole pixels
     * @param playerY             the player's y on screen, in whole pixels
     * @param playerSize          width and height of the player box, in pixels
     * @param playerAnimation     which animation to play
     * @param playerFacingLeft    whether to draw the player mirrored
     * @param playerAnimationTick how many ticks the current animation has been playing (0 on its first tick)
     */
    public GameState(int playerX, int playerY, int playerSize,
                     PlayerAnimation playerAnimation, boolean playerFacingLeft, int playerAnimationTick) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.playerSize = playerSize;
        this.playerAnimation = playerAnimation;
        this.playerFacingLeft = playerFacingLeft;
        this.playerAnimationTick = playerAnimationTick;
    }

    public int getPlayerX() {
        return playerX;
    }

    public int getPlayerY() {
        return playerY;
    }

    public int getPlayerSize() {
        return playerSize;
    }

    public PlayerAnimation getPlayerAnimation() {
        return playerAnimation;
    }

    public boolean isPlayerFacingLeft() {
        return playerFacingLeft;
    }

    public int getPlayerAnimationTick() {
        return playerAnimationTick;
    }
}
