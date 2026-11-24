package ca.ulaval.glo2004.domain;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.ulaval.glo2004.domain.irregulieres.CoupeBordure;
import ca.ulaval.glo2004.domain.irregulieres.CoupeEnL;
import ca.ulaval.glo2004.domain.irregulieres.CoupeRectangulaire;
import ca.ulaval.glo2004.domain.ordinaire.CoupeHorizontale;
import ca.ulaval.glo2004.domain.ordinaire.CoupeVerticale;

public class Panneau implements Serializable {
    private double longueur; // Longueur en mm
    private double largeur;  // Largeur en mm
    private double epaisseur;
    private List<Point> listePoints = new ArrayList<>();
    private List<Coupe> coupes; // Liste des coupes ajoutées au panneau
    private List<Outil> outils;
    private UUID id;

    // --- CONSTRUCTEUR ---
    // Initialise un panneau avec ses dimensions
    public Panneau(double longueur, double largeur, double epaisseur) {
        if (longueur <= 0 || largeur <= 0 || epaisseur <= 0) {
            throw new IllegalArgumentException("Les dimensions du panneau doivent être positives.");
        }
        this.longueur = longueur;
        this.largeur = largeur;
        this.epaisseur = epaisseur;
        this.coupes = new ArrayList<>();
        this.outils = new ArrayList<>();
        this.id = UUID.randomUUID();
    }

    public void ajouterPoint(Point point) {
        listePoints.add(point);
    }

    public List<Point> getListePoints() {
        return listePoints;
    }

    // --- MÉTHODES POUR LES COUPES ---
    // Ajoute une coupe au panneau si elle est valide
    public void ajouterCoupe(Coupe coupe) {
        if (estCoupeValide(coupe)) {
            coupes.add(coupe);
        } else {
            throw new IllegalArgumentException("Coupe invalide !");
        }
    }

    // Vérifie si une coupe est valide par rapport aux dimensions du panneau
    public void ajouterOutil(Outil outil) {
        if (outil == null) {
            throw new IllegalArgumentException("Outil ne peut pas être null.");
        }
        if (trouverOutilParId(outil.getId()).isEmpty()) {
            outils.add(outil);
        } else {
            throw new IllegalArgumentException("Un outil avec cet ID existe déjà.");
        }
    }

    public Optional<Outil> trouverOutilParId(UUID idOutil) {
        return outils.stream().filter(outil -> outil.getId().equals(idOutil)).findFirst();
    }

    public void ajouterCoupeVerticale(int x) {
        Point origine = new Point(x, 0);
        Point destination = new Point(x, (int) longueur);

        CoupeVerticale coupe = new CoupeVerticale(origine, destination, outilPourNouvelleCoupe());
        ajouterCoupe(coupe);
    }

    public void ajouterCoupeHorizontale(int y) {
        Point origine = new Point(0, y);
        Point destination = new Point((int) largeur, y);

        CoupeHorizontale coupe = new CoupeHorizontale(origine, destination, outilPourNouvelleCoupe());
        ajouterCoupe(coupe);
    }

    public void ajouterCoupeRectangulaire(Point origine, Point destination) {
        int largeur = Math.abs(destination.x - origine.x);
        int hauteur = Math.abs(destination.y - origine.y);

        if (largeur <= 0 || hauteur <= 0) {
            throw new IllegalArgumentException("Les dimensions doivent être positives");
        }

        CoupeRectangulaire coupe = new CoupeRectangulaire(new Point(Math.min(origine.x, destination.x), Math.min(origine.y, destination.y)), largeur, hauteur, outilPourNouvelleCoupe());
        ajouterCoupe(coupe);
    }

    public void ajouterCoupeEnL(Point origine, Point destination) {
        CoupeEnL coupe = new CoupeEnL(origine, destination, outilPourNouvelleCoupe());
        ajouterCoupe(coupe);
    }

    public void redimensionnerCoupeEnL(CoupeEnL coupe, Point nouvelleDestination) {
        if (!estPositionValide(nouvelleDestination)) {
            throw new IllegalArgumentException("La nouvelle destination est hors limites du panneau");
        }
        coupe.setDestinationDeCoupe(nouvelleDestination);
    }

    public void ajouterCoupeBordure(int largeurFinale, int longueurFinale) {
        int decalageX = (int) ((largeur - largeurFinale) / 2);
        int decalageY = (int) ((longueur - longueurFinale) / 2);
        Point origine = new Point(decalageX, decalageY);
        var coupe = new CoupeBordure(origine, largeurFinale, longueurFinale, outilPourNouvelleCoupe());
        ajouterCoupe(coupe);
    }

