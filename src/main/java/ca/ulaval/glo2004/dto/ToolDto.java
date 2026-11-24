package ca.ulaval.glo2004.dto;

import java.io.Serializable;
import java.util.UUID;

/** Read-only copy of a tool, given to the user interface. */
public record ToolDto(UUID id, String name, double width, boolean isDefault) implements Serializable {

    /** For logs and debugging; the interface formats tools with its own unit (see gui.Units). */
    @Override
    public String toString() {
        return name + " (" + width + " mm)";
    }
}
