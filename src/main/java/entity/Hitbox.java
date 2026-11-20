package entity;

/**
 * An immutable axis-aligned rectangle in world pixels, used for collision and placement rules.
 *
 * <p>Coordinates follow the game's convention: the origin is the top-left corner of the world
 * and y grows downward. {@code (x, y)} is the rectangle's top-left corner.
 *
 * <p><b>Contact rule.</b> Two hitboxes <em>touch</em> when they overlap or share an edge or a
 * corner. Equivalently, the gap between them is zero or negative on both axes. A small
 * {@link #CONTACT_TOLERANCE} absorbs floating-point rounding, so a box moved exactly up to
 * another counts as touching it.
 *
 * <p>This class is pure domain geometry: it deliberately does not use
 * {@code java.awt.Rectangle}, which belongs to the Swing/AWT framework layer.
 */
public final class Hitbox {
    /** Largest gap, in pixels, that still counts as touching (absorbs rounding error). */
    public static final double CONTACT_TOLERANCE = 1e-6;

    private final double left;
    private final double top;
    private final double width;
    private final double height;

    /**
     * Creates a hitbox.
     *
     * @param left left edge, in pixels
     * @param top top edge, in pixels
     * @param width width, in pixels; must be positive
     * @param height height, in pixels; must be positive
     * @throws IllegalArgumentException if {@code width} or {@code height} is not positive
     */
    public Hitbox(double left, double top, double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException(
                    "Hitbox size must be positive, got " + width + " x " + height);
        }
        this.left = left;
        this.top = top;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns whether this hitbox touches or overlaps another one (edges and corners count).
     *
     * @param other the other hitbox
     * @return {@code true} if the gap between them is at most {@link #CONTACT_TOLERANCE} on
     *     both axes
     */
    public boolean touches(Hitbox other) {
        boolean closeHorizontally = horizontalGapTo(other) <= CONTACT_TOLERANCE;
        boolean closeVertically = verticalGapTo(other) <= CONTACT_TOLERANCE;
        return closeHorizontally && closeVertically;
    }

    /**
     * Returns the empty horizontal space between this hitbox and another.
     *
     * @param other the other hitbox
     * @return the gap in pixels: positive when they are apart on the x-axis, zero when their
     *     vertical edges line up, and negative when their x-ranges overlap
     */
    public double horizontalGapTo(Hitbox other) {
        double gapIfOtherIsRight = other.left - getRight();
        double gapIfOtherIsLeft = left - other.getRight();
        return Math.max(gapIfOtherIsRight, gapIfOtherIsLeft);
    }

    /**
     * Returns the empty vertical space between this hitbox and another.
     *
     * @param other the other hitbox
     * @return the gap in pixels: positive when they are apart on the y-axis, zero when their
     *     horizontal edges line up, and negative when their y-ranges overlap
     */
    public double verticalGapTo(Hitbox other) {
        double gapIfOtherIsBelow = other.top - getBottom();
        double gapIfOtherIsAbove = top - other.getBottom();
        return Math.max(gapIfOtherIsBelow, gapIfOtherIsAbove);
    }

    /**
     * Returns whether this hitbox lies entirely inside a world of the given size.
     *
     * @param worldWidth  world width, in pixels
     * @param worldHeight world height, in pixels
     * @return {@code true} if every edge is within {@code [0, worldWidth] x [0, worldHeight]}
     */
    public boolean isInsideWorld(double worldWidth, double worldHeight) {
        boolean insideHorizontally = left >= 0 && getRight() <= worldWidth;
        boolean insideVertically = top >= 0 && getBottom() <= worldHeight;
        return insideHorizontally && insideVertically;
    }

    /**
     * Returns the left edge.
     *
     * @return left edge, in pixels
     */
    public double getX() {
        return left;
    }

    /**
     * Returns the top edge.
     *
     * @return top edge, in pixels
     */
    public double getY() {
        return top;
    }

    /**
     * Returns the width.
     *
     * @return width, in pixels
     */
    public double getWidth() {
        return width;
    }

    /**
     * Returns the height.
     *
     * @return height, in pixels
     */
    public double getHeight() {
        return height;
    }

    /**
     * Returns the right edge.
     *
     * @return {@code x + width}, in pixels
     */
    public double getRight() {
        return left + width;
    }

    /**
     * Returns the bottom edge.
     *
     * @return {@code y + height}, in pixels
     */
    public double getBottom() {
        return top + height;
    }

    /**
     * Returns the x-coordinate of the centre.
     *
     * @return centre x, in pixels
     */
    public double getCenterX() {
        return left + width / 2;
    }

    /**
     * Returns the y-coordinate of the centre.
     *
     * @return centre y, in pixels
     */
    public double getCenterY() {
        return top + height / 2;
    }
}
