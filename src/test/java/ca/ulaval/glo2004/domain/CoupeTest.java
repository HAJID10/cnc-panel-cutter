package ca.ulaval.glo2004.domain;

import ca.ulaval.glo2004.domain.irregulieres.CoupeBordure;
import ca.ulaval.glo2004.domain.irregulieres.CoupeEnL;
import ca.ulaval.glo2004.domain.irregulieres.CoupeRectangulaire;
import ca.ulaval.glo2004.domain.ordinaire.CoupeHorizontale;
import ca.ulaval.glo2004.domain.ordinaire.CoupeVerticale;
import org.junit.jupiter.api.Test;

import java.awt.Point;
import java.awt.Rectangle;

import static org.junit.jupiter.api.Assertions.*;

class CoupeTest {

    private final Outil scie = new Outil("Scie", 4.0);

    @Test
    void outilObligatoireAvecMessageClair() {
        // Bug d'origine : NullPointerException, car l'outil était utilisé avant d'être vérifié.
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class,
                () -> new CoupeVerticale(new Point(10, 0), new Point(10, 100), null));
        assertTrue(e.getMessage().contains("outil"));
    }

    @Test
    void coupesDroitesValidentLeurOrientation() {
        assertThrows(IllegalArgumentException.class, () -> new CoupeVerticale(new Point(10, 0), new Point(11, 100), scie));
        assertThrows(IllegalArgumentException.class, () -> new CoupeHorizontale(new Point(0, 10), new Point(100, 11), scie));
    }

    @Test
    void zoneCoupeeALaLargeurDeLOutil() {
        // Bug d'origine : la zone faisait deux fois la largeur de l'outil.
        CoupeVerticale coupe = new CoupeVerticale(new Point(50, 0), new Point(50, 100), scie);
        Rectangle zone = coupe.getZoneCoupee().getBounds();
        assertEquals(4, zone.width);
        assertEquals(4.0, coupe.getProfondeurDeCoupe());
    }

    @Test
    void selectionAvecTolerance() {
        CoupeVerticale coupe = new CoupeVerticale(new Point(50, 0), new Point(50, 100), scie);
        assertTrue(coupe.contientPoint(new Point(50, 50)));
        assertTrue(coupe.contientPoint(new Point(54, 50)));   // 2 mm de demi-outil + 3 mm de tolérance
        assertFalse(coupe.contientPoint(new Point(60, 50)));
    }

    @Test
    void coupeEnLSelectionnableSurSesDeuxBras() {
        // Coin en (100, 80) : bras horizontal vers x = 20, bras vertical vers y = 10.
        CoupeEnL coupe = new CoupeEnL(new Point(20, 10), new Point(100, 80), scie);
        assertTrue(coupe.contientPoint(new Point(40, 80)), "bras horizontal");
        assertTrue(coupe.contientPoint(new Point(100, 30)), "bras vertical (perdu dans la version d'origine)");
        assertFalse(coupe.contientPoint(new Point(40, 30)), "à l'intérieur du L");
        assertEquals(new Point(100, 80), coupe.getPointDeReference());
    }

    @Test
    void coupeRectangulaireSelectionnableParLInterieur() {
        CoupeRectangulaire coupe = new CoupeRectangulaire(new Point(100, 100), 200, 50, scie);
        assertTrue(coupe.contientPoint(new Point(200, 125)));
        assertFalse(coupe.contientPoint(new Point(350, 125)));
    }

    @Test
    void deplacerUnRectangleNeLEtirePas() {
        CoupeRectangulaire coupe = new CoupeRectangulaire(new Point(100, 100), 200, 50, scie);
        coupe.setOrigineDeCoupe(new Point(10, 20));
        assertEquals(new Point(210, 70), coupe.getDestinationDeCoupe());
        assertTrue(coupe.contientPoint(new Point(100, 40)));
        assertFalse(coupe.contientPoint(new Point(200, 125)), "la zone a suivi le déplacement");
    }

    @Test
    void bordureSelectionnable() {
        // Bug d'origine : la forme de la bordure valait quatre points en (0, 0).
        CoupeBordure bordure = new CoupeBordure(new Point(10, 10), 500, 300, scie);
        assertTrue(bordure.contientPoint(new Point(10, 150)));
        assertTrue(bordure.contientPoint(new Point(510, 150)));
        assertFalse(bordure.contientPoint(new Point(250, 150)), "le centre du panneau n'est pas coupé");
        assertEquals(new Point(260, 160), bordure.getPointDeReference());
    }

    @Test
    void redimensionnerUneBordureLaGardeCentree() {
        CoupeBordure bordure = new CoupeBordure(new Point(10, 10), 500, 300, scie);
        bordure.redimensionner(300, 100);
        assertEquals(new Point(110, 110), bordure.getOrigineDeCoupe());
        assertEquals(new Point(260, 160), bordure.getPointDeReference());
        assertThrows(IllegalArgumentException.class, () -> bordure.redimensionner(0, 100));
    }

    @Test
    void changerDOutilChangeLaLargeur() {
        CoupeVerticale coupe = new CoupeVerticale(new Point(50, 0), new Point(50, 100), scie);
        coupe.setOutil(new Outil("Fraise", 10.0));
        assertEquals(10.0, coupe.getProfondeurDeCoupe());
        assertEquals(10, coupe.getZoneCoupee().getBounds().width);
    }

    @Test
    void lesPointsNeSontPasPartages() {
        Point origine = new Point(50, 0);
        CoupeVerticale coupe = new CoupeVerticale(origine, new Point(50, 100), scie);
        origine.translate(30, 0);
        coupe.getOrigineDeCoupe().translate(30, 0);
        assertEquals(new Point(50, 0), coupe.getOrigineDeCoupe());
    }
}
