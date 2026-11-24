package ca.ulaval.glo2004.domain.cuts;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.domain.Tool;

/**
 * An L-shaped cut whose corner is the destination: one horizontal arm going back to the origin's x and one
 * vertical arm going back to the origin's y.
 */
public class LCut extends Cut {

    public LCut(Point origin, Point corner, Tool tool) {
        super(origin, corner, tool);
    }

    @Override
    public CutType type() {
        return CutType.L;
    }

    @Override
    protected Shape path() {
        Path2D.Double path = new Path2D.Double();
        path.moveTo(origin.x, destination.y);
        path.lineTo(destination.x, destination.y);
        path.lineTo(destination.x, origin.y);
        return path;
    }

    /** The corner of the L. */
    @Override
    public Point referencePoint() {
        return new Point(destination);
    }
}
