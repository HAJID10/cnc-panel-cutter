package ca.ulaval.glo2004.domain.irregulieres;

import ca.ulaval.glo2004.domain.Coupe;
import ca.ulaval.glo2004.domain.Outil;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.io.Serializable;

/**
 * Coupe en L dont le coin est la destination : un bras horizontal vers l'abscisse de l'origine et un bras vertical
 * vers l'ordonnée de l'origine (même tracé qu'à l'affichage).
 */
public class CoupeEnL extends Coupe implements Serializable {

    public CoupeEnL(Point origine, Point destination, Outil outil) {
        super(origine, destination, outil);
    }

    /**
     * Bug corrigé : l'ancienne forme (un coude à mi-chemin) ne correspondait pas au tracé affiché, et le deuxième
     * segment écrasait le premier, si bien qu'on ne pouvait sélectionner qu'une moitié du L.
     */
    @Override
    protected Shape trajet() {
        Path2D.Double chemin = new Path2D.Double();
        chemin.moveTo(origine.x, destination.y);
        chemin.lineTo(destination.x, destination.y);
        chemin.lineTo(destination.x, origine.y);
        return chemin;
    }

    public int getLargeur() {
        return destination.x - origine.x;
    }

    public int getLongueur() {
        return destination.y - origine.y;
    }

    /** Le coin du L. */
    @Override
    protected Point calculerPointDeReference() {
        return new Point(destination);
    }
}
