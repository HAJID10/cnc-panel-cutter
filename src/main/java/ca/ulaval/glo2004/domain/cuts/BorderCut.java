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

    @Override
    protected Shape path() {
        return new Rectangle2D.Double(origin.x, origin.y, getWidth(), getHeight());
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
