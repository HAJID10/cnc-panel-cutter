package ca.ulaval.glo2004.dto;

import java.io.Serializable;
import java.util.UUID;

/** Read-only copy of a tool, given to the user interface. */
public record ToolDto(UUID id, String name, double width, boolean isDefault) implements Serializable {

    /** Shown in lists and combo boxes. */
    @Override
    public String toString() {
        return String.format("%s (%.2f mm)", name, width);
    }
}
