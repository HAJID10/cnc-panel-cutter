package ca.ulaval.glo2004.GUI.gestionnaires;

import java.awt.*;
import javax.swing.JOptionPane;

import ca.ulaval.glo2004.GUI.DrawingPanel;
import ca.ulaval.glo2004.controller.CNC_Controller;
import ca.ulaval.glo2004.dto.PanneauDTO;

public class GestionnaireCoupeL {
    private Point lFirstPoint;
    private Point lSecondPoint;
    private static final int EDGE_MARGIN = 11; // 3mm in pixels (approx)
    private boolean waitingForSecondClick = false;
    private final CNC_Controller controller;
    private final DrawingPanel drawingPanel;

    public GestionnaireCoupeL(CNC_Controller controller, DrawingPanel drawingPanel) {
        this.controller = controller;
        this.drawingPanel = drawingPanel;
    }

    public void handleLCutClick(Point clickPoint) {
        if (!waitingForSecondClick) {
            /*if (!validateAllowedCut(clickPoint)) {
                reset();
            } else {}*/
            handleFirstLCutClick(clickPoint);
        } else {
            handleSecondLCutClick(clickPoint);
        }
    }

    /*private void drawUnallowedLCutArea(Point clickPoint) {
        int width = (int) drawingPanel.getWidth();
        int height = (int) drawingPanel.getHeight();

        Graphics g = drawingPanel.getGraphics();
        g.setColor(Color.RED);

        if (isNearCorner(clickPoint, new Point(0, 0), EDGE_MARGIN)) {
            g.fillRect(0, 0, EDGE_MARGIN, EDGE_MARGIN); // Top-left corner
        } else if (isNearCorner(clickPoint, new Point(width, 0), EDGE_MARGIN)) {
            g.fillRect(width - EDGE_MARGIN, 0, EDGE_MARGIN, EDGE_MARGIN); // Top-right corner
        } else if (isNearCorner(clickPoint, new Point(0, height), EDGE_MARGIN)) {
            g.fillRect(0, height - EDGE_MARGIN, EDGE_MARGIN, EDGE_MARGIN); // Bottom-left corner
        } else if (isNearCorner(clickPoint, new Point(width, height), EDGE_MARGIN)) {
            g.fillRect(width - EDGE_MARGIN, height - EDGE_MARGIN, EDGE_MARGIN, EDGE_MARGIN); // Bottom-right corner
        }
    }*/

    private boolean isNearCorner(Point clickPoint, Point cornerPoint, int margin) {
        int x = clickPoint.x;
        int y = clickPoint.y;
        int cornerX = cornerPoint.x;
        int cornerY = cornerPoint.y;

        return (Math.abs(x - cornerX) <= margin && Math.abs(y - cornerY) <= margin);
    }

    private boolean validateAllowedCut(Point clickPoint) {
        Point panelClickPoint = new Point(clickPoint.x, clickPoint.y);
        Point panelTopLeft = new Point(0, 0);
        Point panelTopRight = new Point(drawingPanel.getWidth(), 0);
        Point panelBottomLeft = new Point(0, drawingPanel.getHeight());
        Point panelBottomRight = new Point(drawingPanel.getWidth(), drawingPanel.getHeight());

        int edgeMargin = 11; // 3mm in pixels (approx)

        if (isNearCorner(clickPoint, panelTopLeft, edgeMargin) ||
                isNearCorner(clickPoint, panelTopRight, edgeMargin) ||
                isNearCorner(clickPoint, panelBottomLeft, edgeMargin) ||
                isNearCorner(clickPoint, panelBottomRight, edgeMargin)) {
            return true;
        } else {
            JOptionPane.showMessageDialog(drawingPanel,
                    "Le premier point de la coupe en L doit être à proximité d'un coin du panneau (3mm).",
                    "Erreur",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private void handleFirstLCutClick(Point clickPoint) {
        lFirstPoint = clickPoint;
        waitingForSecondClick = true;

        /*JOptionPane.showMessageDialog(drawingPanel,
                "Cliquez pour définir le point final de la coupe en L.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);*/
    }

    private void handleSecondLCutClick(Point clickPoint) {
        lSecondPoint = clickPoint;
        waitingForSecondClick = false;

        try {
            controller.ajouterCoupeL(lFirstPoint, lSecondPoint);
            drawingPanel.repaint();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(drawingPanel, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        reset();
    }

    public void reset() {
        lFirstPoint = null;
        lSecondPoint = null;
        waitingForSecondClick = false;
    }
}
