package ca.ulaval.glo2004.domain.ordinaire;

import ca.ulaval.glo2004.domain.Coupe;
import ca.ulaval.glo2004.domain.Outil;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.io.Serializable;

/** Coupe horizontale : origine et destination ont la même ordonnée. */
public class CoupeHorizontale extends Coupe implements Serializable {

    public CoupeHorizontale(Point origine, Point destination, Outil outil) {
        super(origine, destination, outil);
        if (origine.y != destination.y) {
            throw new IllegalArgumentException("Pour une coupe horizontale, les coordonnées Y doivent être identiques.");
        }
    }

    @Override
    protected Shape trajet() {
        return new Line2D.Double(origine, destination);
    }

    @Override
    protected Point calculerPointDeReference() {
        return new Point((origine.x + destination.x) / 2, origine.y);
    }
}
