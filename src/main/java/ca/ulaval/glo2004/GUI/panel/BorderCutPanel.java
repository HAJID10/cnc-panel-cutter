package ca.ulaval.glo2004.GUI.panel;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import ca.ulaval.glo2004.GUI.DrawingPanel;
import ca.ulaval.glo2004.controller.CNC_Controller;
import ca.ulaval.glo2004.dto.CoupeDTO;

public class BorderCutPanel extends JPanel {
  private final CNC_Controller controller;
  private final DrawingPanel drawingPanel;
  private final JSpinner largeurSpinner;
  private final JSpinner longueurSpinner;
  private final JComboBox<String> outilComboBox;
  private final JButton actionButton;

  public BorderCutPanel(CNC_Controller controller, DrawingPanel drawingPanel) {
    super(new BorderLayout(10, 10));
    this.controller = controller;
    this.drawingPanel = drawingPanel;

    largeurSpinner = new JSpinner(new SpinnerNumberModel(
      (int)(this.controller.getPanneauDTO().largeurPanneau()),
      1,
      (int)this.controller.getPanneauDTO().largeurPanneau(),
      1
    ));

    longueurSpinner = new JSpinner(new SpinnerNumberModel(
      (int)(this.controller.getPanneauDTO().longueurPanneau()),
      1,
      (int)this.controller.getPanneauDTO().longueurPanneau(),
      1
    ));

    outilComboBox = new JComboBox<>();
    actionButton = new JButton("Ajouter");

    setupLayout();
    setupListeners();
    updateOutilsList();
  }

  private void setupLayout() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder("Configuration de la coupe de bordure"));

    // Instructions
    JLabel instructionLabel = new JLabel(
      "<html>Entrez les dimensions finales souhaitées pour le panneau.<br>" +
      "La coupe sera centrée automatiquement.</html>"
    );
    instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    // Panel pour les dimensions et l'outil
    JPanel configPanel = new JPanel(new GridLayout(3, 2, 10, 10));

    configPanel.add(new JLabel("Outil:"));
    configPanel.add(outilComboBox);

    configPanel.add(new JLabel("Largeur finale (mm):"));
    configPanel.add(largeurSpinner);

    configPanel.add(new JLabel("Longueur finale (mm):"));
    configPanel.add(longueurSpinner);

    // Assemblage du panneau principal
    mainPanel.add(instructionLabel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(configPanel);
    mainPanel.add(Box.createVerticalStrut(10));
    mainPanel.add(actionButton);

    add(mainPanel, BorderLayout.NORTH);
  }

  private void setupListeners() {
    actionButton.addActionListener(e -> handleAction());
  }

  private void updateOutilsList() {
    outilComboBox.removeAllItems();
    for (var outil : controller.getPanneauDTO().outilsPanneauDTO()) {
      outilComboBox.addItem(outil.nom());
    }
  }

  private void handleAction() {
    int nouvelleLargeur = (Integer)largeurSpinner.getValue();
    int nouvelleLongueur = (Integer)longueurSpinner.getValue();
    String selectedOutil = (String) outilComboBox.getSelectedItem();

    var coupe = controller.getCoupeSelectionnee();
    if (coupe.isPresent()) {
      controller.redimensionnerCoupeBordure(coupe.get(), nouvelleLargeur, nouvelleLongueur);
      controller.modifierOutilCoupe(coupe.get().id(), selectedOutil);
      this.drawingPanel.repaint();
      this.drawingPanel.firePropertyChange("repaint", false, true);
    } else {
      controller.ajouterCoupeBordure(nouvelleLargeur, nouvelleLongueur);
    }

    drawingPanel.repaint();
    drawingPanel.firePropertyChange("repaint", false, true);
  }

  public void setSelectedCoupe() {
    var coupe = controller.getCoupeSelectionnee();

    if (coupe.isPresent()) {
      actionButton.setText("Modifier");
      CoupeDTO selectedCoupe = coupe.get();
      largeurSpinner.setValue(selectedCoupe.largeur());
      longueurSpinner.setValue(selectedCoupe.longueur());
      outilComboBox.setSelectedItem(selectedCoupe.outilDTO().nom());
    } else {
      actionButton.setText("Ajouter");
      largeurSpinner.setValue((int)(controller.getPanneauDTO().largeurPanneau()));
      longueurSpinner.setValue((int)(controller.getPanneauDTO().longueurPanneau()));
      outilComboBox.setSelectedIndex(0);
    }
    revalidate();
    repaint();
  }
}