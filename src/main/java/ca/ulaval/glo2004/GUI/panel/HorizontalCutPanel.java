package ca.ulaval.glo2004.GUI.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.ulaval.glo2004.GUI.DrawingPanel;
import ca.ulaval.glo2004.controller.CNC_Controller;
import ca.ulaval.glo2004.dto.CoupeDTO;

public class HorizontalCutPanel extends JPanel {
  private final CNC_Controller controller;
  private final DrawingPanel drawingPanel;
  private final JComboBox<String> referenceComboBox;
  private final JComboBox<String> existingCoupeComboBox;
  private final JComboBox<String> positionComboBox;
  private final JComboBox<String> outilComboBox;
  private final JTextField distanceField;
  private final JButton actionButton;

  public HorizontalCutPanel(CNC_Controller controller, DrawingPanel drawingPanel) {
    super(new BorderLayout(10, 10));
    this.controller = controller;
    this.drawingPanel = drawingPanel;

    referenceComboBox = new JComboBox<>(new String[]{"Bas", "Haut", "Coupe existante"});
    existingCoupeComboBox = new JComboBox<>();
    positionComboBox = new JComboBox<>(new String[]{"En dessous", "Au-dessus"});
    outilComboBox = new JComboBox<>();
    distanceField = new JTextField(10);
    actionButton = new JButton("Ajouter");

    setupLayout();
    setupListeners();
    updateExistingCoupesList();
    updateOutilsList();
  }

  private void setupLayout() {
    JPanel mainPanel = new JPanel(new GridLayout(6, 2, 5, 5));
    mainPanel.setBorder(BorderFactory.createTitledBorder("Configuration de la coupe horizontale"));

    mainPanel.add(new JLabel("Référence :"));
    mainPanel.add(referenceComboBox);
    mainPanel.add(new JLabel("Coupe existante :"));
    mainPanel.add(existingCoupeComboBox);
    mainPanel.add(new JLabel("Position relative :"));
    mainPanel.add(positionComboBox);
    mainPanel.add(new JLabel("Outil :"));
    mainPanel.add(outilComboBox);
    mainPanel.add(new JLabel("Distance (mm) :"));
    mainPanel.add(distanceField);
    mainPanel.add(new JLabel(""));
    mainPanel.add(actionButton);

    existingCoupeComboBox.setEnabled(false);
    positionComboBox.setEnabled(false);

    add(mainPanel, BorderLayout.NORTH);
  }

  private void setupListeners() {
    referenceComboBox.addActionListener(e -> {
      boolean isExistingCutSelected = referenceComboBox.getSelectedIndex() == 2;
      existingCoupeComboBox.setEnabled(isExistingCutSelected);
      positionComboBox.setEnabled(isExistingCutSelected);
    });

    actionButton.addActionListener(e -> handleAction());
  }

  private void handleAction() {
    try {
      double distance = Double.parseDouble(distanceField.getText());
      Point position = new Point(0, (int) distance);
      String selectedOutil = (String) outilComboBox.getSelectedItem();

      var coupe = controller.getCoupeSelectionnee();
      if (coupe.isPresent()) {
        controller.deplacerCoupeParId(coupe.get().id(), position);
        controller.modifierOutilCoupe(coupe.get().id(), selectedOutil);
        this.drawingPanel.repaint();
        this.drawingPanel.firePropertyChange("repaint", false, true);
      } else {
        controller.ajouterCoupeHorizontale(position);
      }

      drawingPanel.repaint();
      drawingPanel.firePropertyChange("repaint", false, true);
      updateExistingCoupesList();
      distanceField.setText("");

    } catch (NumberFormatException ex) {
      JOptionPane.showMessageDialog(this,
                                    "Veuillez entrer une distance valide.",
                                    "Erreur",
                                    JOptionPane.ERROR_MESSAGE);
    } catch (IllegalArgumentException ex) {
      JOptionPane.showMessageDialog(this,
                                    ex.getMessage(),
                                    "Erreur",
                                    JOptionPane.ERROR_MESSAGE);
    }
  }

  public void setSelectedCoupe() {
    var coupe = controller.getCoupeSelectionnee();

    if (coupe.isPresent()) {
      actionButton.setText("Modifier");
      distanceField.setText(String.valueOf((int) coupe.get().origine().getY()));
      outilComboBox.setSelectedItem(coupe.get().outilDTO().nom());
    } else {
      actionButton.setText("Ajouter");
      distanceField.setText("");
    }
    revalidate();
    repaint();
  }

  private void updateExistingCoupesList() {
    existingCoupeComboBox.removeAllItems();
    for (CoupeDTO coupeDTO : controller.getPanneauDTO().coupesPanneauDTO()) {
      existingCoupeComboBox.addItem("Coupe " + coupeDTO.id());
    }
  }

  private void updateOutilsList() {
    outilComboBox.removeAllItems();
    for (var outil : controller.getPanneauDTO().outilsPanneauDTO()) {
      outilComboBox.addItem(outil.nom());
    }
  }
}