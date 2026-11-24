package ca.ulaval.glo2004.domain.cuts;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.domain.Tool;

/** A straight cut from top to bottom: origin and destination share the same x. */
public class VerticalCut extends Cut {

    public VerticalCut(Point origin, Point destination, Tool tool) {
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
    protected Point constrainOrigin(Point requested) {
        return new Point(requested.x, origin.y);
    }

    @Override
    public Point referencePoint() {
        return new Point(origin.x, (origin.y + destination.y) / 2);
    }
}
