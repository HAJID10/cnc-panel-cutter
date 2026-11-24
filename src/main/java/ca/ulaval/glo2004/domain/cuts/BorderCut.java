package ca.ulaval.glo2004.domain.cuts;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;

import ca.ulaval.glo2004.domain.Cut;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.domain.Tool;

/** Trims the panel down to a final width × length by cutting all around it. Always centred on the panel. */
public class BorderCut extends Cut {

    public BorderCut(Point topLeft, int width, int length, Tool tool) {
        super(topLeft, new Point(topLeft.x + width, topLeft.y + length), tool);
        checkSize(width, length);
    }

    private static void checkSize(int width, int length) {
        if (width <= 0 || length <= 0) {
            throw new IllegalArgumentException("The width and length must be positive.");
        }
    }

    @Override
    public CutType type() {
        return CutType.BORDER;
    }

    /**
     * origin/destination are the corners of the piece that is kept. The tool runs half a tool width outside them,
     * so the piece comes out at exactly the requested size.
     */
    @Override
    protected Shape path() {
        double halfTool = tool.getWidth() / 2;
        return new Rectangle2D.Double(origin.x - halfTool, origin.y - halfTool,
                getWidth() + tool.getWidth(), getHeight() + tool.getWidth());
    }

    /** A final piece smaller than the tool makes no sense. */
    @Override
    public void checkToolFits() {
        if (getWidth() < tool.getWidth() || getHeight() < tool.getWidth()) {
            throw new IllegalArgumentException(String.format(
                    "The final size is smaller than the tool (%.1f mm).", tool.getWidth()));
        }
    }

    /** A border stays centred on the panel, so it never moves. */
    @Override
    public boolean isMovable() {
        return false;
    }

    /** Resizes around the same centre. */
    @Override
    public void resize(int newWidth, int newLength) {
        checkSize(newWidth, newLength);
        Point centre = referencePoint();
        origin = new Point(centre.x - newWidth / 2, centre.y - newLength / 2);
        destination = new Point(origin.x + newWidth, origin.y + newLength);
    }

    /** The centre of the border. */
    @Override
    public Point referencePoint() {
        return new Point(origin.x + getWidth() / 2, origin.y + getHeight() / 2);
    }
}
