package ca.ulaval.glo2004.GUI.gestionnaires;

import java.awt.Point;

import javax.swing.JOptionPane;

import ca.ulaval.glo2004.GUI.DrawingPanel;
import ca.ulaval.glo2004.controller.CNC_Controller;

public class GestionnaireCoupeBordure {
  private final CNC_Controller controller;
  private final DrawingPanel drawingPanel;
  private Point borderStart;

  public GestionnaireCoupeBordure(CNC_Controller controller, DrawingPanel drawingPanel) {
    this.controller = controller;
    this.drawingPanel = drawingPanel;
  }

  public void handleBorderCutClick() {
//    if (borderStart == null) {
//      if (isOnBorder(clickPoint)) {
//        borderStart = clickPoint;
//        JOptionPane.showMessageDialog(drawingPanel,
//                                      "Cliquez sur le point d'arrivée sur la bordure.",
//                                      "Information",
//                                      JOptionPane.INFORMATION_MESSAGE);
//      } else {
//        JOptionPane.showMessageDialog(drawingPanel,
//                                      "Le point de départ doit être sur la bordure.",
//                                      "Erreur",
//                                      JOptionPane.ERROR_MESSAGE);
//      }
//    } else {
//      if (isOnBorder(clickPoint)) {
//        try {
//          controller.ajouterCoupeBordure(borderStart, clickPoint);
//          drawingPanel.repaint();
//        } catch (IllegalArgumentException e) {
//          JOptionPane.showMessageDialog(drawingPanel, e.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
//        }
//      } else {
//        JOptionPane.showMessageDialog(drawingPanel,
//                                      "Le point d'arrivée doit être sur la bordure.",
//                                      "Erreur",
//                                      JOptionPane.ERROR_MESSAGE);
//      }
//      reset();
//    }
  }

  private boolean isOnBorder(Point point) {
    int tolerance = 5;
    return point.x <= tolerance ||
           point.x >= controller.getPanneauDTO().largeurPanneau() - tolerance ||
           point.y <= tolerance ||
           point.y >= controller.getPanneauDTO().longueurPanneau() - tolerance;
  }

  public void reset() {
    borderStart = null;
  }
}