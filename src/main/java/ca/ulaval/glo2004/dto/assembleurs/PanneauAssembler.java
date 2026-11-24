package ca.ulaval.glo2004.dto.assembleurs;

import java.util.List;

import ca.ulaval.glo2004.domain.Coupe;
import ca.ulaval.glo2004.domain.Outil;
import ca.ulaval.glo2004.domain.Panneau;
import ca.ulaval.glo2004.dto.CoupeDTO;
import ca.ulaval.glo2004.dto.OutilDTO;
import ca.ulaval.glo2004.dto.PanneauDTO;

public class PanneauAssembler {
  private final OutilAssembler outilAssembler;
  private final CoupeAssembler coupeAssembler;

  public PanneauAssembler(OutilAssembler outilAssembler, CoupeAssembler coupeAssembler) {
    this.outilAssembler = outilAssembler;
    this.coupeAssembler = coupeAssembler;
  }

  public Panneau creerPanneau(PanneauDTO dto) {
    Panneau panneau = new Panneau(
      dto.longueurPanneau(),
      dto.largeurPanneau(),
      dto.epaisseurPanneau()
    );

    // Assemblage et ajout des outils
    for (OutilDTO outilDTO : dto.outilsPanneauDTO()) {
      Outil outil = outilAssembler.creerOutil(outilDTO);
      panneau.ajouterOutil(outil);
    }

    // Assemblage et ajout des coupes
    for (CoupeDTO coupeDTO : dto.coupesPanneauDTO()) {
      Coupe coupe = coupeAssembler.creerCoupe(coupeDTO);
      // Les coupes partagent les outils du panneau : changer la largeur d'un outil met ses coupes à jour.
      panneau.trouverOutilParId(coupeDTO.outilDTO().id()).ifPresent(coupe::setOutil);
      panneau.ajouterCoupe(coupe);
    }

    return panneau;
  }

  public PanneauDTO creerPanneauDTO(Panneau panneau) {
    List<CoupeDTO> coupesDTO = panneau.getCoupes().stream()
                                      .map(coupeAssembler::creerCoupeDTO)
                                      .toList();

    List<OutilDTO> outilsDTO = panneau.getOutils().stream()
                                      .map(outilAssembler::creerOutilDTO)
                                      .toList();

    return new PanneauDTO(
      panneau.getId(),
      panneau.getLongueur(),
      panneau.getLargeur(),
      panneau.getEpaisseur(),
      coupesDTO,
      outilsDTO
    );
  }
}