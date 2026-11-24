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

public class RectangularCutPanel extends JPanel {
  private final CNC_Controller controller;
  private final DrawingPanel drawingPanel;
  private final JSpinner largeurSpinner;
  private final JSpinner longueurSpinner;
  private final JComboBox<String> referenceComboBox;
  private final JComboBox<String> outilComboBox;
  private final JButton actionButton;

  public RectangularCutPanel(CNC_Controller controller, DrawingPanel drawingPanel) {
    super(new BorderLayout(10, 10));
    this.controller = controller;
    this.drawingPanel = drawingPanel;

    // Configuration des spinners avec les limites du panneau
    largeurSpinner = new JSpinner(new SpinnerNumberModel((int) (this.controller.getPanneauDTO().largeurPanneau() * 0.5),
                                                         1,
                                                         (int) this.controller.getPanneauDTO().largeurPanneau(),
                                                         1));

    longueurSpinner = new JSpinner(new SpinnerNumberModel((int) (this.controller.getPanneauDTO().longueurPanneau() * 0.5),
                                                          1,
                                                          (int) this.controller.getPanneauDTO().longueurPanneau(),
                                                          1));

    referenceComboBox = new JComboBox<>(new String[]{"Centre", "Coin supérieur gauche", "Coin supérieur droit", "Coin inférieur gauche", "Coin inférieur droit"});
    outilComboBox = new JComboBox<>();
    actionButton = new JButton("Ajouter");

    setupLayout();
    setupListeners();
    updateOutilsList();
  }

  private void setupLayout() {
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createTitledBorder("Configuration de la coupe rectangulaire"));

    // Instructions
    JLabel instructionLabel = new JLabel("<html>Entrez les dimensions souhaitées pour la coupe rectangulaire.<br>" +
                                         "Cliquez ensuite sur le panneau pour positionner la coupe.</html>");
    instructionLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

    // Panel pour les dimensions et options
    JPanel configPanel = new JPanel(new GridLayout(4, 2, 10, 10));

    configPanel.add(new JLabel("Point de référence:"));
    configPanel.add(referenceComboBox);

    configPanel.add(new JLabel("Outil:"));
    configPanel.add(outilComboBox);

    configPanel.add(new JLabel("Largeur (mm):"));
    configPanel.add(largeurSpinner);

    configPanel.add(new JLabel("Longueur (mm):"));
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

  private void handleAction() {
    int nouvelleLargeur = (Integer) largeurSpinner.getValue();
    int nouvelleLongueur = (Integer) longueurSpinner.getValue();
    String selectedOutil = (String) outilComboBox.getSelectedItem();

    var coupe = controller.getCoupeSelectionnee();
    if (coupe.isPresent()) {
      controller.redimensionnerCoupeRectangulaire(coupe.get(), nouvelleLargeur, nouvelleLongueur);
      controller.modifierOutilCoupe(coupe.get().id(), selectedOutil);
    } else {
      drawingPanel.resetRectangularSelection();
    }

    repaint();
    drawingPanel.repaint();
    drawingPanel.firePropertyChange("repaint", false, true);
  }

  private void updateOutilsList() {
    outilComboBox.removeAllItems();
    for (var outil : controller.getPanneauDTO().outilsPanneauDTO()) {
      outilComboBox.addItem(outil.nom());
    }
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
      // Réinitialiser aux valeurs par défaut
      largeurSpinner.setValue((int) (controller.getPanneauDTO().largeurPanneau() * 0.5));
      longueurSpinner.setValue((int) (controller.getPanneauDTO().longueurPanneau() * 0.5));
      outilComboBox.setSelectedIndex(0);
      referenceComboBox.setSelectedIndex(0);
    }

    revalidate();
    repaint();
  }
}