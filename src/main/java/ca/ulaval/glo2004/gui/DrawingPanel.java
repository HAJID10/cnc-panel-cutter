package ca.ulaval.glo2004.gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.UUID;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ZoneDto;
import ca.ulaval.glo2004.gui.render.PanelRenderer;

/**
 * The drawing area. Converts mouse positions (screen pixels) to panel coordinates (mm), creates cuts according
 * to the current {@link Mode}, lets the user select and drag cuts, and supports zoom (wheel) and pan (drag on
 * an empty spot).
 */
public class DrawingPanel extends JPanel {

    /** What a click on the panel does. */
    public enum Mode { NONE, SELECT, VERTICAL, HORIZONTAL, RECTANGULAR, BORDER, L, ZONE }

    private final CncController controller;
    private final Runnable onChange;
    private final PanelRenderer renderer = new PanelRenderer();

    private Mode mode = Mode.NONE;
    private Point2D.Double pendingPoint;   // first click of a two-click shape (rectangle, L, zone)
    private Point2D.Double hoverPoint;     // mouse position in mm, for the two-click preview

    // View: screen = model * scale + offset
    private double scale = 1.0;
    private double offsetX;
    private double offsetY;
    private Point panStart;       // screen point where panning started

    // Selected forbidden zone (cuts are selected in the domain; zones only here, as they have no edit history)
    private UUID selectedZone;

    // Dragging a selected cut or zone
    private UUID dragId;                   // the cut or zone being dragged
    private Point2D.Double dragStart;      // model point where the drag started
    private Point2D.Double dragOrigin;     // its origin when the drag started
    private CutType dragType;
    private double dragDx;
    private double dragDy;

    private boolean showGrid;
    private double gridSpacing = 20; // mm

