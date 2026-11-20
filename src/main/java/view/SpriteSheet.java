package view;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.imageio.ImageIO;

/**
 * A horizontal strip of equal-sized animation frames loaded from a classpath PNG.
 *
 * <p>Reusable for any animated sprite (enemies, spells). Images are loaded from the classpath
 * (for example {@code /sprites/enemy/slime/slime-walk.png} under {@code src/main/resources}),
 * so the game finds them from Maven, IntelliJ or a jar without machine-specific paths.
 * Transparency is kept as stored in the PNG.
 */
public class SpriteSheet {
    private final List<BufferedImage> frames;
    private final Rectangle visibleBounds;

    /**
     * Loads a sprite sheet and cuts it into frames.
     *
     * @param resourcePath absolute classpath path of the PNG, starting with {@code /}
     * @param frameWidth   width of one frame, in pixels
     * @param frameHeight  height of one frame (and of the sheet), in pixels
     * @throws IllegalStateException if the resource is missing, unreadable, or not a whole
     *                               number of frames wide; startup fails with a clear message
     *                               instead of the game silently drawing nothing
     */
    public SpriteSheet(String resourcePath, int frameWidth, int frameHeight) {
        BufferedImage sheet = load(resourcePath);
        if (sheet.getHeight() != frameHeight || sheet.getWidth() % frameWidth != 0) {
            throw new IllegalStateException("Sprite sheet " + resourcePath + " is "
                    + sheet.getWidth() + " x " + sheet.getHeight()
                    + ", which is not a single row of " + frameWidth + " x " + frameHeight
                    + " frames");
        }

        List<BufferedImage> cutFrames = new ArrayList<>();
        int frameCount = sheet.getWidth() / frameWidth;
        for (int index = 0; index < frameCount; index++) {
            cutFrames.add(sheet.getSubimage(index * frameWidth, 0, frameWidth, frameHeight));
        }
        this.frames = Collections.unmodifiableList(cutFrames);
        this.visibleBounds = measureVisibleBounds(cutFrames, resourcePath);
    }

    private static BufferedImage load(String resourcePath) {
        try (InputStream stream = SpriteSheet.class.getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Sprite not found on the classpath: "
                        + resourcePath);
            }
            BufferedImage image = ImageIO.read(stream);
            if (image == null) {
                throw new IllegalStateException("Not a readable image: " + resourcePath);
            }
            return image;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not read sprite " + resourcePath, exception);
        }
    }

    /**
     * Finds the smallest rectangle (in frame coordinates) containing every non-transparent
     * pixel of every frame. This lets callers scale the visible character, not the padding.
     */
    private static Rectangle measureVisibleBounds(List<BufferedImage> frames,
                                                  String resourcePath) {
        Rectangle bounds = null;
        for (BufferedImage frame : frames) {
            for (int pixelY = 0; pixelY < frame.getHeight(); pixelY++) {
                for (int pixelX = 0; pixelX < frame.getWidth(); pixelX++) {
                    int alpha = frame.getRGB(pixelX, pixelY) >>> 24;
                    if (alpha == 0) {
                        continue;
                    }
                    Rectangle pixel = new Rectangle(pixelX, pixelY, 1, 1);
                    if (bounds == null) {
                        bounds = pixel;
                    } else {
                        bounds = bounds.union(pixel);
                    }
                }
            }
        }
        if (bounds == null) {
            throw new IllegalStateException("Sprite sheet is fully transparent: "
                    + resourcePath);
        }
        return bounds;
    }

    /**
     * Returns one frame.
     *
     * @param index frame number; wraps around, so any non-negative tick-based count works
     * @return the frame image
     */
    public BufferedImage getFrame(long index) {
        return frames.get((int) (index % frames.size()));
    }

    /**
     * Returns the number of frames.
     *
     * @return the frame count, at least 1
     */
    public int getFrameCount() {
        return frames.size();
    }

    /**
     * Returns the area of a frame that any frame actually draws in.
     *
     * @return a copy of the visible bounds, in frame pixel coordinates
     */
    public Rectangle getVisibleBounds() {
        return new Rectangle(visibleBounds);
    }
}
