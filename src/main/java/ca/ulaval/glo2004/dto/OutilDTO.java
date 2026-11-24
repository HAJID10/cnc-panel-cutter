package ca.ulaval.glo2004.dto;

import java.io.Serializable;
import java.util.UUID;

public record OutilDTO(
  UUID id,
  String nom,
  double largeur,
  boolean utiliseParDefault
) implements Serializable
{
}