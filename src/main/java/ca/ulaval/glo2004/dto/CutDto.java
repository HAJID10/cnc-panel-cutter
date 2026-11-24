package ca.ulaval.glo2004.dto;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.UUID;

import ca.ulaval.glo2004.domain.CutType;

/**
 * Read-only copy of a cut. {@code width} and {@code height} are the extent of the tool path from
 * {@code origin} to {@code destination}. {@code inForbiddenZone} is true when a forbidden zone was placed over
 * the cut afterwards: the cut is then shown as a conflict to fix.
 */
public record CutDto(UUID id, CutType type, Point2D.Double origin, Point2D.Double destination, double width,
                     double height, double depth, ToolDto tool, boolean selected, boolean inForbiddenZone)
        implements Serializable {

    /** A cut that is not in conflict with a forbidden zone. */
    public CutDto(UUID id, CutType type, Point2D.Double origin, Point2D.Double destination, double width,
                  double height, double depth, ToolDto tool, boolean selected) {
        this(id, type, origin, destination, width, height, depth, tool, selected, false);
    }
}
