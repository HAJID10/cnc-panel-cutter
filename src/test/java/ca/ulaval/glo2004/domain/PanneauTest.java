package ca.ulaval.glo2004.domain;

import ca.ulaval.glo2004.domain.irregulieres.CoupeBordure;
import ca.ulaval.glo2004.domain.irregulieres.CoupeEnL;
import ca.ulaval.glo2004.domain.irregulieres.CoupeRectangulaire;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.Point;

import static org.junit.jupiter.api.Assertions.*;

class PanneauTest {

    private Panneau panneau;
    private Outil scie;

    @BeforeEach
    void creer() {
        // longueur (axe y) 1200 mm, largeur (axe x) 900 mm
        panneau = new Panneau(1200, 900, 18);
        scie = new Outil("Scie", 4.0);
        panneau.ajouterOutil(scie);
        panneau.definirOutilParDefaut(scie.getId());
    }

    @Test
    void dimensionsValidees() {
        assertThrows(IllegalArgumentException.class, () -> new Panneau(0, 900, 18));
        assertThrows(IllegalArgumentException.class, () -> new Panneau(1200, -1, 18));
    }

    @Test
    void coupesDroitesTraversentLePanneau() {
        panneau.ajouterCoupeVerticale(300);
        panneau.ajouterCoupeHorizontale(400);
        assertEquals(2, panneau.getCoupes().size());
        assertEquals(new Point(300, 1200), panneau.getCoupes().get(0).getDestinationDeCoupe());
        assertEquals(new Point(900, 400), panneau.getCoupes().get(1).getDestinationDeCoupe());
        assertThrows(IllegalArgumentException.class, () -> panneau.ajouterCoupeVerticale(950));
    }

    @Test
    void sansOutilParDefautMessageClair() {
        Panneau vide = new Panneau(100, 100, 10);
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> vide.ajouterCoupeVerticale(10));
        assertTrue(e.getMessage().contains("outil"));
    }

    @Test
    void rectangleTraceDansNimporteQuelSens() {
        panneau.ajouterCoupeRectangulaire(new Point(300, 300), new Point(100, 200));
        CoupeRectangulaire r = (CoupeRectangulaire) panneau.getCoupes().get(0);
        assertEquals(new Point(100, 200), r.getOrigineDeCoupe());
        assertEquals(200, r.getLargeur());
        assertEquals(100, r.getHauteur());
    }

    @Test
    void deplacerUneCoupe() {
        panneau.ajouterCoupeVerticale(300);
        Coupe coupe = panneau.getCoupes().get(0);
        panneau.deplacerCoupeParId(coupe.getId(), new Point(500, 0));
        assertEquals(500, coupe.getOrigineDeCoupe().x);
        assertTrue(coupe.contientPoint(new Point(500, 600)));
        assertFalse(coupe.contientPoint(new Point(300, 600)));
        assertThrows(IllegalArgumentException.class, () -> panneau.deplacerCoupe(coupe, new Point(1000, 0)));
    }

    @Test
    void deplacerUneCoupeEnL() {
        panneau.ajouterCoupeEnL(new Point(100, 100), new Point(300, 400));
        CoupeEnL l = (CoupeEnL) panneau.getCoupes().get(0);
        panneau.deplacerCoupe(l, new Point(200, 150));
        assertEquals(new Point(400, 450), l.getDestinationDeCoupe());
        assertThrows(IllegalArgumentException.class, () -> panneau.deplacerCoupe(l, new Point(800, 100)));
    }

    @Test
    void redimensionnerUnRectangle() {
        panneau.ajouterCoupeRectangulaire(new Point(100, 100), new Point(300, 200));
        CoupeRectangulaire r = (CoupeRectangulaire) panneau.getCoupes().get(0);
        panneau.modifierCoupeRectangulaire(r, 400, 150);
        assertEquals(new Point(500, 250), r.getDestinationDeCoupe());
        assertThrows(IllegalArgumentException.class, () -> panneau.modifierCoupeRectangulaire(r, 900, 150));
    }

    @Test
    void bordureCentreeEtRedimensionnable() {
        panneau.ajouterCoupeBordure(800, 1000);
        CoupeBordure b = (CoupeBordure) panneau.getCoupes().get(0);
        assertEquals(new Point(50, 100), b.getOrigineDeCoupe());
        panneau.modifierCoupeBordure(b, 600, 800);
        assertEquals(new Point(150, 200), b.getOrigineDeCoupe());
        assertThrows(IllegalArgumentException.class, () -> panneau.modifierCoupeBordure(b, 1000, 800));
    }

    @Test
    void modifierLaLargeurDUnOutilMetSesCoupesAJour() {
        panneau.ajouterCoupeVerticale(300);
        panneau.modifierLargeurOutil(scie.getId(), 12.0);
        Coupe coupe = panneau.getCoupes().get(0);
        assertEquals(12.0, coupe.getProfondeurDeCoupe());
        assertEquals(12, coupe.getZoneCoupee().getBounds().width);
        assertThrows(IllegalArgumentException.class, () -> panneau.modifierLargeurOutil(scie.getId(), 0));
    }

    @Test
    void selectionUnique() {
        panneau.ajouterCoupeVerticale(100);
        panneau.ajouterCoupeVerticale(200);
        Coupe premiere = panneau.getCoupes().get(0);
        Coupe deuxieme = panneau.getCoupes().get(1);
        panneau.selectionnerCoupe(premiere.getId());
        panneau.selectionnerCoupe(deuxieme.getId());
        assertFalse(premiere.estSelectionne());
        assertEquals(deuxieme, panneau.getCoupeSelectionnee().orElseThrow());
    }

    @Test
    void listeDesCoupesNonModifiable() {
        panneau.ajouterCoupeVerticale(100);
        assertThrows(UnsupportedOperationException.class, () -> panneau.getCoupes().clear());
    }

    @Test
    void outilParDefautUnique() {
        Outil fraise = new Outil("Fraise", 10.0);
        panneau.ajouterOutil(fraise);
        panneau.definirOutilParDefaut(fraise.getId());
        assertEquals(fraise, panneau.getOutilParDefaut());
        assertFalse(scie.estParDefault());
        assertThrows(IllegalArgumentException.class, () -> panneau.ajouterOutil(fraise));
    }
}
