package ca.ulaval.glo2004.controller;

import java.awt.Point;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import ca.ulaval.glo2004.domain.Panneau;
import ca.ulaval.glo2004.dto.CoupeDTO;
import ca.ulaval.glo2004.dto.OutilDTO;
import ca.ulaval.glo2004.dto.PanneauDTO;
import ca.ulaval.glo2004.dto.assembleurs.CoupeAssembler;
import ca.ulaval.glo2004.dto.assembleurs.OutilAssembler;
import ca.ulaval.glo2004.dto.assembleurs.PanneauAssembler;
import ca.ulaval.glo2004.infra.UndoRedoManager;

public class CNC_Controller {
  private final PanneauAssembler panneauAssembler;
  private final OutilAssembler outilAssembler;
  private final CoupeAssembler coupeAssembler;
  private final UndoRedoManager undoRedoManager;
  private Panneau panneau;

  public CNC_Controller(PanneauDTO panneauDTO) {
    this.outilAssembler = new OutilAssembler();
    this.coupeAssembler = new CoupeAssembler(outilAssembler);
    this.panneauAssembler = new PanneauAssembler(outilAssembler, coupeAssembler);
    this.undoRedoManager = new UndoRedoManager();
    this.panneau = panneauAssembler.creerPanneau(panneauDTO);
  }

  private void saveStateForUndo() {
    undoRedoManager.saveState(getPanneauDTO());
  }

  public void undo() {
    PanneauDTO previousState = undoRedoManager.undo(getPanneauDTO());
    if (previousState != null) {
      this.panneau = panneauAssembler.creerPanneau(previousState);
    }
  }

  public void redo() {
    PanneauDTO nextState = undoRedoManager.redo(getPanneauDTO());
    if (nextState != null) {
      this.panneau = panneauAssembler.creerPanneau(nextState);
    }
  }

  public void savePanneau(String filePath) throws IOException {
    undoRedoManager.saveToFile(getPanneauDTO(), filePath);
  }

  public void loadPanneau(String filePath) throws IOException, ClassNotFoundException {
    var loadedPanneau = undoRedoManager.loadFromFile(filePath);
    this.panneau = panneauAssembler.creerPanneau(loadedPanneau);
  }

  public PanneauDTO getPanneauDTO() {
    return panneauAssembler.creerPanneauDTO(panneau);
  }

  public void ajouterCoupeVerticale(Point start) {
    saveStateForUndo();
    panneau.ajouterCoupeVerticale(start.x);
  }

  public void ajouterCoupeHorizontale(Point start) {
    saveStateForUndo();
    panneau.ajouterCoupeHorizontale(start.y);
  }

  public void ajouterCoupeRectangulaire(Point origine, Point destination) {
    saveStateForUndo();
    panneau.ajouterCoupeRectangulaire(origine, destination);
  }

  public void deplacerCoupe(CoupeDTO coupeDTO, Point nouvelleOrigine) {
    // Bug corrigé : une copie de la coupe était déplacée au lieu de la coupe du panneau.
    deplacerCoupeParId(coupeDTO.id(), nouvelleOrigine);
  }

  public void ajouterOutil(OutilDTO outilDTO) {
    saveStateForUndo();
    panneau.ajouterOutil(outilAssembler.creerOutil(outilDTO));
  }

  public void definirOutilParDefaut(UUID idOutil) {
    saveStateForUndo();
    panneau.definirOutilParDefaut(idOutil);
  }

  public OutilDTO getDefaultOutil() {
    return outilAssembler.creerOutilDTO(panneau.getOutilParDefaut());
  }

  public void modifierLargeurOutil(UUID idOutil, double largeur) {
    saveStateForUndo();
    panneau.modifierLargeurOutil(idOutil, largeur);
  }

  public void supprimerCoupeParId(UUID idCoupe) {
    saveStateForUndo();
    panneau.supprimerCoupeParId(idCoupe);
  }

  public Optional<OutilDTO> trouverOutilParNom(String nom) {
    return panneau.trouverOutilParNom(nom).map(outilAssembler::creerOutilDTO);
  }

  public void supprimerOutil(UUID id) {
    saveStateForUndo();
    panneau.supprimerOutil(id);
  }

  public void ajouterCoupeL(Point point1, Point point2) {
    saveStateForUndo();
    panneau.ajouterCoupeEnL(point1, point2);
    
  }

  public void ajouterCoupeBordure(int largeur, int longueur) {
    // Bug corrigé : les paramètres étaient inversés par rapport à l'appel de l'interface (largeur, longueur).
    saveStateForUndo();
    panneau.ajouterCoupeBordure(largeur, longueur);
  }

  public void creerNouveauPanneau(PanneauDTO dto) {
    saveStateForUndo();
    this.panneau = panneauAssembler.creerPanneau(dto);
  }

  public Optional<CoupeDTO> contientCoupe(Point point) {
    return this.panneau.getCoupes()
                       .stream()
                       .filter(coupe -> coupe.contientPoint(point))
                       .findFirst()
                       .map(coupeAssembler::creerCoupeDTO);
  }

  // La sélection ne fait pas partie de l'historique : « Annuler » n'annule que des modifications.
  public void selectionnerCoupe(UUID idCoupe) {
    panneau.selectionnerCoupe(idCoupe);
  }

  public void deselectionnerToutesLesCoupes() {
    panneau.deselectionnerToutesLesCoupes();
  }
  public Optional<CoupeDTO> getCoupeSelectionnee() {
    return panneau.getCoupeSelectionnee().map(coupeAssembler::creerCoupeDTO);
  }

  public void deplacerCoupeParId(UUID idCoupe, Point nouvelleOrigine) {
    saveStateForUndo();
    panneau.deplacerCoupeParId(idCoupe, nouvelleOrigine);
    
  }

  public void modifierOutilCoupe(UUID idCoupe, String nomOutil) {
    saveStateForUndo();
    panneau.modifierOutilCoupe(idCoupe, nomOutil);
  }
  public void redimensionnerCoupeBordure(CoupeDTO coupeDTO, int nouvelleLargeur, int nouvelleLongueur) {
    saveStateForUndo();
    panneau.trouverCoupeParId(coupeDTO.id())
           .ifPresent(coupe -> panneau.modifierCoupeBordure(coupe, nouvelleLargeur, nouvelleLongueur));
  }

  public void redimensionnerCoupeRectangulaire(CoupeDTO coupeDTO, int nouvelleLargeur, int nouvelleLongueur) {
    saveStateForUndo();
    panneau.trouverCoupeParId(coupeDTO.id())
           .ifPresent(coupe -> panneau.modifierCoupeRectangulaire(coupe, nouvelleLargeur, nouvelleLongueur));
  }
}
