package ca.ulaval.glo2004.dto;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.UUID;

/** Read-only copy of a forbidden zone: a rectangle from its top-left corner. */
public record ZoneDto(UUID id, Point2D.Double origin, double width, double height) implements Serializable {
}
