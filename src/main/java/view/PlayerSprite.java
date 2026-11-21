package view;

import interface_adapter.game.GameState;
import interface_adapter.game.PlayerAnimation;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.EnumMap;
import java.util.Map;

/**
 * Draws the player's current animation frame, scaled up, flipped when facing left,
 * with the character's feet on the bottom edge of the player's box.
 */
public class PlayerSprite {
    private final Map<PlayerAnimation, SpriteAnimation> animations;
    private final int frameSize;
    private final int scale;
    private final double bodyCenterX;
    private final int feetY;

    /**
     * Creates a sprite from one animation per PlayerAnimation.
     *
     * @param animations  the animation to play for each PlayerAnimation
     * @param frameSize   width and height of one sheet frame, in sheet pixels
     * @param scale       how many screen pixels each sheet pixel becomes
     * @param bodyCenterX x of the character's body centre inside a frame, in sheet pixels
     * @param feetY       y just below the character's feet inside a frame, in sheet pixels
     */
    public PlayerSprite(Map<PlayerAnimation, SpriteAnimation> animations,
                        int frameSize, int scale, double bodyCenterX, int feetY) {
        this.animations = new EnumMap<>(animations);
        this.frameSize = frameSize;
        this.scale = scale;
        this.bodyCenterX = bodyCenterX;
        this.feetY = feetY;
    }

    /**
     * The wizard from the Tiny RPG Character Asset Pack (100x100 frames, body spans x 43..59,
     * feet on row 59).
     */
    public static PlayerSprite wizard() {
        Map<PlayerAnimation, SpriteAnimation> animations = new EnumMap<>(PlayerAnimation.class);
        animations.put(PlayerAnimation.IDLE,
                new SpriteAnimation("/sprites/wizard/Wizard-Idle.png", 100, 10));
        animations.put(PlayerAnimation.WALK,
                new SpriteAnimation("/sprites/wizard/Wizard-Walk.png", 100, 6));
        return new PlayerSprite(animations, 100, 3, 51.5, 60);
    }

    /**
     * Draws the player's current frame, centred on the player box with its feet on the box's
     * bottom edge.
     *
     * @param g2    where to draw
     * @param state the snapshot giving the player's box, animation, tick and facing
     */
    public void draw(Graphics2D g2, GameState state) {
        BufferedImage frame = animations.get(state.getPlayerAnimation())
                .frameAt(state.getPlayerAnimationTick());

        double boxCenterX = state.getPlayerX() + state.getPlayerSize() / 2.0;
        int boxBottom = state.getPlayerY() + state.getPlayerSize();
        int size = frameSize * scale;
        int top = boxBottom - feetY * scale;

        if (state.isPlayerFacingLeft()) {
            // Mirror the frame by drawing it with its left and right edges swapped.
            int left = (int) Math.round(boxCenterX - (frameSize - bodyCenterX) * scale);
            g2.drawImage(frame, left + size, top, left, top + size,
                    0, 0, frameSize, frameSize, null);
        } else {
            int left = (int) Math.round(boxCenterX - bodyCenterX * scale);
            g2.drawImage(frame, left, top, left + size, top + size,
                    0, 0, frameSize, frameSize, null);
        }
    }
}
