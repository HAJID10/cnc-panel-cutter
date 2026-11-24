package ca.ulaval.glo2004.dto;

import java.awt.Point;
import java.io.Serializable;
import java.util.UUID;

import ca.ulaval.glo2004.domain.CutType;

/**
 * Read-only copy of a cut. {@code width} and {@code height} are the extent of the tool path from
 * {@code origin} to {@code destination}.
 */
public record CutDto(UUID id, CutType type, Point origin, Point destination, int width, int height, double depth,
                     ToolDto tool, boolean selected) implements Serializable {
}
