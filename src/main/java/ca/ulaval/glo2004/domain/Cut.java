package ca.ulaval.glo2004.domain;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.io.Serializable;
import java.util.UUID;

/**
 * A cut on the panel. Every cut type describes its <em>path</em>: the line followed by the centre of the tool.
 * The material actually removed is that path thickened by the tool width, and the click area used for
 * selection is the same shape with a small extra margin.
 * <p>
 * Each subclass decides how it moves ({@link #constrainOrigin}), whether it can be resized ({@link #resize})
 * and what its path looks like, so the {@link Panel} never needs to check the concrete type.
 */
public abstract class Cut implements Serializable {

    /** Margin (mm) around a cut inside which a click still selects it. */
    public static final double SELECTION_TOLERANCE = 3.0;

    protected UUID id;
    protected Point origin;
    protected Point destination;
    protected Tool tool;
    protected double depth;
    protected boolean selected;

    protected Cut(Point origin, Point destination, Tool tool) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("A cut needs a start and an end point.");
        }
        if (tool == null) {
            throw new IllegalArgumentException("A cut needs a tool.");
        }
        // Defensive copies: java.awt.Point is mutable and must not be shared with the caller.
        this.origin = new Point(origin);
        this.destination = new Point(destination);
        this.tool = tool;
        this.depth = tool.getWidth();
        this.id = UUID.randomUUID();
    }

    public abstract CutType type();

    /** Line followed by the centre of the tool. */
    protected abstract Shape path();

    /** Point used to describe where the cut is (shown to the user). */
    public abstract Point referencePoint();

    /** True if clicking inside the closed path also selects the cut (rectangular cuts). */
    protected boolean selectableInside() {
        return false;
    }

    /** False for cuts whose position is fixed (the border is always centred). */
    public boolean isMovable() {
        return true;
    }

    /**
     * Adjusts a requested new origin to what this cut allows. A vertical cut, for example, can only slide
     * sideways, so it keeps its own y.
     */
    protected Point constrainOrigin(Point requested) {
        return new Point(requested);
    }

    /** Changes the size of the cut. Only cuts that have a width and a height support it. */
    public void resize(int newWidth, int newHeight) {
        throw new IllegalArgumentException("This type of cut can't be resized.");
    }

    /**
     * Checks that the tool can actually make this cut. Does nothing by default; cuts with an inside (rectangle,
     * border) refuse to be smaller than the tool.
     */
    public void checkToolFits() {
    }

    /** True if both cuts are the same kind of cut at exactly the same place. */
    public boolean sameAs(Cut other) {
        return other != this && type() == other.type() && origin.equals(other.origin)
                && destination.equals(other.destination);
    }

    /** Horizontal size, always positive (an L can be drawn towards the left). */
    public int getWidth() {
        return Math.abs(destination.x - origin.x);
    }

    /** Vertical size, always positive (an L can be drawn upwards). */
    public int getHeight() {
        return Math.abs(destination.y - origin.y);
    }

    /** Material removed by the tool. */
    public Shape getCutArea() {
        return thicken(tool.getWidth());
    }

    private Shape thicken(double width) {
        return new BasicStroke((float) width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER).createStrokedShape(path());
    }

    /** True if the point touches the cut, with a small tolerance to make clicking easier. */
    public boolean contains(Point p) {
        return isNear(p, SELECTION_TOLERANCE);
    }

    /** True if the point is less than {@code distance} mm away from the cut area. */
    public boolean isNear(Point p, double distance) {
        return thicken(tool.getWidth() + 2 * distance).contains(p) || (selectableInside() && path().contains(p));
    }

    /** Moves the cut by (dx, dy) without changing its shape. */
    public void translate(int dx, int dy) {
        origin.translate(dx, dy);
        destination.translate(dx, dy);
    }

    /** Where the origin would land if the cut were moved to {@code requested} (see {@link #constrainOrigin}). */
    public Point movedOrigin(Point requested) {
        return constrainOrigin(requested);
    }

    // --- Accessors ---

    public UUID getId() {
        return id;
    }

    /** Used when rebuilding a cut from a saved state, so that its identity survives undo and loading. */
    public void setId(UUID id) {
        this.id = id;
    }

    public Point getOrigin() {
        return new Point(origin);
    }

    public Point getDestination() {
        return new Point(destination);
    }

    public double getDepth() {
        return depth;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("A cut needs a tool.");
        }
        this.tool = tool;
        this.depth = tool.getWidth();
    }

    /** Call when the width of this cut's tool has changed. */
    public void toolChanged() {
        this.depth = tool.getWidth();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public String toString() {
        return type() + "[" + origin.x + "," + origin.y + " -> " + destination.x + "," + destination.y
                + ", tool=" + tool.getName() + "]";
    }
}
