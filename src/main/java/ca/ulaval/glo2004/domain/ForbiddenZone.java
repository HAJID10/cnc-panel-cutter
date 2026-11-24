package ca.ulaval.glo2004.domain;

import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;
import java.util.UUID;

/**
 * A rectangle of the panel the tool must never touch, for example where a clamp holds the panel down.
 */
public class ForbiddenZone implements Serializable {
    private UUID id = UUID.randomUUID();
    private Rectangle2D.Double bounds;

    public ForbiddenZone(Point2D.Double topLeft, double width, double height) {
        setBounds(topLeft, width, height);
    }

    /** Moves and/or resizes the zone. */
    public void setBounds(Point2D.Double topLeft, double width, double height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("A forbidden zone needs a positive width and height.");
        }
        this.bounds = new Rectangle2D.Double(topLeft.x, topLeft.y, width, height);
    }

    /** True if the material removed by the cut overlaps this zone. */
    public boolean touches(Cut cut) {
        Area overlap = new Area(cut.getCutArea());
        overlap.intersect(new Area(bounds));
        return !overlap.isEmpty();
    }

    public boolean contains(Point2D.Double p) {
        return bounds.contains(p);
    }

    public Shape getShape() {
        return (Shape) bounds.clone();
    }

    public UUID getId() {
        return id;
    }

    /** Used when rebuilding a zone from a saved state. */
    public void setId(UUID id) {
        this.id = id;
    }

    public Point2D.Double getOrigin() {
        return new Point2D.Double(bounds.x, bounds.y);
    }

    public double getWidth() {
        return bounds.width;
    }

    public double getHeight() {
        return bounds.height;
    }
}
