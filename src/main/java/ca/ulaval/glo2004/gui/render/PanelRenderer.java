package ca.ulaval.glo2004.gui.render;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;

/**
 * Draws the panel and its cuts in model coordinates (millimetres); the caller sets the zoom and offset on the
 * Graphics2D beforehand. Cut shapes come from the domain, so what is drawn is exactly what can be clicked.
 */
public class PanelRenderer {
    private static final Color PANEL = new Color(222, 196, 150);
    private static final Color PANEL_EDGE = new Color(90, 70, 40);
    private static final Color CUT = new Color(30, 30, 30);
    private static final Color SHALLOW = new Color(170, 140, 95);
    private static final Color SELECTED = new Color(30, 110, 230);
    private static final Color GRID = new Color(0, 0, 0, 40);

    /**
     * @param cutArea    gives the removed material of a cut from its id
     * @param dragged    id of a cut being dragged (drawn shifted by dx/dy), or null
     */
    public void draw(Graphics2D g, PanelDto panel, Function<UUID, Optional<Shape>> cutArea, boolean showGrid,
                     double gridSpacing, UUID dragged, double dx, double dy) {
        Rectangle2D board = new Rectangle2D.Double(0, 0, panel.width(), panel.length());
        g.setColor(PANEL);
        g.fill(board);

        if (showGrid && gridSpacing > 0) {
            drawGrid(g, panel, gridSpacing);
        }

        for (CutDto cut : panel.cuts()) {
            Optional<Shape> area = cutArea.apply(cut.id());
            if (area.isEmpty()) {
                continue;
            }
            Shape shape = area.get();
            if (cut.id().equals(dragged)) {
                shape = AffineTransform.getTranslateInstance(dx, dy).createTransformedShape(shape);
            }
            g.setColor(cut.selected() ? SELECTED : colourForDepth(cut.depth(), panel.thickness()));
            g.fill(shape);
        }

        g.setColor(PANEL_EDGE);
        g.setStroke(new BasicStroke(0f)); // thinnest line, whatever the zoom
        g.draw(board);
    }

    /**
     * Through cuts are drawn dark; shallower grooves get lighter, down to a light shade of the wood colour for a
     * groove that barely scratches the surface.
     */
    static Color colourForDepth(double depth, double thickness) {
        double ratio = Math.max(0, Math.min(1, depth / thickness));
        double t = 0.25 + 0.75 * ratio; // never fully invisible
        return new Color(mix(SHALLOW.getRed(), CUT.getRed(), t), mix(SHALLOW.getGreen(), CUT.getGreen(), t),
                mix(SHALLOW.getBlue(), CUT.getBlue(), t));
    }

    private static int mix(int from, int to, double t) {
        return (int) Math.round(from + (to - from) * t);
    }

    private void drawGrid(Graphics2D g, PanelDto panel, double spacing) {
        g.setColor(GRID);
        g.setStroke(new BasicStroke(0f));
        for (double x = spacing; x < panel.width(); x += spacing) {
            g.draw(new java.awt.geom.Line2D.Double(x, 0, x, panel.length()));
        }
        for (double y = spacing; y < panel.length(); y += spacing) {
            g.draw(new java.awt.geom.Line2D.Double(0, y, panel.width(), y));
        }
    }
}
