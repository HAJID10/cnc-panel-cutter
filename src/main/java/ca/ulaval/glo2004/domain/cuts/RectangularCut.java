package ca.ulaval.glo2004.domain.cuts;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.domain.Tool;

/**
 * The tool follows the outline of a rectangle (top-left corner {@code origin}, opposite corner {@code destination}).
 * The path is offset half a tool width inwards, so the hole left in the panel has exactly the requested size.
 */
public class RectangularCut extends Cut {

    public RectangularCut(Point topLeft, int width, int height, Tool tool) {
        super(topLeft, new Point(topLeft.x + width, topLeft.y + height), tool);
        checkSize(width, height);
    }

    private static void checkSize(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("The width and height must be positive.");
        }
    }

    @Override
    public CutType type() {
        return CutType.RECTANGULAR;
    }

    @Override
    protected Shape path() {
        double halfTool = tool.getWidth() / 2;
        return new Rectangle2D.Double(origin.x + halfTool, origin.y + halfTool,
                Math.max(0, getWidth() - tool.getWidth()), Math.max(0, getHeight() - tool.getWidth()));
    }

    @Override
    protected boolean selectableInside() {
        return true;
    }

    /** Keeps the top-left corner and moves the opposite one. */
    @Override
    public void resize(int newWidth, int newHeight) {
        checkSize(newWidth, newHeight);
        destination = new Point(origin.x + newWidth, origin.y + newHeight);
    }

    @Override
    public Point referencePoint() {
        return new Point(origin);
    }
}
