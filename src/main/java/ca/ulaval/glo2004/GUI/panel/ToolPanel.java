package ca.ulaval.glo2004.GUI.panel;


import ca.ulaval.glo2004.controller.CNC_Controller;
import ca.ulaval.glo2004.dto.OutilDTO;

import javax.swing.*;
import java.awt.*;
import java.util.UUID;

public class ToolPanel extends JPanel {
  private final JLabel activeToolLabel;
  private final DefaultListModel<String> toolListModel;
  private final JList<String> toolList;
  private final JPanel editPanel;
  private final JTextField editWidthField;
  private final JButton applyEditButton;

  public ToolPanel(CNC_Controller controller, JLabel activeToolLabel, JPanel drawingPanel) {
    super(new BorderLayout());
    this.activeToolLabel = activeToolLabel;

    toolListModel = new DefaultListModel<>();
    toolList = new JList<>(toolListModel);
    toolList.setBorder(BorderFactory.createTitledBorder("Liste des outils"));

    // Ajout de l'outil par défaut à la liste
    OutilDTO defaultTool = controller.getDefaultOutil();
    toolListModel.addElement(String.format("%s (Largeur : %.2f mm)", defaultTool.nom(), defaultTool.largeur()));

    // Section pour créer un outil
    JPanel createToolPanel = new JPanel(new GridLayout(3, 2, 5, 5));
    createToolPanel.setBorder(BorderFactory.createTitledBorder("Créer un outil"));

    createToolPanel.add(new JLabel("Nom :"));
    JTextField toolNameField = new JTextField();
    createToolPanel.add(toolNameField);

    createToolPanel.add(new JLabel("Largeur (mm) :"));
    JTextField toolWidthField = new JTextField();
    createToolPanel.add(toolWidthField);

    JButton createToolButton = new JButton("Ajouter Outil");
    createToolPanel.add(createToolButton);

    add(createToolPanel, BorderLayout.NORTH);

    // Section pour modifier la largeur
    editPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
    editPanel.setBorder(BorderFactory.createTitledBorder("Modifier la largeur"));
    editPanel.add(new JLabel("Nouvelle largeur (mm) :"));
    editWidthField = new JTextField(10);
    editPanel.add(editWidthField);
    applyEditButton = new JButton("Appliquer");
    editPanel.add(applyEditButton);
    editPanel.setVisible(false);

    // Panneau central avec la liste et le panneau d'édition
    JPanel centerPanel = new JPanel(new BorderLayout());
    centerPanel.add(new JScrollPane(toolList), BorderLayout.CENTER);
    centerPanel.add(editPanel, BorderLayout.SOUTH);
    add(centerPanel, BorderLayout.CENTER);

    // Boutons pour la gestion des outils
    JPanel buttonPanel = new JPanel(new FlowLayout());
    JButton setDefaultToolButton = new JButton("Définir comme outil par défaut");
    JButton deleteToolButton = new JButton("Supprimer Outil Sélectionné");
    JButton editWidthButton = new JButton("Modifier la largeur");

    buttonPanel.add(setDefaultToolButton);
    buttonPanel.add(deleteToolButton);
    buttonPanel.add(editWidthButton);
    add(buttonPanel, BorderLayout.SOUTH);

    // Gestion de la sélection dans la liste
    toolList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        boolean hasSelection = toolList.getSelectedValue() != null;
        editWidthButton.setEnabled(hasSelection);
        setDefaultToolButton.setEnabled(hasSelection);
        deleteToolButton.setEnabled(hasSelection);
      }
    });

    // Actions des boutons
    editWidthButton.addActionListener(e -> {
      String selectedTool = toolList.getSelectedValue();
      if (selectedTool != null) {
        String toolName = selectedTool.split(" ")[0];
        var toolDTO = controller.trouverOutilParNom(toolName);

        if (toolDTO.isPresent()) {
          editWidthField.setText(String.valueOf(toolDTO.get().largeur()));
          editPanel.setVisible(true);
          revalidate();
          repaint();
        }
      }
    });

    applyEditButton.addActionListener(e -> {
      String selectedTool = toolList.getSelectedValue();
      if (selectedTool != null) {
        String toolName = selectedTool.split(" ")[0];
        var toolDTO = controller.trouverOutilParNom(toolName);

        try {
          double newWidth = Double.parseDouble(editWidthField.getText());
          if (newWidth <= 0) {
            JOptionPane.showMessageDialog(this,
                                          "La largeur doit être supérieure à 0.",
                                          "Erreur",
                                          JOptionPane.ERROR_MESSAGE);
            return;
          }

          if (toolDTO.isPresent()) {
            OutilDTO updatedTool = new OutilDTO(
              toolDTO.get().id(),
              toolDTO.get().nom(),
              newWidth,
              toolDTO.get().utiliseParDefault()
            );

            controller.modifierLargeurOutil(   toolDTO.get().id(),
                                                newWidth);
            drawingPanel.repaint();
            // Mettre à jour l'affichage dans la liste
            int selectedIndex = toolList.getSelectedIndex();
            toolListModel.removeElementAt(selectedIndex);
            toolListModel.add(selectedIndex,
                              String.format("%s (Largeur : %.2f mm)",
                                            updatedTool.nom(),
                                            updatedTool.largeur()));

            // Mettre à jour le label de l'outil actif si nécessaire
            if (updatedTool.utiliseParDefault()) {
              updateActiveToolLabel(updatedTool);
            }

            // Cacher le panneau d'édition
            editPanel.setVisible(false);
            revalidate();
            repaint();
          }
        } catch (NumberFormatException ex) {
          JOptionPane.showMessageDialog(this,
                                        "Largeur invalide.",
                                        "Erreur",
                                        JOptionPane.ERROR_MESSAGE);
        }
      }
    });

    createToolButton.addActionListener(e -> {
      String toolName = toolNameField.getText();
      String toolWidthText = toolWidthField.getText();

      try {
        double toolWidth = Double.parseDouble(toolWidthText);

        if (toolName.isEmpty() || toolWidth <= 0) {
          JOptionPane.showMessageDialog(this,
                                        "Nom ou largeur invalide.",
                                        "Erreur",
                                        JOptionPane.ERROR_MESSAGE);
          return;
        }

        OutilDTO newTool = new OutilDTO(UUID.randomUUID(), toolName, toolWidth, false);
        controller.ajouterOutil(newTool);

        toolListModel.addElement(String.format("%s (Largeur : %.2f mm)", toolName, toolWidth));
        toolNameField.setText("");
        toolWidthField.setText("");

      } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,
                                      "Largeur invalide.",
                                      "Erreur",
                                      JOptionPane.ERROR_MESSAGE);
      }
    });

    setDefaultToolButton.addActionListener(e -> {
      String selectedTool = toolList.getSelectedValue();
      if (selectedTool != null) {
        String toolName = selectedTool.split(" ")[0];
        var toolDTO = controller.trouverOutilParNom(toolName);

        if (toolDTO.isPresent()) {
          controller.definirOutilParDefaut(toolDTO.get().id());
          updateActiveToolLabel(toolDTO.get());
        }
      }
    });

    deleteToolButton.addActionListener(e -> {
      String selectedTool = toolList.getSelectedValue();
      if (selectedTool != null) {
        String toolName = selectedTool.split(" ")[0];
        var toolDTO = controller.trouverOutilParNom(toolName);

        if (toolDTO.isPresent()) {
          controller.supprimerOutil(toolDTO.get().id());
          toolListModel.removeElement(selectedTool);

          if (toolDTO.get().utiliseParDefault()) {
            OutilDTO nouvelOutilParDefaut = controller.getPanneauDTO().outilsPanneauDTO().isEmpty()
                                            ? null
                                            : controller.getPanneauDTO().outilsPanneauDTO().getFirst();
            if (nouvelOutilParDefaut != null) {
              controller.definirOutilParDefaut(nouvelOutilParDefaut.id());
              updateActiveToolLabel(nouvelOutilParDefaut);
            } else {
              activeToolLabel.setText("Outil actif : Aucun");
            }
          }

          // Cacher le panneau d'édition si visible
          editPanel.setVisible(false);
          revalidate();
          repaint();
        }
      }
    });

    // Désactiver initialement les boutons
    editWidthButton.setEnabled(false);
    setDefaultToolButton.setEnabled(false);
    deleteToolButton.setEnabled(false);
  }

  private void updateActiveToolLabel(OutilDTO toolDTO) {
    activeToolLabel.setText(String.format("Outil actif : %s (Largeur : %.2f mm)",
                                          toolDTO.nom(),
                                          toolDTO.largeur()));
  }
}