    public DrawingPanel(CncController controller, Runnable onChange) {
        this.controller = controller;
        this.onChange = onChange;
        setBackground(new Color(70, 70, 70));
        setToolTipText(""); // enables tooltips; the text is set while the mouse moves

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                onPress(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                onDrag(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                onRelease();
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                hoverPoint = toModel(e.getPoint());
                setToolTipText(controller.findCutAt(hoverPoint).map(DrawingPanel::describe)
                        .or(() -> controller.findZoneAt(hoverPoint).map(z -> "<html>Forbidden zone<br>"
                                + Units.format(z.width()) + " × " + Units.withUnit(z.height()) + "</html>"))
                        .orElse(null));
                if (pendingPoint != null) {
                    repaint();
                }
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom(e.getPoint(), e.getPreciseWheelRotation() > 0 ? 0.9 : 1.1);
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
        addMouseWheelListener(mouse);
    }

    // --- Mouse handling ---

    private void onPress(MouseEvent e) {
        Point2D.Double p = toModel(e.getPoint());
        switch (mode) {
            case SELECT -> {
                // Cuts are picked before zones, so a cut lying under a zone can still be selected and moved out.
                var cut = controller.findCutAt(p);
                Optional<ZoneDto> zone = cut.isPresent() ? Optional.empty() : controller.findZoneAt(p);
                selectedZone = zone.map(ZoneDto::id).orElse(null);
                if (cut.isPresent()) {
                    controller.selectCut(cut.get().id());
                    if (cut.get().type() != CutType.BORDER) {
                        startDrag(p, cut.get().id(), cut.get().origin(), cut.get().type());
                    }
                } else if (zone.isPresent()) {
                    controller.deselectAll();
                    startDrag(p, zone.get().id(), zone.get().origin(), null);
                } else {
                    controller.deselectAll();
                    panStart = e.getPoint();
                }
                onChange.run();
            }
            case VERTICAL -> run(() -> controller.addVerticalCut(p.x, null));
            case HORIZONTAL -> run(() -> controller.addHorizontalCut(p.y, null));
            case RECTANGULAR, L, ZONE -> {
                if (pendingPoint == null) {
                    pendingPoint = p;
                    repaint();
                } else {
                    Point2D.Double first = pendingPoint;
                    pendingPoint = null;
                    if (mode == Mode.RECTANGULAR) {
                        run(() -> controller.addRectangularCut(first, p, null));
                    } else if (mode == Mode.ZONE) {
                        run(() -> controller.addZone(first, p));
                        warnAboutConflicts();
                    } else {
                        run(() -> controller.addLCut(first, p, null));
                    }
                }
            }
            default -> panStart = e.getPoint();
        }
    }

    private void onDrag(MouseEvent e) {
        if (dragStart != null) {
            Point2D.Double p = toModel(e.getPoint());
            double dx = dragType == CutType.HORIZONTAL ? 0 : p.x - dragStart.x;
            double dy = dragType == CutType.VERTICAL ? 0 : p.y - dragStart.y;
            if (showGrid) {
                // Snap the new origin (not the mouse) to the grid.
                Point2D.Double snapped = snap(new Point2D.Double(dragOrigin.x + dx, dragOrigin.y + dy));
                dx = dragType == CutType.HORIZONTAL ? 0 : snapped.x - dragOrigin.x;
                dy = dragType == CutType.VERTICAL ? 0 : snapped.y - dragOrigin.y;
            }
            dragDx = dx;
            dragDy = dy;
            repaint();
        } else if (panStart != null) {
            offsetX += e.getX() - panStart.x;
            offsetY += e.getY() - panStart.y;
            panStart = e.getPoint();
            repaint();
        }
    }

    /** A drag only changes the model when the mouse is released: one move, one undo step. */
    private void onRelease() {
        if (dragStart != null && (dragDx != 0 || dragDy != 0)) {
            Point2D.Double newOrigin = new Point2D.Double(dragOrigin.x + dragDx, dragOrigin.y + dragDy);
            UUID id = dragId;
            Optional<ZoneDto> zone = controller.getPanel().zones().stream().filter(z -> z.id().equals(id)).findFirst();
            if (zone.isPresent()) {
                run(() -> controller.editZone(id, newOrigin, zone.get().width(), zone.get().height()));
                warnAboutConflicts();
            } else {
                run(() -> controller.moveCut(id, newOrigin));
            }
        }
        dragId = null;
        dragStart = null;
        dragOrigin = null;
        dragDx = 0;
        dragDy = 0;
        panStart = null;
        repaint();
    }

    private void startDrag(Point2D.Double p, UUID id, Point2D.Double origin, CutType type) {
        dragStart = p;
        dragId = id;
        dragOrigin = origin;
        dragType = type; // null for a zone: it moves freely
    }

    /** A zone may be placed over existing cuts: they turn red and the user is told why. */
    private void warnAboutConflicts() {
        long conflicts = controller.getPanel().cuts().stream().filter(CutDto::inForbiddenZone).count();
        if (conflicts > 0) {
            JOptionPane.showMessageDialog(this, conflicts + " cut(s) touch a forbidden zone. They are shown in red:\n"
                    + "move them or change the zone before machining.", "Forbidden zone", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** The selected forbidden zone, if it still exists (it may have been deleted or undone). */
    public Optional<ZoneDto> getSelectedZone() {
        return controller.getPanel().zones().stream().filter(z -> z.id().equals(selectedZone)).findFirst();
    }

    public void clearSelectedZone() {
        selectedZone = null;
        repaint();
    }

    /** Runs a change and shows the domain's message if it is refused. */
    private void run(Runnable action) {
        try {
            action.run();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Not allowed", JOptionPane.WARNING_MESSAGE);
        }
        onChange.run();
        repaint();
    }

    private Point2D.Double snap(Point2D.Double p) {
        return new Point2D.Double(Math.round(p.x / gridSpacing) * gridSpacing,
                Math.round(p.y / gridSpacing) * gridSpacing);
    }

    // --- View ---

    /**
     * Screen pixels to panel millimetres, rounded to the click step of the current unit (0.5 mm or 1/32 inch): a mouse click can't be more precise than
     * that, and round numbers are easier to read. Exact values can be typed in the side panel.
     */
    private Point2D.Double toModel(Point screen) {
        return new Point2D.Double(round((screen.x - offsetX) / scale), round((screen.y - offsetY) / scale));
    }

    private static double round(double mm) {
        double step = Units.clickStepMm();
        return Math.round(mm / step) * step;
    }

    private void zoom(Point screenPoint, double factor) {
        // Keep the point under the mouse where it is.
        offsetX = screenPoint.x - factor * (screenPoint.x - offsetX);
        offsetY = screenPoint.y - factor * (screenPoint.y - offsetY);
        scale *= factor;
        repaint();
    }

    /** Zooms so the whole panel fits in the drawing area, with a margin. */
    public void fitToWindow() {
        PanelDto panel = controller.getPanel();
        if (getWidth() == 0 || getHeight() == 0) {
            return;
        }
        scale = Math.min(getWidth() / panel.width(), getHeight() / panel.length()) * 0.9;
        offsetX = (getWidth() - panel.width() * scale) / 2;
        offsetY = (getHeight() - panel.length() * scale) / 2;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(offsetX, offsetY);
        g2.scale(scale, scale);

        UUID dragged = dragStart == null ? null : dragId;
        renderer.draw(g2, controller.getPanel(), controller::getCutArea, showGrid, gridSpacing, dragged, dragDx, dragDy,
                selectedZone);
        drawPendingPreview(g2);
        g2.dispose();
    }

    /** Dashed outline of the rectangle or L being drawn, between the first click and the mouse. */
    private void drawPendingPreview(Graphics2D g) {
        if (pendingPoint == null || hoverPoint == null) {
            return;
        }
        g.setColor(new Color(30, 110, 230));
        g.setStroke(new BasicStroke((float) (1.5 / scale), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f,
                new float[]{(float) (6 / scale)}, 0f));
        if (mode == Mode.RECTANGULAR || mode == Mode.ZONE) {
            g.draw(new Rectangle2D.Double(Math.min(pendingPoint.x, hoverPoint.x), Math.min(pendingPoint.y, hoverPoint.y),
                    Math.abs(hoverPoint.x - pendingPoint.x), Math.abs(hoverPoint.y - pendingPoint.y)));
        } else {
            Path2D.Double l = new Path2D.Double();
            l.moveTo(pendingPoint.x, hoverPoint.y);
            l.lineTo(hoverPoint.x, hoverPoint.y);
            l.lineTo(hoverPoint.x, pendingPoint.y);
            g.draw(l);
        }
        double r = 4 / scale;
        g.draw(new Line2D.Double(pendingPoint.x - r, pendingPoint.y, pendingPoint.x + r, pendingPoint.y));
        g.draw(new Line2D.Double(pendingPoint.x, pendingPoint.y - r, pendingPoint.x, pendingPoint.y + r));
    }

    /** Tooltip text for a cut. */
    static String describe(CutDto cut) {
        String size = switch (cut.type()) {
            case VERTICAL -> "Length: " + Units.withUnit(cut.height());
            case HORIZONTAL -> "Length: " + Units.withUnit(cut.width());
            case RECTANGULAR -> "Size: " + Units.format(cut.width()) + " × " + Units.withUnit(cut.height());
            case BORDER -> "Final size: " + Units.format(cut.width()) + " × " + Units.withUnit(cut.height());
            case L -> "Corner: " + point(cut.destination());
        };
        return "<html>" + cut.type() + "<br>Start: " + point(cut.origin()) + "<br>" + size
                + "<br>Depth: " + Units.withUnit(cut.depth()) + "<br>Tool: " + cut.tool().name()
                + (cut.inForbiddenZone() ? "<br><b>Touches a forbidden zone</b>" : "") + "</html>";
    }

    /** "(12.5, 30)" */
    static String point(Point2D.Double p) {
        return "(" + Units.format(p.x) + ", " + Units.format(p.y) + ")";
    }

    // --- Settings used by the main window ---

    public void setMode(Mode mode) {
        this.mode = mode;
        if (mode != Mode.SELECT) {
            selectedZone = null;
        }
        cancelPending();
    }

    public Mode getMode() {
        return mode;
    }

    /** Forgets the first click of a two-click cut (Escape). */
    public void cancelPending() {
        pendingPoint = null;
        repaint();
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    /** @param gridSpacing in mm */
    public void setGridSpacing(double gridSpacing) {
        this.gridSpacing = gridSpacing;
        repaint();
    }

    public double getGridSpacing() {
        return gridSpacing;
    }
}
