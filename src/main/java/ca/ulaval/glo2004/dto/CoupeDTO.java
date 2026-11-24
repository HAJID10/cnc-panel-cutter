package ca.ulaval.glo2004.dto;

import java.awt.Point;
import java.io.Serializable;
import java.util.UUID;

import ca.ulaval.glo2004.domain.TypeCoupe;

public record CoupeDTO(
  UUID id,
  Point origine,
  Point destination,
  double profondeur,
  OutilDTO outilDTO,
  boolean estSelectionne,
  TypeCoupe typeCoupe,
  int largeur,
  int longueur
) implements Serializable {}