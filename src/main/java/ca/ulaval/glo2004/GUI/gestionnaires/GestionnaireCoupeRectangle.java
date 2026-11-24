package ca.ulaval.glo2004.GUI.gestionnaires;

import ca.ulaval.glo2004.GUI.DrawingPanel;
import ca.ulaval.glo2004.controller.CNC_Controller;

import javax.swing.*;
import java.awt.*;

public class GestionnaireCoupeRectangle {
    private final CNC_Controller controller;
    private final DrawingPanel drawingPanel;
    private boolean waitingForSecondClick = false; // State tracking
    private boolean waitingForThirdClick = false; // State tracking
    private Point firstPoint;
    private Point secondPoint;
    private Point thirdPoint;

    public GestionnaireCoupeRectangle(CNC_Controller controller, DrawingPanel drawingPanel) {
        this.controller = controller;
        this.drawingPanel = drawingPanel;
    }

    private void handleFirstCutClick(Point clickPoint) {
        firstPoint = clickPoint;
        waitingForSecondClick = true;
    }

    private void handleSecondCutClick(Point clickPoint) {
        secondPoint = clickPoint;
        waitingForThirdClick = true;
    }

    private void handleThirdCutClick(Point clickPoint) {
        thirdPoint = clickPoint;

        try {
            controller.ajouterCoupeRectangulaire(secondPoint, thirdPoint);
            drawingPanel.repaint();
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(drawingPanel, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
        }
        reset();
    }

    public void handleRectangularCutClick(Point clickPoint) {
        if (!waitingForSecondClick) {
            handleFirstCutClick(clickPoint);
        } else if (!waitingForThirdClick) {
            handleSecondCutClick(clickPoint);
        } else {
            handleThirdCutClick(clickPoint);
        }
    }

    public void reset() {
        firstPoint = null;
        secondPoint = null;
        thirdPoint = null;
        waitingForSecondClick = false;
        waitingForThirdClick = false;
    }

}
