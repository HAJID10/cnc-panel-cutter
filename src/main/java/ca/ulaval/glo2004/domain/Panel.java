package ca.ulaval.glo2004.domain;

import java.awt.Point;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import ca.ulaval.glo2004.domain.cuts.BorderCut;
import ca.ulaval.glo2004.domain.cuts.HorizontalCut;
import ca.ulaval.glo2004.domain.cuts.LCut;
import ca.ulaval.glo2004.domain.cuts.RectangularCut;
import ca.ulaval.glo2004.domain.cuts.VerticalCut;

/**
 * The wooden panel and everything planned on it: its cuts and the tools available.
 * <p>
 * Axes: x goes along the <em>width</em> (left to right), y along the <em>length</em> (top to bottom). All values
 * are in millimetres.
 */
public class Panel implements Serializable {
    private final UUID id;
    private final double length;
    private final double width;
    private final double thickness;
    private final List<Cut> cuts = new ArrayList<>();
    private final List<Tool> tools = new ArrayList<>();

    public Panel(double length, double width, double thickness) {
        this(UUID.randomUUID(), length, width, thickness);
    }

    /** Rebuilds a panel with a known identity (undo, loading a file). */
    public Panel(UUID id, double length, double width, double thickness) {
        if (length <= 0 || width <= 0 || thickness <= 0) {
            throw new IllegalArgumentException("The panel dimensions must be positive.");
        }
        this.id = id;
        this.length = length;
        this.width = width;
        this.thickness = thickness;
    }

    // --- Tools ---

    public void addTool(Tool tool) {
        if (tool == null) {
            throw new IllegalArgumentException("The tool can't be empty.");
        }
        if (findTool(tool.getId()).isPresent()) {
            throw new IllegalArgumentException("A tool with this id already exists.");
        }
        tools.add(tool);
    }

    public Optional<Tool> findTool(UUID toolId) {
        return tools.stream().filter(tool -> tool.getId().equals(toolId)).findFirst();
    }

    /**
     * Removes a tool that no cut uses. If it was the default tool, the first remaining tool becomes the default,
     * so new cuts can still be added.
     */
    public void removeTool(UUID toolId) {
        Tool tool = findTool(toolId).orElseThrow(() -> new IllegalArgumentException("Unknown tool."));
        long users = cuts.stream().filter(cut -> cut.getTool() == tool).count();
        if (users > 0) {
            throw new IllegalArgumentException(String.format(
                    "%s is used by %d cut(s). Give them another tool first.", tool.getName(), users));
        }
        if (tools.size() == 1) {
            throw new IllegalArgumentException("The panel needs at least one tool.");
        }
        tools.remove(tool);
        if (tool.isDefault()) {
            tools.get(0).setDefault(true);
        }
    }

    public Tool getDefaultTool() {
        return tools.stream().filter(Tool::isDefault).findFirst().orElse(null);
    }

    public void setDefaultTool(UUID toolId) {
        tools.forEach(tool -> tool.setDefault(tool.getId().equals(toolId)));
    }

    /** Changes a tool's width; the cuts that use it follow. */
    public void setToolWidth(UUID toolId, double newWidth) {
        Tool tool = findTool(toolId).orElseThrow(() -> new IllegalArgumentException("Unknown tool."));
        double oldWidth = tool.getWidth();
        tool.setWidth(newWidth);
        try {
            cuts.stream().filter(cut -> cut.getTool() == tool).forEach(Cut::checkToolFits);
        } catch (IllegalArgumentException e) {
            tool.setWidth(oldWidth); // a wider tool would no longer fit in one of its cuts
            throw e;
        }
        cuts.stream().filter(cut -> cut.getTool() == tool).forEach(Cut::toolChanged);
    }

    /** Tool used for a new cut: the one asked for, or the default tool. */
    private Tool toolForNewCut(UUID toolId) {
        if (toolId != null) {
            return findTool(toolId).orElseThrow(() -> new IllegalArgumentException("Unknown tool."));
        }
        Tool tool = getDefaultTool();
        if (tool == null) {
            throw new IllegalArgumentException("There is no default tool: choose a tool before adding a cut.");
        }
        return tool;
    }

    // --- Adding cuts ---

    /** Adds a cut after checking that it lies on the panel, fits its tool and is not a duplicate. */
    public void addCut(Cut cut) {
        if (!isInside(cut.getOrigin()) || !isInside(cut.getDestination())) {
            throw new IllegalArgumentException("The cut goes outside the panel.");
        }
        cut.checkToolFits();
        checkNotDuplicate(cut);
        cuts.add(cut);
    }

    /** Refuses a cut identical to another one: the tool would just cut the same groove twice. */
    private void checkNotDuplicate(Cut cut) {
        if (cuts.stream().anyMatch(other -> cut.sameAs(other))) {
            throw new IllegalArgumentException("There is already the same cut at this place.");
        }
    }

    public void addVerticalCut(int x, UUID toolId) {
        addCut(new VerticalCut(new Point(x, 0), new Point(x, (int) length), toolForNewCut(toolId)));
    }

