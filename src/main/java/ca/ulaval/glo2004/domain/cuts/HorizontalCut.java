package ca.ulaval.glo2004.domain.cuts;

import java.awt.geom.Point2D;
import java.awt.Shape;
import java.awt.geom.Line2D;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.domain.Tool;

/** A straight cut from left to right: origin and destination share the same y. */
public class HorizontalCut extends Cut {

    public HorizontalCut(Point2D.Double origin, Point2D.Double destination, Tool tool) {
        super(origin, destination, tool);
        if (origin.y != destination.y) {
            throw new IllegalArgumentException("A horizontal cut needs the same y at both ends.");
        }
    }

    @Override
    public CutType type() {
        return CutType.HORIZONTAL;
    }

    @Override
    protected Shape path() {
        return new Line2D.Double(origin, destination);
    }

    /** It can only slide up or down. */
    @Override
    protected Point2D.Double constrainOrigin(Point2D.Double requested) {
        return new Point2D.Double(origin.x, requested.y);
    }

    @Override
    public Point2D.Double referencePoint() {
        return new Point2D.Double((origin.x + destination.x) / 2, origin.y);
    }
}
