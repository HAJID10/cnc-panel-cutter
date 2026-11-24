package ca.ulaval.glo2004.dto.assembleurs;

import ca.ulaval.glo2004.domain.Outil;
import ca.ulaval.glo2004.dto.OutilDTO;

public class OutilAssembler {
  public Outil creerOutil(OutilDTO dto) {
    Outil outil = new Outil(dto.nom(), dto.largeur());
    outil.setId(dto.id());
    if (dto.utiliseParDefault()) {
      outil.activerOutilParDefaut();
    }
    return outil;
  }

  public OutilDTO creerOutilDTO(Outil outil) {
    return new OutilDTO(
      outil.getId(),
      outil.getNomOutil(),
      outil.getLargeur(),
      outil.estParDefault()
    );
  }
}
