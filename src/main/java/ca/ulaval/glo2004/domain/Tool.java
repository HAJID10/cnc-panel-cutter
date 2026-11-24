package ca.ulaval.glo2004.domain;

import java.io.Serializable;
import java.util.UUID;

/** A router bit. Its width is the width of the groove it cuts (the kerf). */
public class Tool implements Serializable {
    private UUID id;
    private final String name;
    private double width;
    private boolean isDefault;

    public Tool(String name, double width) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("The tool name can't be empty.");
        }
        checkWidth(width);
        this.id = UUID.randomUUID();
        this.name = name.trim();
        this.width = width;
    }

    private static void checkWidth(double width) {
        if (width <= 0) {
            throw new IllegalArgumentException("The tool width must be positive.");
        }
    }

    public UUID getId() {
        return id;
    }

    /** Used when rebuilding a tool from a saved state, so that cuts keep pointing to it. */
    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        checkWidth(width);
        this.width = width;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }

    @Override
    public String toString() {
        return name + " (" + width + " mm)";
    }
}