    public void redimensionnerCoupeRectangulaire(CoupeRectangulaire coupe, int nouvelleLargeur, int nouvelleLongueur) {
        if (nouvelleLargeur <= 0 || nouvelleLongueur <= 0) {
            throw new IllegalArgumentException("Les dimensions doivent être positives");
        }

        Point origine = coupe.getOrigineDeCoupe();
        Point nouvelleDestination = new Point(origine.x + nouvelleLargeur,
                origine.y + nouvelleLongueur
        );

        // Vérifier si la nouvelle taille est valide
        if (!estPositionValide(nouvelleDestination)) {
            throw new IllegalArgumentException("Les nouvelles dimensions dépassent les limites du panneau");
        }
        coupe.setLargeur(nouvelleLargeur);
        coupe.setHauteur(nouvelleLongueur);
    }

    public void redimensionnerCoupeBordure(CoupeBordure coupe, int nouvelleLargeur, int nouvelleLongueur) {
        if (nouvelleLargeur <= 0 || nouvelleLongueur <= 0) {
            throw new IllegalArgumentException("Les dimensions doivent être positives");
        }
        if (nouvelleLargeur > largeur || nouvelleLongueur > longueur) {
            throw new IllegalArgumentException("Les nouvelles dimensions dépassent les limites du panneau");
        }
        // Bug corrigé : la bordure n'était plus centrée après un redimensionnement.
        coupe.redimensionner(nouvelleLargeur, nouvelleLongueur);
        Point origine = new Point((int) ((largeur - nouvelleLargeur) / 2), (int) ((longueur - nouvelleLongueur) / 2));
        coupe.setOrigineDeCoupe(origine);
        coupe.setDestinationDeCoupe(new Point(origine.x + nouvelleLargeur, origine.y + nouvelleLongueur));
    }

    public void deplacerCoupe(Coupe coupe, Point nouvelleOrigine) {
        if (coupe instanceof CoupeVerticale) {
            deplacerCoupeVerticale((CoupeVerticale) coupe, nouvelleOrigine);
        } else if (coupe instanceof CoupeHorizontale) {
            deplacerCoupeHorizontale((CoupeHorizontale) coupe, nouvelleOrigine);
        } else if (coupe instanceof CoupeRectangulaire) {
            deplacerCoupeRectangulaire((CoupeRectangulaire) coupe, nouvelleOrigine);
        } else if (coupe instanceof CoupeEnL) {
            deplacerCoupeEnL((CoupeEnL) coupe, nouvelleOrigine);
        }
        // Une coupe de bordure reste toujours centrée : elle ne se déplace pas.
    }

    private void deplacerCoupeEnL(CoupeEnL coupe, Point nouvelleOrigine) {
        Point origine = coupe.getOrigineDeCoupe();
        Point destination = coupe.getDestinationDeCoupe();
        int dx = nouvelleOrigine.x - origine.x;
        int dy = nouvelleOrigine.y - origine.y;
        if (!estPositionValide(nouvelleOrigine) || !estPositionValide(new Point(destination.x + dx, destination.y + dy))) {
            throw new IllegalArgumentException("La coupe dépasserait les limites du panneau");
        }
        coupe.translater(dx, dy);
    }

    private void deplacerCoupeVerticale(CoupeVerticale coupe, Point nouvelleOrigine) {
        if (!estPositionValide(nouvelleOrigine)) {
            throw new IllegalArgumentException("Position hors limites du panneau");
        }
        Point nouvelleDestination = new Point(nouvelleOrigine.x, (int) longueur);
        coupe.setOrigineDeCoupe(nouvelleOrigine);
        coupe.setDestinationDeCoupe(nouvelleDestination);
    }

    private void deplacerCoupeHorizontale(CoupeHorizontale coupe, Point nouvelleOrigine) {
        if (!estPositionValide(nouvelleOrigine)) {
            throw new IllegalArgumentException("Position hors limites du panneau");
        }

        Point nouvelleDestination = new Point((int) largeur, nouvelleOrigine.y);
        coupe.setOrigineDeCoupe(nouvelleOrigine);
        coupe.setDestinationDeCoupe(nouvelleDestination);
    }

    private void deplacerCoupeRectangulaire(CoupeRectangulaire coupe, Point nouvelleOrigine) {
        if (!estPositionValide(nouvelleOrigine)) {
            throw new IllegalArgumentException("Position hors limites du panneau");
        }

        Point nouvelleDestination = new Point(
                nouvelleOrigine.x + coupe.getLargeur(),
                nouvelleOrigine.y + coupe.getHauteur()
        );

        if (!estPositionValide(nouvelleDestination)) {
            throw new IllegalArgumentException("La coupe dépasserait les limites du panneau");
        }

        coupe.setOrigineDeCoupe(nouvelleOrigine);
    }

    /** Outil à utiliser pour une nouvelle coupe. */
    private Outil outilPourNouvelleCoupe() {
        Outil outil = getOutilParDefaut();
        if (outil == null) {
            throw new IllegalArgumentException("Aucun outil par défaut : choisissez un outil avant d'ajouter une coupe.");
        }
        return outil;
    }

