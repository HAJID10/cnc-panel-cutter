package ca.ulaval.glo2004.domain.irregulieres;

import ca.ulaval.glo2004.domain.Coupe;
import ca.ulaval.glo2004.domain.Outil;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/**
 * Coupe rectangulaire : l'outil suit le contour d'un rectangle (coin {@code origine}, de taille largeur × hauteur).
 * Comme à l'affichage, le trajet est décalé d'une demi-largeur d'outil vers l'intérieur.
 */
public class CoupeRectangulaire extends Coupe implements Serializable {

    private int largeur;
    private int hauteur;

    public CoupeRectangulaire(Point coinReference, int largeur, int hauteur, Outil outil) {
        super(coinReference, new Point(coinReference.x + largeur, coinReference.y + hauteur), outil);
        if (largeur <= 0 || hauteur <= 0) {
            throw new IllegalArgumentException("Les dimensions doivent être positives");
        }
        this.largeur = largeur;
        this.hauteur = hauteur;
        this.reference = calculerPointDeReference();
    }

    @Override
    protected Shape trajet() {
        double demiOutil = outil.getLargeur() / 2;
        return new Rectangle2D.Double(origine.x + demiOutil, origine.y + demiOutil,
                Math.max(0, largeur - outil.getLargeur()), Math.max(0, hauteur - outil.getLargeur()));
    }

    @Override
    protected boolean selectionnableParInterieur() {
        return true;
    }

    public int getLargeur() {
        return largeur;
    }

    public int getHauteur() {
        return hauteur;
    }

    public void setLargeur(int largeur) {
        if (largeur <= 0) {
            throw new IllegalArgumentException("La largeur doit être positive");
        }
        this.largeur = largeur;
        setDestinationDeCoupe(new Point(origine.x + largeur, destination.y));
    }

    public void setHauteur(int hauteur) {
        if (hauteur <= 0) {
            throw new IllegalArgumentException("La hauteur doit être positive");
        }
        this.hauteur = hauteur;
        setDestinationDeCoupe(new Point(destination.x, origine.y + hauteur));
    }

    /** Bug corrigé : déplacer l'origine ne déplaçait pas le coin opposé, ce qui étirait le rectangle. */
    @Override
    public void setOrigineDeCoupe(Point origine) {
        super.setOrigineDeCoupe(origine);
        super.setDestinationDeCoupe(new Point(origine.x + largeur, origine.y + hauteur));
    }

    @Override
    protected Point calculerPointDeReference() {
        return new Point(origine);
    }
}
