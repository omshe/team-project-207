package view;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

/** One looping animation cut from a horizontal sprite sheet of square frames. Picks the frame to show for a tick. */
public class SpriteAnimation {
    private final BufferedImage[] frames;
    private final int ticksPerFrame;

    /**
     * @param resourcePath  classpath path of the sheet, e.g. "/sprites/wizard/Wizard-Walk.png"
     * @param frameSize     width and height of one frame, in pixels
     * @param ticksPerFrame how many game ticks each frame stays on screen
     */
    public SpriteAnimation(String resourcePath, int frameSize, int ticksPerFrame) {
        BufferedImage sheet = load(resourcePath);
        int frameCount = sheet.getWidth() / frameSize;
        this.frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = sheet.getSubimage(i * frameSize, 0, frameSize, frameSize);
        }
        this.ticksPerFrame = ticksPerFrame;
    }

    /** The frame to show after the animation has played for the given number of ticks. Loops forever. */
    public BufferedImage frameAt(int tick) {
        int index = (tick / ticksPerFrame) % frames.length;
        return frames[index];
    }

    private static BufferedImage load(String resourcePath) {
        URL url = SpriteAnimation.class.getResource(resourcePath);
        if (url == null) {
            throw new IllegalStateException("Missing sprite sheet on the classpath: " + resourcePath);
        }
        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not read sprite sheet " + resourcePath, e);
        }
    }
}