    /** Change la largeur d'un outil; les coupes qui l'utilisent sont mises à jour. */
    public void modifierLargeurOutil(UUID idOutil, double nouvelleLargeur) {
        if (nouvelleLargeur <= 0) {
            throw new IllegalArgumentException("La largeur de l'outil doit être positive.");
        }
        trouverOutilParId(idOutil).ifPresent(outil -> {
            outil.setLargeur(nouvelleLargeur);
            coupes.stream().filter(c -> c.getOutilDeCoupe() == outil).forEach(Coupe::outilModifie);
        });
    }

    public Outil getOutilParDefaut() {
        return outils.stream()
                .filter(Outil::estParDefault)
                .findFirst()
                .orElse(null);
    }

    public void definirOutilParDefaut(UUID idOutil) {
        outils.forEach(outil -> {
            if (outil.getId().equals(idOutil)) {
                outil.activerOutilParDefaut();
            } else {
                outil.desactiverOutilParDefaut();
            }
        });
    }

    public Optional<Outil> trouverOutilParNom(String nom) {
        return outils.stream()
                .filter(outil -> outil.getNomOutil().equals(nom))
                .findFirst();
    }

    public void supprimerOutil(UUID id) {
        outils.removeIf(outil -> outil.getId().equals(id));
    }

    public Optional<Coupe> trouverCoupeParId(UUID idCoupe) {
        return coupes.stream()
                .filter(coupe -> coupe.getId().equals(idCoupe))
                .findFirst();
    }

    public void supprimerCoupe(Coupe coupe) {
        coupes.remove(coupe);
    }

    private boolean estCoupeValide(Coupe coupe) {
        return estPositionValide(coupe.getOrigineDeCoupe()) &&
                estPositionValide(coupe.getDestinationDeCoupe());
    }

    private boolean estPositionValide(Point point) {
        return point.x >= 0 && point.x <= largeur &&
                point.y >= 0 && point.y <= longueur;
    }

    public void selectionnerCoupe(UUID idCoupe) {
        deselectionnerToutesLesCoupes();
        trouverCoupeParId(idCoupe).ifPresent(coupe -> coupe.setEstSelectionne(true));
    }

    public void deselectionnerToutesLesCoupes() {
        coupes.forEach(coupe -> coupe.setEstSelectionne(false));
    }

    public Optional<Coupe> getCoupeSelectionnee() {
        return coupes.stream().filter(Coupe::estSelectionne).findFirst();
    }

    public void deplacerCoupeParId(UUID idCoupe, Point nouvelleOrigine) {
      coupes.stream()
            .filter(c -> c.getId().equals(idCoupe))
            .findFirst().ifPresent(coupe -> deplacerCoupe(coupe, nouvelleOrigine));

    }

    public void supprimerCoupeParId(UUID idCoupe) {
        coupes.removeIf(coupe -> coupe.getId().equals(idCoupe));
    }

    public UUID getId() {
        return id;
    }

    public double getLongueur() {

        return longueur;
    }

    public double getLargeur() {

        return largeur;
    }

    public double getEpaisseur() {
        return epaisseur;
    }

    public List<Outil> getOutils() {
        return Collections.unmodifiableList(outils);
    }

    public List<Coupe> getCoupes() {
        return Collections.unmodifiableList(coupes);
    }

    public void modifierOutilCoupe(UUID idCoupe, String nomOutil) {
        trouverOutilParNom(nomOutil).ifPresent(outil -> coupes.stream()
                                                          .filter(coupe -> coupe.getId().equals(idCoupe))
                                                          .findFirst()
                                                          .ifPresent(coupe -> coupe.setOutil(outil)));
    }

    public void modifierCoupeBordure(Coupe coupeBordure, int nouvelleLargeur, int nouvelleLongueur) {
        coupes.stream()
                .filter(coupe -> coupe.getId().equals(coupeBordure.getId()))
                .findFirst()
                .ifPresent(coupe -> {
                    if (coupe instanceof CoupeBordure trouvee) {
                        // Bug corrigé : la copie reçue en paramètre était modifiée au lieu de la coupe du panneau.
                        redimensionnerCoupeBordure(trouvee, nouvelleLargeur, nouvelleLongueur);
                    }
                });
    }
    public void modifierCoupeRectangulaire(Coupe coupeRectangulaire, int nouvelleLargeur, int nouvelleLongueur) {
        coupes.stream()
              .filter(coupe -> coupe.getId().equals(coupeRectangulaire.getId()))
              .findFirst()
              .ifPresent(coupeFound -> {
                  if (coupeFound instanceof CoupeRectangulaire) {
                      redimensionnerCoupeRectangulaire((CoupeRectangulaire) coupeFound, nouvelleLargeur, nouvelleLongueur);
                  }
              });
    }
}