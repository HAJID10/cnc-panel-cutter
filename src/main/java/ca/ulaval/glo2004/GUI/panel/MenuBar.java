package ca.ulaval.glo2004.GUI.panel;

import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import ca.ulaval.glo2004.GUI.MainWindow;
import ca.ulaval.glo2004.controller.CNC_Controller;
import ca.ulaval.glo2004.dto.OutilDTO;
import ca.ulaval.glo2004.dto.PanneauDTO;

public class MenuBar {
  private final MainWindow mainWindow;
  private final CNC_Controller controller;

  public MenuBar(MainWindow mainWindow, CNC_Controller controller) {
    this.mainWindow = mainWindow;
    this.controller = controller;
  }

  public JMenuBar createMenuBar() {
    JMenuBar menuBar = new JMenuBar();

    JMenu fileMenu = createFileMenu();
    JMenu panelMenu = createPanelMenu();
    JButton undoButton = createUndoButton();
    JButton redoButton = createRedoButton();

    menuBar.add(fileMenu);
    menuBar.add(panelMenu);
    menuBar.add(undoButton);
    menuBar.add(redoButton);

    return menuBar;
  }

  private JMenu createFileMenu() {
    JMenu fileMenu = new JMenu("Fichier");

    JMenuItem saveItem = new JMenuItem("Sauvegarder");
    saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
    saveItem.addActionListener(e -> saveProject());

    JMenuItem loadItem = new JMenuItem("Charger");
    loadItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
    loadItem.addActionListener(e -> loadProject());

    fileMenu.add(saveItem);
    fileMenu.add(loadItem);

    return fileMenu;
  }

  private JMenu createPanelMenu() {
    JMenu panelMenu = new JMenu("Panneau");

    JMenuItem newPanelItem = new JMenuItem("Nouveau Panneau");
    newPanelItem.addActionListener(e -> this.createNewPanel());

    panelMenu.add(newPanelItem);
    return panelMenu;
  }

  private JButton createUndoButton() {
    JButton undoButton = new JButton("↶");
    undoButton.setToolTipText("Annuler (Ctrl+Z)");
    undoButton.addActionListener(e -> {
      controller.undo();
      mainWindow.getDrawingPanel().repaint();
    });
    return undoButton;
  }

  private JButton createRedoButton() {
    JButton redoButton = new JButton("↷");
    redoButton.setToolTipText("Rétablir (Ctrl+Y)");
    redoButton.addActionListener(e -> {
      controller.redo();
      mainWindow.getDrawingPanel().repaint();
    });
    return redoButton;
  }

  private void saveProject() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers CNC", "cnc"));

