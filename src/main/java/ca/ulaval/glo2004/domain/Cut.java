package ca.ulaval.glo2004.domain;

import java.awt.BasicStroke;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
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
    protected Point2D.Double origin;
    protected Point2D.Double destination;
    protected Tool tool;
    /** How deep the tool goes (mm). Set by the panel: the full thickness unless asked otherwise. */
    protected double depth;
    protected boolean selected;

    protected Cut(Point2D.Double origin, Point2D.Double destination, Tool tool) {
        if (origin == null || destination == null) {
            throw new IllegalArgumentException("A cut needs a start and an end point.");
        }
        if (tool == null) {
            throw new IllegalArgumentException("A cut needs a tool.");
        }
        // Defensive copies: Point2D.Double is mutable and must not be shared with the caller.
        this.origin = copy(origin);
        this.destination = copy(destination);
        this.tool = tool;
        this.id = UUID.randomUUID();
    }

    public abstract CutType type();

    /** Line followed by the centre of the tool. */
    protected abstract Shape path();

    /** Point used to describe where the cut is (shown to the user). */
    public abstract Point2D.Double referencePoint();

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
    protected Point2D.Double constrainOrigin(Point2D.Double requested) {
        return copy(requested);
    }

    /** Changes the size of the cut. Only cuts that have a width and a height support it. */
    public void resize(double newWidth, double newHeight) {
        throw new IllegalArgumentException("This type of cut can't be resized.");
    }

    /** Sets both ends at once. Only cuts defined by two free points (the L) support it. */
    public void reshape(Point2D.Double newOrigin, Point2D.Double newDestination) {
        throw new IllegalArgumentException("This type of cut can't be reshaped.");
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
    public double getWidth() {
        return Math.abs(destination.x - origin.x);
    }

    /** Vertical size, always positive (an L can be drawn upwards). */
    public double getHeight() {
        return Math.abs(destination.y - origin.y);
    }

    /**
     * The tool path as a list of points to visit in order (a closed shape ends on its first point). Read from
     * {@link #path()}, so it always matches what is drawn and checked.
     */
    public List<Point2D.Double> pathPoints() {
        List<Point2D.Double> points = new ArrayList<>();
        Point2D.Double start = null;
        double[] c = new double[6];
        for (PathIterator it = path().getPathIterator(null); !it.isDone(); it.next()) {
            switch (it.currentSegment(c)) {
                case PathIterator.SEG_MOVETO -> {
                    start = new Point2D.Double(c[0], c[1]);
                    points.add(start);
                }
                case PathIterator.SEG_LINETO -> points.add(new Point2D.Double(c[0], c[1]));
                case PathIterator.SEG_CLOSE -> {
                    if (start != null && !points.get(points.size() - 1).equals(start)) {
                        points.add(copy(start));
                    }
                }
                default -> throw new IllegalStateException("Cut paths are made of straight lines only.");
            }
        }
        return points;
    }

    /** Material removed by the tool. */
    public Shape getCutArea() {
        return thicken(tool.getWidth());
    }

    private Shape thicken(double width) {
        return new BasicStroke((float) width, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER).createStrokedShape(path());
    }

    /** True if the point touches the cut, with a small tolerance to make clicking easier. */
    public boolean contains(Point2D.Double p) {
        return isNear(p, SELECTION_TOLERANCE);
    }

    /** True if the point is less than {@code distance} mm away from the cut area. */
    public boolean isNear(Point2D.Double p, double distance) {
        return thicken(tool.getWidth() + 2 * distance).contains(p) || (selectableInside() && path().contains(p));
    }

    /** Moves the cut by (dx, dy) without changing its shape. */
    public void translate(double dx, double dy) {
        origin.setLocation(origin.x + dx, origin.y + dy);
        destination.setLocation(destination.x + dx, destination.y + dy);
    }

    /** Where the origin would land if the cut were moved to {@code requested} (see {@link #constrainOrigin}). */
    public Point2D.Double movedOrigin(Point2D.Double requested) {
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

    /** Copy of a point: Point2D.Double is mutable and must not be shared. */
    public static Point2D.Double copy(Point2D.Double p) {
        return new Point2D.Double(p.x, p.y);
    }

    public Point2D.Double getOrigin() {
        return copy(origin);
    }

    public Point2D.Double getDestination() {
        return copy(destination);
    }

    public double getDepth() {
        return depth;
    }

    /** The panel checks that the depth is not more than its thickness. */
    public void setDepth(double depth) {
        if (depth <= 0) {
            throw new IllegalArgumentException("The depth must be positive.");
        }
        this.depth = depth;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("A cut needs a tool.");
        }
        this.tool = tool;
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
