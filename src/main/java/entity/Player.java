package entity;

/**
 * The player character. Holds only data and rules (position, size, speed, facing).
 * Knows nothing about Swing, threads or the screen.
 */
public class Player {
    private double x;
    private double y;
    private final int size;
    private final double speed;
    private boolean facingLeft;
    private boolean moving;

    /**
     * Creates a player that starts facing right and not moving.
     *
     * @param x     starting x position (top-left corner, in pixels)
     * @param y     starting y position (top-left corner, in pixels)
     * @param size  width and height of the player box, in pixels
     * @param speed distance moved per tick, in pixels
     */
    public Player(double x, double y, int size, double speed) {
        this.x = x;
        this.y = y;
        this.size = size;
        this.speed = speed;
    }

    /**
     * Moves the player one tick in the given direction.
     * dx and dy are -1, 0 or 1. Diagonal movement is normalized so it is not faster.
     * Moving sideways turns the player to face that way; moving straight up or down keeps the old facing.
     */
    public void move(int dx, int dy) {
        moving = dx != 0 || dy != 0;
        if (!moving) {
            return;
        }
        if (dx < 0) {
            facingLeft = true;
        }
        if (dx > 0) {
            facingLeft = false;
        }
        double length = Math.sqrt(dx * dx + dy * dy);
        x += dx / length * speed;
        y += dy / length * speed;
    }

    /** Keeps the player inside a world of the given size. */
    public void clampTo(int worldWidth, int worldHeight) {
        x = Math.max(0, Math.min(x, worldWidth - size));
        y = Math.max(0, Math.min(y, worldHeight - size));
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getSize() {
        return size;
    }

    /** True if the player last turned left; players start facing right. */
    public boolean isFacingLeft() {
        return facingLeft;
    }

    /** True if the player was asked to move on the most recent tick. */
    public boolean isMoving() {
        return moving;
    }
}
