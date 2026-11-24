package ca.ulaval.glo2004.dto;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.UUID;

import ca.ulaval.glo2004.domain.CutType;

/**
 * Read-only copy of a cut. {@code width} and {@code height} are the extent of the tool path from
 * {@code origin} to {@code destination}.
 */
public record CutDto(UUID id, CutType type, Point2D.Double origin, Point2D.Double destination, double width, double height, double depth,
                     ToolDto tool, boolean selected) implements Serializable {
}
