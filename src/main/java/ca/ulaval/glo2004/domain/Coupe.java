package ca.ulaval.glo2004.domain;

import java.awt.BasicStroke;
import java.awt.Point;
import java.awt.Shape;
import java.io.Serializable;
import java.util.UUID;

/**
 * Coupe sur un panneau. Chaque type de coupe décrit son <em>trajet</em> (la ligne suivie par le centre de l'outil);
 * la zone réellement coupée est ce trajet épaissi de la largeur de l'outil.
 */
public abstract class Coupe implements Serializable {

    /** Marge (en mm) autour d'une coupe à l'intérieur de laquelle un clic la sélectionne. */
    public static final double TOLERANCE_SELECTION = 3.0;

    protected UUID id;
    protected Point origine;
    protected Point destination;
    protected Point reference;
    protected double profondeur;
    protected Outil outil;
    protected boolean estSelectionne;

    protected Coupe(Point origine, Point destination, Outil outil) {
        // Bug corrigé : l'outil était utilisé avant d'être vérifié (NullPointerException au lieu d'un message clair).
        if (origine == null || destination == null) {
            throw new IllegalArgumentException("Les points d'origine et de destination ne peuvent pas être null.");
        }
        if (outil == null) {
            throw new IllegalArgumentException("L'outil ne peut pas être null.");
        }
        this.origine = new Point(origine);
        this.destination = new Point(destination);
        this.outil = outil;
        this.profondeur = outil.getLargeur();
        this.id = UUID.randomUUID();
        this.estSelectionne = false;
        this.reference = calculerPointDeReference();
    }

    /** Ligne suivie par le centre de l'outil. */
    protected abstract Shape trajet();

    /** Vrai si un clic à l'intérieur du trajet fermé sélectionne aussi la coupe (coupes rectangulaires). */
    protected boolean selectionnableParInterieur() {
        return false;
    }

    protected abstract Point calculerPointDeReference();

    /** Zone retirée par l'outil. */
    public Shape getZoneCoupee() {
        return epaissir(outil.getLargeur());
    }

    private Shape epaissir(double largeur) {
        return new BasicStroke((float) largeur, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER).createStrokedShape(trajet());
    }

    /** Vrai si le point touche la coupe (avec une petite tolérance pour faciliter la sélection). */
    public boolean contientPoint(Point p) {
        return estProcheDuPoint(p, TOLERANCE_SELECTION);
    }

    /** Vrai si le point est à moins de {@code distance} mm de la zone coupée. */
    public boolean estProcheDuPoint(Point p, double distance) {
        return epaissir(outil.getLargeur() + 2 * distance).contains(p)
                || (selectionnableParInterieur() && trajet().contains(p));
    }

    // --- Accès ---

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Point getOrigineDeCoupe() {
        return new Point(origine);
    }

    public void setOrigineDeCoupe(Point origine) {
        this.origine = new Point(origine);
        this.reference = calculerPointDeReference();
    }

    public Point getDestinationDeCoupe() {
        return new Point(destination);
    }

    public void setDestinationDeCoupe(Point destination) {
        this.destination = new Point(destination);
        this.reference = calculerPointDeReference();
    }

    public Point getPointDeReference() {
        return reference == null ? null : new Point(reference);
    }

    public double getProfondeurDeCoupe() {
        return profondeur;
    }

    public Outil getOutilDeCoupe() {
        return outil;
    }

    /** Change l'outil; la largeur de coupe suit (bug corrigé : elle restait celle de l'ancien outil). */
    public void setOutil(Outil outil) {
        if (outil == null) {
            throw new IllegalArgumentException("L'outil ne peut pas être null.");
        }
        this.outil = outil;
        this.profondeur = outil.getLargeur();
    }

    /** À appeler quand la largeur de l'outil de cette coupe a changé. */
    public void outilModifie() {
        this.profondeur = outil.getLargeur();
    }

    public boolean estSelectionne() {
        return estSelectionne;
    }

    public void setEstSelectionne(boolean estSelectionne) {
        this.estSelectionne = estSelectionne;
    }

    /** Déplace la coupe de (dx, dy) sans changer sa forme. */
    public void translater(int dx, int dy) {
        origine.translate(dx, dy);
        destination.translate(dx, dy);
        this.reference = calculerPointDeReference();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + origine.x + "," + origine.y + " -> " + destination.x + ","
                + destination.y + ", outil=" + outil.getNomOutil() + "]";
    }
}
