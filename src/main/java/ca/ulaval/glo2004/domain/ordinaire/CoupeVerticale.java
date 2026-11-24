package ca.ulaval.glo2004.domain.ordinaire;

import ca.ulaval.glo2004.domain.Coupe;
import ca.ulaval.glo2004.domain.Outil;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Line2D;
import java.io.Serializable;

/** Coupe verticale : origine et destination ont la même abscisse. */
public class CoupeVerticale extends Coupe implements Serializable {

    public CoupeVerticale(Point origine, Point destination, Outil outil) {
        super(origine, destination, outil);
        if (origine.x != destination.x) {
            throw new IllegalArgumentException("Pour une coupe verticale, les coordonnées X doivent être identiques.");
        }
    }

    @Override
    protected Shape trajet() {
        return new Line2D.Double(origine, destination);
    }

    @Override
    protected Point calculerPointDeReference() {
        return new Point(origine.x, (origine.y + destination.y) / 2);
    }
}
