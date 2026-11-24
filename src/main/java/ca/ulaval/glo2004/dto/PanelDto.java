package ca.ulaval.glo2004.dto;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/** Read-only copy of the whole panel: used for display, undo history and files. */
public record PanelDto(UUID id, double length, double width, double thickness, List<CutDto> cuts,
                       List<ToolDto> tools, List<ZoneDto> zones) implements Serializable {

    /** A panel without forbidden zones. */
    public PanelDto(UUID id, double length, double width, double thickness, List<CutDto> cuts, List<ToolDto> tools) {
        this(id, length, width, thickness, cuts, tools, List.of());
    }
}
