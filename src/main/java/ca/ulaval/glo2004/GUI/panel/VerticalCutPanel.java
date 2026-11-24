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

public class VerticalCutPanel extends JPanel {
    private final CNC_Controller controller;
    private final DrawingPanel drawingPanel;
    private final JComboBox<String> referenceComboBox;
    private final JComboBox<String> existingCoupeComboBox;
    private final JComboBox<String> positionComboBox;
    private final JComboBox<String> outilComboBox;
    private final JTextField distanceField;
    private final JButton actionButton;

    public VerticalCutPanel(CNC_Controller controller, DrawingPanel drawingPanel) {
        super(new BorderLayout(10, 10));
        this.controller = controller;
        this.drawingPanel = drawingPanel;

        referenceComboBox = new JComboBox<>(new String[]{"Côté gauche", "Côté droit", "Coupe existante"});
        existingCoupeComboBox = new JComboBox<>();
        positionComboBox = new JComboBox<>(new String[]{"À gauche", "À droite"});
        outilComboBox = new JComboBox<>();  // Initialisation du ComboBox pour les outils
        distanceField = new JTextField(10);
        actionButton = new JButton("Ajouter");

        setupLayout();
        setupListeners();
        updateExistingCoupesList();
        updateOutilsList();
    }

    private void setupLayout() {
        JPanel mainPanel = new JPanel(new GridLayout(6, 2, 5, 5));  // Augmenté à 6 rangées pour inclure l'outil
        mainPanel.setBorder(BorderFactory.createTitledBorder("Configuration de la coupe verticale"));

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
            Point position = calculatePosition(distance);
            String selectedOutil = (String) outilComboBox.getSelectedItem();  // Récupération de l'outil sélectionné

            var coupe = controller.getCoupeSelectionnee();
            if (coupe.isPresent()) {
                controller.deplacerCoupeParId(coupe.get().id(), position);
                controller.modifierOutilCoupe(coupe.get().id(), selectedOutil);  // Mise à jour de l'outil
                this.drawingPanel.repaint();
                this.drawingPanel.firePropertyChange("repaint", false, true);
            } else {
                controller.ajouterCoupeVerticale(position);
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
            distanceField.setText(String.valueOf((int) coupe.get().origine().getX()));
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

    private Point calculatePosition(double distance) {
        if (referenceComboBox.getSelectedIndex() == 0) {
            return new Point((int) distance, 0);
        } else if (referenceComboBox.getSelectedIndex() == 1) {
            return new Point((int) (controller.getPanneauDTO().largeurPanneau() - distance), 0);
        } else {
            int selectedIndex = existingCoupeComboBox.getSelectedIndex();
            CoupeDTO selectedCoupe = controller.getPanneauDTO().coupesPanneauDTO().get(selectedIndex);
            int x = positionComboBox.getSelectedIndex() == 0
                    ? (int) (selectedCoupe.origine().x - distance)
                    : (int) (selectedCoupe.origine().x + distance);
            return new Point(x, 0);
        }
    }
}