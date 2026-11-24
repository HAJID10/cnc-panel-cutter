package ca.ulaval.glo2004.controller;

import ca.ulaval.glo2004.dto.CoupeDTO;
import ca.ulaval.glo2004.dto.OutilDTO;
import ca.ulaval.glo2004.dto.PanneauDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.awt.Point;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CNCControllerTest {

    private CNC_Controller controller;
    private OutilDTO scie;

    @BeforeEach
    void creer() {
        scie = new OutilDTO(UUID.randomUUID(), "Scie", 4.0, true);
        controller = new CNC_Controller(new PanneauDTO(UUID.randomUUID(), 1200, 900, 18, new ArrayList<>(), List.of(scie)));
    }

    private List<CoupeDTO> coupes() {
        return controller.getPanneauDTO().coupesPanneauDTO();
    }

    @Test
    void annulerEtRetablir() {
        controller.ajouterCoupeVerticale(new Point(100, 0));
        controller.ajouterCoupeVerticale(new Point(200, 0));
        UUID id = coupes().get(0).id();

        controller.undo();
        assertEquals(1, coupes().size());
        // Bug d'origine : les identifiants changeaient à chaque annulation.
        assertEquals(id, coupes().get(0).id());

        controller.redo();
        assertEquals(2, coupes().size());
        // Bug d'origine : la première annulation après un rétablissement ne faisait rien.
        controller.undo();
        assertEquals(1, coupes().size());
        controller.undo();
        assertEquals(0, coupes().size());
    }

    @Test
    void laSelectionNeRemplitPasLHistorique() {
        controller.ajouterCoupeVerticale(new Point(100, 0));
        UUID id = coupes().get(0).id();
        controller.selectionnerCoupe(id);
        controller.deselectionnerToutesLesCoupes();
        controller.selectionnerCoupe(id);
        controller.undo(); // annule l'ajout, pas la sélection
        assertTrue(coupes().isEmpty());
    }

    @Test
    void deplacerAvecUnDTO() {
        controller.ajouterCoupeVerticale(new Point(100, 0));
        // Bug d'origine : c'est une copie de la coupe qui était déplacée.
        controller.deplacerCoupe(coupes().get(0), new Point(400, 0));
        assertEquals(400, coupes().get(0).origine().x);
    }

    @Test
    void redimensionnerAvecUnDTO() {
        controller.ajouterCoupeBordure(800, 1000); // largeur, longueur
        controller.redimensionnerCoupeBordure(coupes().get(0), 600, 800);
        assertEquals(600, coupes().get(0).largeur());

        controller.ajouterCoupeRectangulaire(new Point(100, 100), new Point(200, 200));
        CoupeDTO rectangle = coupes().get(1);
        controller.redimensionnerCoupeRectangulaire(rectangle, 300, 50);
        assertEquals(300, coupes().get(1).largeur());
        assertEquals(50, coupes().get(1).longueur());
    }

    @Test
    void selectionnerAuClic() {
        controller.ajouterCoupeHorizontale(new Point(0, 500));
        assertTrue(controller.contientCoupe(new Point(300, 501)).isPresent());
        assertTrue(controller.contientCoupe(new Point(300, 600)).isEmpty());
    }

    @Test
    void largeurDOutilAppliqueeAuxCoupes() {
        controller.ajouterCoupeVerticale(new Point(100, 0));
        controller.modifierLargeurOutil(scie.id(), 8.0);
        assertEquals(8.0, coupes().get(0).profondeur());
        assertEquals(8.0, coupes().get(0).outilDTO().largeur());
    }

    @Test
    void enregistrerEtRecharger(@TempDir Path dossier) throws Exception {
        controller.ajouterCoupeVerticale(new Point(100, 0));
        controller.ajouterCoupeL(new Point(100, 100), new Point(300, 300));
        List<CoupeDTO> avant = coupes();
        String fichier = dossier.resolve("panneau.cnc").toString();
        controller.savePanneau(fichier);

        controller.creerNouveauPanneau(new PanneauDTO(UUID.randomUUID(), 500, 500, 10, new ArrayList<>(), List.of(scie)));
        assertTrue(coupes().isEmpty());

        controller.loadPanneau(fichier);
        assertEquals(avant, coupes());
        assertEquals(1200, controller.getPanneauDTO().longueurPanneau());
    }
}
