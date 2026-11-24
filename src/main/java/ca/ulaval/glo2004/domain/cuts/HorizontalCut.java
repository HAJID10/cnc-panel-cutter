package ca.ulaval.glo2004.domain.cuts;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.domain.Tool;

/** A straight cut from left to right: origin and destination share the same y. */
public class HorizontalCut extends Cut {

    public HorizontalCut(Point origin, Point destination, Tool tool) {
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
    protected Point constrainOrigin(Point requested) {
        return new Point(origin.x, requested.y);
    }

    @Override
    public Point referencePoint() {
        return new Point((origin.x + destination.x) / 2, origin.y);
    }
}
