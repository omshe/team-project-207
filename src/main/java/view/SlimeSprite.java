package view;

import interface_adapter.enemy_wave.EnemyDisplayData;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

/**
 * Draws an enemy as an animated slime fitted to its hitbox.
 *
 * <p><b>Animation:</b> the walk animation plays while the enemy is moving and the idle
 * animation while it is still. Frames advance every {@value #TICKS_PER_FRAME} simulation ticks
 * (60 ticks per second, so 7.5 frames per second). Timing comes from the tick count in the
 * snapshot, so repainting more or less often never changes the animation speed or the game.
 *
 * <p><b>Fitting:</b> the slime only fills a small part of each 100 x 100 frame. The visible
 * area shared by the idle and walk frames is scaled so its width equals the hitbox width,
 * centred horizontally and resting on the hitbox's bottom edge. The hitbox itself is decided by
 * the game settings, never by the image. Nearest-neighbour scaling keeps the pixel art sharp.
 *
 * <p><b>Facing:</b> the art faces right; it is mirrored when the enemy faces left.
 */
public class SlimeSprite {
    /** Simulation ticks each animation frame stays on screen. */
    static final int TICKS_PER_FRAME = 8;

    private static final String IDLE_SHEET = "/sprites/enemy/slime/slime-idle.png";
    private static final String WALK_SHEET = "/sprites/enemy/slime/slime-walk.png";
    private static final int FRAME_SIZE = 100;

    private final SpriteSheet idle;
    private final SpriteSheet walk;
    private final Rectangle visibleBounds;

    /**
     * Loads the slime sheets from the classpath.
     *
     * @throws IllegalStateException if a sheet is missing or has an unexpected layout
     */
    public SlimeSprite() {
        this.idle = new SpriteSheet(IDLE_SHEET, FRAME_SIZE, FRAME_SIZE);
        this.walk = new SpriteSheet(WALK_SHEET, FRAME_SIZE, FRAME_SIZE);
        // Use the union of both animations so the slime doesn't jump when switching.
        this.visibleBounds = idle.getVisibleBounds().union(walk.getVisibleBounds());
    }

    /**
     * Draws one enemy.
     *
     * @param graphics      where to draw
     * @param enemy         the enemy's display values
     * @param animationTick the snapshot's tick count, used to choose the frame
     */
    public void draw(Graphics2D graphics, EnemyDisplayData enemy, long animationTick) {
        SpriteSheet sheet = idle;
        if (enemy.isMoving()) {
            sheet = walk;
        }
        BufferedImage frame = sheet.getFrame(animationTick / TICKS_PER_FRAME);

        // Scale so the visible slime is exactly as wide as the hitbox.
        double scale = (double) enemy.getWidth() / visibleBounds.width;
        int drawnWidth = (int) Math.round(FRAME_SIZE * scale);
        int drawnHeight = (int) Math.round(FRAME_SIZE * scale);

        // Where the visible slime's centre and bottom sit inside the (possibly mirrored) frame.
        double visibleCenterX = visibleBounds.x + visibleBounds.width / 2.0;
        if (enemy.isFacingLeft()) {
            visibleCenterX = FRAME_SIZE - visibleCenterX;
        }
        double visibleBottom = visibleBounds.y + visibleBounds.height;

        // Line those points up with the hitbox's centre and bottom edge.
        double hitboxCenterX = enemy.getX() + enemy.getWidth() / 2.0;
        double hitboxBottom = enemy.getY() + enemy.getHeight();
        int left = (int) Math.round(hitboxCenterX - visibleCenterX * scale);
        int top = (int) Math.round(hitboxBottom - visibleBottom * scale);

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if (enemy.isFacingLeft()) {
            // Swapping the destination's left and right edges mirrors the image.
            graphics.drawImage(frame, left + drawnWidth, top, left, top + drawnHeight,
                    0, 0, FRAME_SIZE, FRAME_SIZE, null);
        } else {
            graphics.drawImage(frame, left, top, left + drawnWidth, top + drawnHeight,
                    0, 0, FRAME_SIZE, FRAME_SIZE, null);
        }
    }
}
