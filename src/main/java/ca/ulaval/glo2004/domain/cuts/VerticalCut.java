package ca.ulaval.glo2004.domain.cuts;

import java.awt.geom.Point2D;
import java.awt.Shape;
import java.awt.geom.Line2D;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.domain.Tool;

/** A straight cut from top to bottom: origin and destination share the same x. */
public class VerticalCut extends Cut {

    public VerticalCut(Point2D.Double origin, Point2D.Double destination, Tool tool) {
        super(origin, destination, tool);
        if (origin.x != destination.x) {
            throw new IllegalArgumentException("A vertical cut needs the same x at both ends.");
        }
    }

    @Override
    public CutType type() {
        return CutType.VERTICAL;
    }

    @Override
    protected Shape path() {
        return new Line2D.Double(origin, destination);
    }

    /** It can only slide sideways. */
    @Override
    protected Point2D.Double constrainOrigin(Point2D.Double requested) {
        return new Point2D.Double(requested.x, origin.y);
    }

    @Override
    public Point2D.Double referencePoint() {
        return new Point2D.Double(origin.x, (origin.y + destination.y) / 2);
    }
}
