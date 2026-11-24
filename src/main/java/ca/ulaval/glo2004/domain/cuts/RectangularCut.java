package ca.ulaval.glo2004.domain.cuts;

import java.awt.geom.Point2D;
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

    public RectangularCut(Point2D.Double topLeft, double width, double height, Tool tool) {
        super(topLeft, new Point2D.Double(topLeft.x + width, topLeft.y + height), tool);
        checkSize(width, height);
    }

    private static void checkSize(double width, double height) {
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

    /** A rectangle narrower than the tool can't be cut out. */
    @Override
    public void checkToolFits() {
        if (getWidth() < tool.getWidth() || getHeight() < tool.getWidth()) {
            throw new IllegalArgumentException(String.format(
                    "The rectangle is smaller than the tool (%.1f mm).", tool.getWidth()));
        }
    }

    @Override
    protected boolean selectableInside() {
        return true;
    }

    /** Keeps the top-left corner and moves the opposite one. */
    @Override
    public void resize(double newWidth, double newHeight) {
        checkSize(newWidth, newHeight);
        destination = new Point2D.Double(origin.x + newWidth, origin.y + newHeight);
    }

    @Override
    public Point2D.Double referencePoint() {
        return copy(origin);
    }
}
