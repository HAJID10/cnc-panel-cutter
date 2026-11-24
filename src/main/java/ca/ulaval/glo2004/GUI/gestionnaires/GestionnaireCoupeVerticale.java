package ca.ulaval.glo2004.GUI.gestionnaires;

import java.awt.Point;

import javax.swing.JOptionPane;

import ca.ulaval.glo2004.GUI.DrawingPanel;
import ca.ulaval.glo2004.controller.CNC_Controller;

public class GestionnaireCoupeVerticale {
  private final CNC_Controller controller;
  private final DrawingPanel drawingPanel;

  public GestionnaireCoupeVerticale(CNC_Controller controller, DrawingPanel drawingPanel) {
    this.controller = controller;
    this.drawingPanel = drawingPanel;
  }

  public void ajouterCoupeVerticale(Point point) {
    try {
      controller.ajouterCoupeVerticale(point);
      drawingPanel.repaint();
    } catch (IllegalArgumentException e) {
      JOptionPane.showMessageDialog(drawingPanel, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
    }
  }
}