    public void addHorizontalCut(int y, UUID toolId) {
        addCut(new HorizontalCut(new Point(0, y), new Point((int) width, y), toolForNewCut(toolId)));
    }

    /** The two points are opposite corners, in any order. */
    public void addRectangularCut(Point corner1, Point corner2, UUID toolId) {
        Point topLeft = new Point(Math.min(corner1.x, corner2.x), Math.min(corner1.y, corner2.y));
        int w = Math.abs(corner2.x - corner1.x);
        int h = Math.abs(corner2.y - corner1.y);
        addCut(new RectangularCut(topLeft, w, h, toolForNewCut(toolId)));
    }

    public void addLCut(Point origin, Point corner, UUID toolId) {
        addCut(new LCut(origin, corner, toolForNewCut(toolId)));
    }

    /** Trims the panel to {@code finalWidth} × {@code finalLength}, centred. */
    public void addBorderCut(int finalWidth, int finalLength, UUID toolId) {
        Point topLeft = new Point((int) ((width - finalWidth) / 2), (int) ((length - finalLength) / 2));
        addCut(new BorderCut(topLeft, finalWidth, finalLength, toolForNewCut(toolId)));
    }

    // --- Changing cuts ---

    /** Moves a cut so that its origin goes to {@code newOrigin} (as far as the cut type allows). */
    public void moveCut(UUID cutId, Point newOrigin) {
        Cut cut = getCut(cutId);
        if (!cut.isMovable()) {
            throw new IllegalArgumentException("This cut can't be moved.");
        }
        Point target = cut.movedOrigin(newOrigin);
        Point origin = cut.getOrigin();
        Point destination = cut.getDestination();
        int dx = target.x - origin.x;
        int dy = target.y - origin.y;
        if (!isInside(target) || !isInside(new Point(destination.x + dx, destination.y + dy))) {
            throw new IllegalArgumentException("The cut would go outside the panel.");
        }
        cut.translate(dx, dy);
        try {
            checkNotDuplicate(cut);
        } catch (IllegalArgumentException e) {
            cut.translate(-dx, -dy); // put it back
            throw e;
        }
    }

    /** Resizes a cut that has a width and a height (rectangle, border). */
    public void resizeCut(UUID cutId, int newWidth, int newHeight) {
        Cut cut = getCut(cutId);
        Point oldOrigin = cut.getOrigin();
        Point oldDestination = cut.getDestination();
        cut.resize(newWidth, newHeight);
        try {
            if (!isInside(cut.getOrigin()) || !isInside(cut.getDestination())) {
                throw new IllegalArgumentException("The new size goes outside the panel.");
            }
            cut.checkToolFits();
            checkNotDuplicate(cut);
        } catch (IllegalArgumentException e) {
            // Put it back exactly as it was (resizing back could round differently around the centre).
            cut.origin = oldOrigin;
            cut.destination = oldDestination;
            throw e;
        }
    }

    public void setCutTool(UUID cutId, UUID toolId) {
        Tool tool = findTool(toolId).orElseThrow(() -> new IllegalArgumentException("Unknown tool."));
        Cut cut = getCut(cutId);
        Tool oldTool = cut.getTool();
        cut.setTool(tool);
        try {
            cut.checkToolFits();
        } catch (IllegalArgumentException e) {
            cut.setTool(oldTool);
            throw e;
        }
    }

    public void removeCut(UUID cutId) {
        cuts.removeIf(cut -> cut.getId().equals(cutId));
    }

    // --- Selection ---

    public void selectCut(UUID cutId) {
        deselectAll();
        findCut(cutId).ifPresent(cut -> cut.setSelected(true));
    }

    public void deselectAll() {
        cuts.forEach(cut -> cut.setSelected(false));
    }

    public Optional<Cut> getSelectedCut() {
        return cuts.stream().filter(Cut::isSelected).findFirst();
    }

    /** The first cut under the point (with the selection tolerance). */
    public Optional<Cut> findCutAt(Point point) {
        return cuts.stream().filter(cut -> cut.contains(point)).findFirst();
    }

    // --- Lookup and checks ---

    public Optional<Cut> findCut(UUID cutId) {
        return cuts.stream().filter(cut -> cut.getId().equals(cutId)).findFirst();
    }

    private Cut getCut(UUID cutId) {
        return findCut(cutId).orElseThrow(() -> new IllegalArgumentException("Unknown cut."));
    }

    private boolean isInside(Point point) {
        return point.x >= 0 && point.x <= width && point.y >= 0 && point.y <= length;
    }

    public UUID getId() {
        return id;
    }

    public double getLength() {
        return length;
    }

    public double getWidth() {
        return width;
    }

    public double getThickness() {
        return thickness;
    }

    public List<Cut> getCuts() {
        return Collections.unmodifiableList(cuts);
    }

    public List<Tool> getTools() {
        return Collections.unmodifiableList(tools);
    }
}
