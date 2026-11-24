package ca.ulaval.glo2004.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

public record PanneauDTO(
  UUID id,
  double longueurPanneau,
  double largeurPanneau,
  double epaisseurPanneau,
  List<CoupeDTO> coupesPanneauDTO,
  List<OutilDTO> outilsPanneauDTO
) implements Serializable {

    @Override
    public String toString() {
        return String.format(
          "PanneauDTO [id=%s, longueur=%.2f, largeur=%.2f, epaisseur=%.2f, nbCoupes=%d, nbOutils=%d]",
          id, longueurPanneau, largeurPanneau, epaisseurPanneau,
          coupesPanneauDTO.size(), outilsPanneauDTO.size()
                            );
    }
}