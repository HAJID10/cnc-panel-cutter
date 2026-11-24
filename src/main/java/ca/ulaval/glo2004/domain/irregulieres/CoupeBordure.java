package ca.ulaval.glo2004.domain.irregulieres;

import ca.ulaval.glo2004.domain.Coupe;
import ca.ulaval.glo2004.domain.Outil;

import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;

/** Coupe de bordure : ramène le panneau à largeur × longueur en coupant tout autour (rectangle centré). */
public class CoupeBordure extends Coupe implements Serializable {

    private int largeur;
    private int longueur;

    public CoupeBordure(Point origine, int largeur, int longueur, Outil outil) {
        super(origine, new Point(origine.x + largeur, origine.y + longueur), outil);
        verifier(largeur, longueur);
        this.largeur = largeur;
        this.longueur = longueur;
        this.reference = calculerPointDeReference();
    }

    private static void verifier(int largeur, int longueur) {
        if (largeur <= 0 || longueur <= 0) {
            throw new IllegalArgumentException("Les dimensions doivent être positives");
        }
    }

    /** Bug corrigé : la forme valait quatre points en (0, 0), la bordure ne pouvait donc jamais être sélectionnée. */
    @Override
    protected Shape trajet() {
        return new Rectangle2D.Double(origine.x, origine.y, largeur, longueur);
    }

    /** Redimensionne la bordure en la gardant centrée au même endroit. */
    public void redimensionner(int nouvelleLargeur, int nouvelleLongueur) {
        verifier(nouvelleLargeur, nouvelleLongueur);
        Point centre = calculerPointDeReference();
        this.largeur = nouvelleLargeur;
        this.longueur = nouvelleLongueur;
        setOrigineDeCoupe(new Point(centre.x - nouvelleLargeur / 2, centre.y - nouvelleLongueur / 2));
        setDestinationDeCoupe(new Point(origine.x + nouvelleLargeur, origine.y + nouvelleLongueur));
    }

    @Override
    protected Point calculerPointDeReference() {
        return new Point(origine.x + largeur / 2, origine.y + longueur / 2);
    }

    public int getLargeur() {
        return largeur;
    }

    public int getLongueur() {
        return longueur;
    }

    public void setLargeur(int largeur) {
        redimensionner(largeur, longueur);
    }

    public void setLongueur(int longueur) {
        redimensionner(largeur, longueur);
    }
}
