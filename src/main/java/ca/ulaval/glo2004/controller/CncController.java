package ca.ulaval.glo2004.controller;

import java.awt.geom.Point2D;
import java.awt.Shape;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import ca.ulaval.glo2004.domain.Panel;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;
import ca.ulaval.glo2004.dto.ZoneDto;
import ca.ulaval.glo2004.dto.mappers.CutMapper;
import ca.ulaval.glo2004.dto.mappers.PanelMapper;
import ca.ulaval.glo2004.dto.mappers.ToolMapper;
import ca.ulaval.glo2004.infra.History;
import ca.ulaval.glo2004.infra.ProjectFile;

/**
 * Single entry point used by the user interface. It only hands out DTOs (read-only copies), so the interface can
 * never change the domain behind the controller's back. Every change is recorded in the undo history.
 */
public class CncController {
    private final ToolMapper toolMapper = new ToolMapper();
    private final CutMapper cutMapper = new CutMapper(toolMapper);
    private final PanelMapper panelMapper = new PanelMapper(toolMapper, cutMapper);
    private final History history = new History();
    private final ProjectFile projectFile = new ProjectFile();
    private Panel panel;

    public CncController(PanelDto initialPanel) {
        this.panel = panelMapper.toDomain(initialPanel);
    }

    /**
     * Runs a change on the panel. The state before the change goes into the undo history only if the change
     * succeeds; if it is refused, the panel is put back as it was, so a refused action leaves no trace (and a
     * multi-step edit is all-or-nothing).
     */
    private void change(Runnable action) {
        PanelDto before = getPanel();
        try {
            action.run();
        } catch (RuntimeException e) {
            panel = panelMapper.toDomain(before);
            throw e;
        }
        history.record(before);
    }

    // --- Panel, history and files ---

    public PanelDto getPanel() {
        return panelMapper.toDto(panel);
    }

    public void newPanel(PanelDto dto) {
        change(() -> panel = panelMapper.toDomain(dto));
    }

    public void undo() {
        PanelDto previous = history.undo(getPanel());
        if (previous != null) {
            panel = panelMapper.toDomain(previous);
        }
    }

    public void redo() {
        PanelDto next = history.redo(getPanel());
        if (next != null) {
            panel = panelMapper.toDomain(next);
        }
    }

    public void save(String path) throws IOException {
        projectFile.save(getPanel(), path);
    }

    public void load(String path) throws IOException {
        panel = panelMapper.toDomain(projectFile.load(path));
    }

    // --- Cuts ---
    // toolId may be null: the default tool is then used. depth may be null: the cut goes right through.

    public void addVerticalCut(double x, UUID toolId) {
        addVerticalCut(x, toolId, null);
    }

    public void addVerticalCut(double x, UUID toolId, Double depth) {
        change(() -> panel.addVerticalCut(x, toolId, depth));
    }

    public void addHorizontalCut(double y, UUID toolId) {
        addHorizontalCut(y, toolId, null);
    }

    public void addHorizontalCut(double y, UUID toolId, Double depth) {
        change(() -> panel.addHorizontalCut(y, toolId, depth));
    }

    public void addRectangularCut(Point2D.Double corner1, Point2D.Double corner2, UUID toolId) {
        addRectangularCut(corner1, corner2, toolId, null);
    }

    public void addRectangularCut(Point2D.Double corner1, Point2D.Double corner2, UUID toolId, Double depth) {
        change(() -> panel.addRectangularCut(corner1, corner2, toolId, depth));
    }

    public void addLCut(Point2D.Double origin, Point2D.Double corner, UUID toolId) {
        addLCut(origin, corner, toolId, null);
    }

    public void addLCut(Point2D.Double origin, Point2D.Double corner, UUID toolId, Double depth) {
        change(() -> panel.addLCut(origin, corner, toolId, depth));
    }

    public void addBorderCut(double finalWidth, double finalLength, UUID toolId) {
        addBorderCut(finalWidth, finalLength, toolId, null);
    }

    public void addBorderCut(double finalWidth, double finalLength, UUID toolId, Double depth) {
        change(() -> panel.addBorderCut(finalWidth, finalLength, toolId, depth));
    }

    public void moveCut(UUID cutId, Point2D.Double newOrigin) {
        change(() -> panel.moveCut(cutId, newOrigin));
    }

    public void resizeCut(UUID cutId, double newWidth, double newHeight) {
        change(() -> panel.resizeCut(cutId, newWidth, newHeight));
    }

    public void setCutTool(UUID cutId, UUID toolId) {
        change(() -> panel.setCutTool(cutId, toolId));
    }

    /**
     * Applies several edits to one cut as a single undo step. Any argument may be null to leave that part alone
     * (width and height go together).
     */
    public void editCut(UUID cutId, Point2D.Double newOrigin, Double newWidth, Double newHeight, UUID toolId,
                        Double depth) {
        change(() -> {
            if (depth != null) {
                panel.setCutDepth(cutId, depth);
            }
            if (toolId != null) {
                panel.setCutTool(cutId, toolId);
            }
            if (newWidth != null && newHeight != null) {
                panel.resizeCut(cutId, newWidth, newHeight);
            }
            if (newOrigin != null) {
                panel.moveCut(cutId, newOrigin);
            }
        });
    }

    public void setCutDepth(UUID cutId, double depth) {
        change(() -> panel.setCutDepth(cutId, depth));
    }

    public void deleteCut(UUID cutId) {
        change(() -> panel.removeCut(cutId));
    }

    /** The material removed by a cut, used to draw it exactly as the domain sees it. */
    public Optional<Shape> getCutArea(UUID cutId) {
        return panel.findCut(cutId).map(cut -> cut.getCutArea());
    }

    // --- Selection (not part of the history: undo only undoes real changes) ---

    public Optional<CutDto> findCutAt(Point2D.Double point) {
        return panel.findCutAt(point).map(cut -> cutMapper.toDto(cut, panel.isInForbiddenZone(cut)));
    }

    public void selectCut(UUID cutId) {
        panel.selectCut(cutId);
    }

    public void deselectAll() {
        panel.deselectAll();
    }

    public Optional<CutDto> getSelectedCut() {
        return panel.getSelectedCut().map(cut -> cutMapper.toDto(cut, panel.isInForbiddenZone(cut)));
    }

    // --- Forbidden zones ---

    public void addZone(Point2D.Double corner1, Point2D.Double corner2) {
        change(() -> panel.addZone(corner1, corner2));
    }

    public void editZone(UUID zoneId, Point2D.Double topLeft, double width, double height) {
        change(() -> panel.editZone(zoneId, topLeft, width, height));
    }

    public void deleteZone(UUID zoneId) {
        change(() -> panel.removeZone(zoneId));
    }

    public Optional<ZoneDto> findZoneAt(Point2D.Double point) {
        return panel.findZoneAt(point).flatMap(zone -> getPanel().zones().stream()
                .filter(dto -> dto.id().equals(zone.getId())).findFirst());
    }

    // --- Tools ---

    public void addTool(ToolDto tool) {
        change(() -> panel.addTool(toolMapper.toDomain(tool)));
    }

    public void removeTool(UUID toolId) {
        change(() -> panel.removeTool(toolId));
    }

    public void setDefaultTool(UUID toolId) {
        change(() -> panel.setDefaultTool(toolId));
    }

    public void setToolWidth(UUID toolId, double width) {
        change(() -> panel.setToolWidth(toolId, width));
    }

    public Optional<ToolDto> getDefaultTool() {
        return Optional.ofNullable(toolMapper.toDto(panel.getDefaultTool()));
    }
}