    if (fileChooser.showSaveDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
      String filePath = fileChooser.getSelectedFile().getPath();
      if (!filePath.endsWith(".cnc")) {
        filePath += ".cnc";
      }

      try {
        controller.savePanneau(filePath);
        JOptionPane.showMessageDialog(mainWindow, "Projet sauvegardé avec succès!");
      } catch (IOException e) {
        JOptionPane.showMessageDialog(mainWindow,
                                      "Erreur lors de la sauvegarde: " + e.getMessage(),
                                      "Erreur",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }

  private void loadProject() {
    JFileChooser fileChooser = new JFileChooser();
    fileChooser.setFileFilter(new FileNameExtensionFilter("Fichiers CNC", "cnc"));

    if (fileChooser.showOpenDialog(mainWindow) == JFileChooser.APPROVE_OPTION) {
      try {
        controller.loadPanneau(fileChooser.getSelectedFile().getPath());
        mainWindow.getDrawingPanel().refreshFromController();
        mainWindow.updateLineCount();
        JOptionPane.showMessageDialog(mainWindow, "Projet chargé avec succès!");
      } catch (IOException | ClassNotFoundException e) {
        JOptionPane.showMessageDialog(mainWindow,
                                      "Erreur lors du chargement: " + e.getMessage(),
                                      "Erreur",
                                      JOptionPane.ERROR_MESSAGE);
      }
    }
  }
  public void createNewPanel() {
    // Créer un panneau pour demander les dimensions
    JPanel inputPanel = new JPanel(new GridLayout(4, 2, 10, 10));

    // Champs pour entrer les dimensions
    JTextField longueurField = new JTextField("914.4");
    JTextField largeurField = new JTextField("1219.2");
    JTextField epaisseurField = new JTextField("18.0");

    // Labels pour les unités
    JLabel longueurLabel = new JLabel("Longueur (mm) :");
    JLabel largeurLabel = new JLabel("Largeur (mm) :");
    JLabel epaisseurLabel = new JLabel("Épaisseur (mm) :");

    // Case à cocher pour entrer les dimensions en mètres
    JCheckBox metresCheckBox = new JCheckBox("Entrer les dimensions en mètres");
    metresCheckBox.addActionListener(e -> {
      if (metresCheckBox.isSelected()) {
        longueurField.setText(String.valueOf(Double.parseDouble(longueurField.getText()) / 1000));
        largeurField.setText(String.valueOf(Double.parseDouble(largeurField.getText()) / 1000));
        epaisseurField.setText(String.valueOf(Double.parseDouble(epaisseurField.getText()) / 1000));

        longueurLabel.setText("Longueur (m) :");
        largeurLabel.setText("Largeur (m) :");
        epaisseurLabel.setText("Épaisseur (m) :");
      } else {
        longueurField.setText(String.valueOf(Double.parseDouble(longueurField.getText()) * 1000));
        largeurField.setText(String.valueOf(Double.parseDouble(largeurField.getText()) * 1000));
        epaisseurField.setText(String.valueOf(Double.parseDouble(epaisseurField.getText()) * 1000));

        longueurLabel.setText("Longueur (mm) :");
        largeurLabel.setText("Largeur (mm) :");
        epaisseurLabel.setText("Épaisseur (mm) :");
      }
    });

    inputPanel.add(longueurLabel);
    inputPanel.add(longueurField);
    inputPanel.add(largeurLabel);
    inputPanel.add(largeurField);
    inputPanel.add(epaisseurLabel);
    inputPanel.add(epaisseurField);
    inputPanel.add(metresCheckBox);
    inputPanel.add(new JLabel());

    int result = JOptionPane.showConfirmDialog(
      mainWindow,
      inputPanel,
      "Dimensions du nouveau panneau",
      JOptionPane.OK_CANCEL_OPTION,
      JOptionPane.PLAIN_MESSAGE
                                              );

    if (result == JOptionPane.OK_OPTION) {
      try {
        double longueur = Double.parseDouble(longueurField.getText());
        double largeur = Double.parseDouble(largeurField.getText());
        double epaisseur = Double.parseDouble(epaisseurField.getText());

        if (metresCheckBox.isSelected()) {
          longueur *= 1000;
          largeur *= 1000;
          epaisseur *= 1000;
        }

        if (longueur <= 0 || largeur <= 0 || epaisseur <= 0) {
          JOptionPane.showMessageDialog(mainWindow, "Les dimensions doivent être positives.", "Erreur", JOptionPane.ERROR_MESSAGE);
          return;
        }

        int confirmation = JOptionPane.showConfirmDialog(
          mainWindow,
          "Voulez-vous vraiment créer un nouveau panneau avec ces dimensions ?",
          "Confirmation",
          JOptionPane.YES_NO_OPTION
                                                        );

        if (confirmation == JOptionPane.YES_OPTION) {
          OutilDTO outilDTO = new OutilDTO(UUID.randomUUID(), "Initial", 4.0, true);
          PanneauDTO newPanneauDTO = new PanneauDTO(UUID.randomUUID(), longueur, largeur, epaisseur, new ArrayList<>(), List.of(outilDTO));
          this.controller.creerNouveauPanneau(newPanneauDTO);

          this.mainWindow.updateLineCount();
          mainWindow.revalidate();
          mainWindow.repaint();
        }
      } catch (NumberFormatException e) {
        JOptionPane.showMessageDialog(mainWindow, "Veuillez entrer des valeurs numériques valides.", "Erreur", JOptionPane.ERROR_MESSAGE);
      }
    }
  }


}