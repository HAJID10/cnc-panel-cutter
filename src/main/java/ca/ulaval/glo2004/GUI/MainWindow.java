package ca.ulaval.glo2004.GUI;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import ca.ulaval.glo2004.GUI.panel.BorderCutPanel;
import ca.ulaval.glo2004.GUI.panel.HorizontalCutPanel;
import ca.ulaval.glo2004.GUI.panel.MenuBar;
import ca.ulaval.glo2004.GUI.panel.RectangularCutPanel;
import ca.ulaval.glo2004.GUI.panel.ToolPanel;
import ca.ulaval.glo2004.GUI.panel.VerticalCutPanel;
import ca.ulaval.glo2004.controller.CNC_Controller;
import ca.ulaval.glo2004.domain.TypeCoupe;
import ca.ulaval.glo2004.dto.CoupeDTO;
import ca.ulaval.glo2004.dto.OutilDTO;
import ca.ulaval.glo2004.dto.PanneauDTO;

public class MainWindow extends JFrame {
  private final JLabel lineCountLabel;
  private final JLabel activeToolLabel;
  private final JPanel bottomContainer;
  private final JPanel dynamicRightPanel; // Panel de droite pour les options dynamiques
  private final JLabel selectedCoupeInfoLabel;
  private DrawingPanel drawingPanel;
  private CNC_Controller controller;
  private JButton verticalLineButton;
  private JButton horizontalLineButton;
  private JButton rectangularCutButton;
  private JButton borderCutButton;
  private JButton LCutButton;
  private JButton selectLineButton;

  public MainWindow() {
    // Configuration de la fenêtre principale
    setTitle("CNC Project");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(1200, 800);
    setLocationRelativeTo(null);

    // Initialisation du panneau par défaut
    OutilDTO outilDTO = new OutilDTO(UUID.randomUUID(), "Initial", 4.0, true);
    PanneauDTO panneauDTO = new PanneauDTO(UUID.randomUUID(), 914.4, 1219.2, 18.0, new ArrayList<>(), List.of(outilDTO));
    controller = new CNC_Controller(panneauDTO);
    controller.definirOutilParDefaut(outilDTO.id());
    drawingPanel = new DrawingPanel(controller);

    lineCountLabel = new JLabel("Nombre de coupe: 0");
    selectedCoupeInfoLabel = new JLabel("Coupe sélectionnée : Aucune    ");
    activeToolLabel = new JLabel("Outil actif : Aucun");
    updateActiveToolLabel(outilDTO);

    // Configuration de la barre de menu
    setJMenuBar(new MenuBar(this, controller).createMenuBar());

    // Mise en page principale
    setLayout(new BorderLayout());
    add(drawingPanel, BorderLayout.CENTER);
    drawingPanel.fitToWindow(new Dimension(getWidth(), getHeight()));
    //INITIALISATION DES INFOS DE LA COUPE

    // --- Conteneur inférieur ---
    bottomContainer = new JPanel(new BorderLayout());
    JPanel lineAndGridPanel = new JPanel(new BorderLayout());
    lineAndGridPanel.add(lineCountLabel, BorderLayout.WEST);
    lineAndGridPanel.add(selectedCoupeInfoLabel, BorderLayout.EAST);
    bottomContainer.add(lineAndGridPanel, BorderLayout.NORTH);

    JTabbedPane tabbedPane = createTabbedPane();
    bottomContainer.add(tabbedPane, BorderLayout.CENTER);

    adjustBottomPanelHeight(120); // Ajuste la hauteur pour l'onglet sélectionné
    add(bottomContainer, BorderLayout.SOUTH);

    //
    // --- Panneau latéral droit dynamique ---
    dynamicRightPanel = new JPanel(new BorderLayout());
    dynamicRightPanel.setBorder(BorderFactory.createTitledBorder("Options"));
    dynamicRightPanel.setPreferredSize(new Dimension(400, getHeight()));
    setDefaultRightPanel();
    add(dynamicRightPanel, BorderLayout.EAST);

    drawingPanel.addPropertyChangeListener("repaint", evt -> {
      updateLineCount();
      updateSelectedCoupeInfo();
    });

  }

  public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
      MainWindow mainWindow = new MainWindow();
      mainWindow.setVisible(true);
      mainWindow.drawingPanel.fitToWindow(new Dimension(mainWindow.getWidth(), mainWindow.getHeight()));
    });
  }

  private void updateSelectedCoupeInfo() {
    var selectedCoupe = controller.getCoupeSelectionnee();
    selectedCoupeInfoLabel.setText(selectedCoupe.map(coupeDTO -> String.format("Coupe sélectionnée: %s - %s - %s",
                                                                               coupeDTO.id(),
                                                                               coupeDTO.origine(),
                                                                               coupeDTO.destination()))
                                                .orElse("Aucune coupe sélectionnée"));

    selectedCoupe.ifPresentOrElse(coupe -> {
      switch (coupe.typeCoupe()) {
      case VERTICAL -> updateCoupeVertical();
      case HORIZONTAL -> updateCoupeHorizontale();
      case RECTANGULAR -> updateCoupeRectangle();
      case BORDURE -> updateCoupeBordure();
      case L -> { /* rien faire */ }
      default -> setDefaultRightPanel();
      }
    }, this::setDefaultRightPanel);

    repaint();
  }

    private void setDefaultRightPanel() {
        dynamicRightPanel.removeAll();

        JPanel gridPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

        JButton toggleGridButton = new JButton("Afficher Grille");

        toggleGridButton.addActionListener(e -> {
            boolean currentState = drawingPanel.isShowGrid();
            drawingPanel.setShowGrid(!currentState);

            if (drawingPanel.isShowGrid()) {
                toggleGridButton.setText("Afficher Grille Off");
            } else {
                toggleGridButton.setText("Afficher Grille On");
            }

            drawingPanel.repaint();
            dynamicRightPanel.revalidate();
            dynamicRightPanel.repaint();
        });
        if (drawingPanel.isShowGrid()) {
            toggleGridButton.setText("Masquer Grille");
        } else {
            toggleGridButton.setText("Afficher Grille");
        }

        gridPanel.add(toggleGridButton);

        JLabel gridSpacingLabel = new JLabel("Espacement de la grille:");
        JSpinner gridSpacingSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 100, 1));
        JLabel gridSpacingUnitLabel = new JLabel("po");
        gridSpacingSpinner.addChangeListener(e -> {
            int gridSpacing = (int) gridSpacingSpinner.getValue();
            drawingPanel.setGridSpacing(gridSpacing); // Set grid spacing
            drawingPanel.repaint();  // Repaint the panel to reflect new spacing
        });
        gridPanel.add(gridSpacingLabel);
        gridPanel.add(gridSpacingSpinner);
        gridPanel.add(gridSpacingUnitLabel);

        dynamicRightPanel.add(activeToolLabel, BorderLayout.NORTH);
        dynamicRightPanel.add(gridPanel, BorderLayout.SOUTH);

        dynamicRightPanel.revalidate();
        dynamicRightPanel.repaint();
    }


  private void setCoupeOptionsPanel(TypeCoupe typeCoupe) {
    dynamicRightPanel.removeAll();

    switch (typeCoupe) {
    case VERTICAL -> {
      dynamicRightPanel.add(new VerticalCutPanel(controller, drawingPanel), BorderLayout.CENTER);
      dynamicRightPanel.revalidate();
    }
    case HORIZONTAL -> {
      HorizontalCutPanel horizontalCutPanel = new HorizontalCutPanel(controller, drawingPanel);
      dynamicRightPanel.add(horizontalCutPanel, BorderLayout.CENTER);
      dynamicRightPanel.revalidate();
    }
    case RECTANGULAR -> {
        RectangularCutPanel rectangularCutPanel = new RectangularCutPanel(controller, drawingPanel);
        dynamicRightPanel.add(rectangularCutPanel, BorderLayout.CENTER);
        dynamicRightPanel.revalidate();

    }
    case BORDURE -> {
      BorderCutPanel borderCutPanel = new BorderCutPanel(controller, drawingPanel);
        dynamicRightPanel.add(borderCutPanel, BorderLayout.CENTER);
        dynamicRightPanel.revalidate();
    }
    case L -> {

    }

    }

    revalidate();
    repaint();
  }


  private void setLCutOptionsPanel() {
    dynamicRightPanel.removeAll();

    JPanel optionsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
    optionsPanel.setBorder(BorderFactory.createTitledBorder("Options Coupe en L"));

    JLabel instructionLabel = new JLabel(
      "<html>Cliquez trois points pour définir la coupe en L:<br>" +
      "1. Point de départ<br>" +
      "2. Point de coude<br>" +
      "3. Point d'arrivée</html>"
    );
    optionsPanel.add(instructionLabel);

    dynamicRightPanel.add(activeToolLabel, BorderLayout.NORTH);
    dynamicRightPanel.add(optionsPanel, BorderLayout.CENTER);

    revalidate();
    repaint();
  }

  private JTabbedPane createTabbedPane() {
    JTabbedPane tabbedPane = new JTabbedPane();

    JPanel coupePanel = createCoupePanel();
    JPanel coupeContainer = new JPanel(new BorderLayout());
    coupeContainer.add(coupePanel, BorderLayout.CENTER);
    coupeContainer.setPreferredSize(new Dimension(0, 120));
    tabbedPane.addTab("Coupe", coupeContainer);

    JPanel outilPanel = new ToolPanel(controller, activeToolLabel, drawingPanel);
    JPanel outilContainer = new JPanel(new BorderLayout());
    outilContainer.add(outilPanel, BorderLayout.CENTER);
    outilContainer.setPreferredSize(new Dimension(0, 300)); // Onglet outil nécessite plus de place
    tabbedPane.addTab("Outil", outilContainer);

    // Ajouter un ChangeListener pour basculer entre les hauteurs
    tabbedPane.addChangeListener(e -> {
      if (tabbedPane.getSelectedIndex() == 0) { // Index 0 correspond à "Coupe"
        selectedCoupeInfoLabel.setVisible(true);
        adjustBottomPanelHeight(150); // Ajuste la hauteur pour "Coupe"
      } else { // Onglet "Outil"
        selectedCoupeInfoLabel.setVisible(false);
        adjustBottomPanelHeight(350); // Ajuste la hauteur pour "Outil"
      }
    });

    return tabbedPane;
  }

  private JPanel createCoupePanel() {
    JPanel coupePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

    verticalLineButton = createToggleButton("Coupe Verticale", e -> {
      boolean activate = toggleMode(verticalLineButton, drawingPanel::setVerticalMode);
      if (activate) {
        setCoupeOptionsPanel(TypeCoupe.VERTICAL);
      }
    });

    horizontalLineButton = createToggleButton("Coupe Horizontale", e -> {
      boolean activate = toggleMode(horizontalLineButton, drawingPanel::setHorizontalMode);
      if (activate) {
        setCoupeOptionsPanel(TypeCoupe.HORIZONTAL);
      }
    });
    borderCutButton = createToggleButton("Coupe de Bordure", e -> {
      boolean activate = toggleMode(borderCutButton, drawingPanel::setBorderMode);
      if (activate) {
        setCoupeOptionsPanel(TypeCoupe.BORDURE);
      }
    });

    rectangularCutButton = createToggleButton("Coupe Rectangulaire", e -> {
      boolean activate = toggleMode(rectangularCutButton, drawingPanel::setRectangularMode);
      if (activate) {
        setCoupeOptionsPanel(TypeCoupe.RECTANGULAR);
      } else {
        drawingPanel.resetRectangularSelection();
        setDefaultRightPanel();
      }
    });

    LCutButton = createToggleButton("Coupe en L", e -> {
      boolean activate = toggleMode(LCutButton, drawingPanel::setLMode);
      if (activate) {
        setLCutOptionsPanel();
      }
    });

    selectLineButton = createToggleButton("Sélectionner Coupe", e -> {
      boolean activate = toggleMode(selectLineButton, drawingPanel::setSelectionMode);
      if (!activate) {
        setDefaultRightPanel();
        drawingPanel.repaint();
        repaint();
        //        }
      }
    });

    JButton deleteLineButton = new JButton("Supprimer Coupe");
    deleteLineButton.addActionListener(e -> {
      drawingPanel.deleteSelectedCoupe();
      updateLineCount();
    });

    coupePanel.add(verticalLineButton);
    coupePanel.add(horizontalLineButton);
    coupePanel.add(rectangularCutButton);
    coupePanel.add(borderCutButton);
    coupePanel.add(LCutButton);
    coupePanel.add(selectLineButton);
    coupePanel.add(deleteLineButton);

    return coupePanel;
  }

  private JButton createToggleButton(String baseText, ActionListener listener) {
    JButton button = new JButton("Activer " + baseText);
    button.addActionListener(listener);
    return button;
  }

  private boolean toggleMode(JButton button, Consumer<Boolean> modeSetterFunction) {
    boolean activate = button.getText().startsWith("Activer");

    resetModes();

    if (activate) {
      modeSetterFunction.accept(true);
      button.setText("Désactiver " + button.getText().substring(8));
    }

    return activate;
  }

  private void updateCoupeVertical() {
    // Vérifie si un VerticalCutPanel existe déjà
    VerticalCutPanel existingPanel = null;
    for (Component comp : dynamicRightPanel.getComponents()) {
      if (comp instanceof VerticalCutPanel) {
        existingPanel = (VerticalCutPanel) comp;
        break;
      }
    }

    // Si le panneau n'existe pas, en créer un nouveau
    if (existingPanel == null) {
      dynamicRightPanel.removeAll();
      existingPanel = new VerticalCutPanel(controller, drawingPanel);
      dynamicRightPanel.add(existingPanel, BorderLayout.CENTER);
      dynamicRightPanel.revalidate();
    }

    // Mettre à jour la coupe sélectionnée
    existingPanel.setSelectedCoupe();
    dynamicRightPanel.repaint();
  }

  private void updateCoupeHorizontale() {
    // Vérifie si un HorizontalCutPanel existe déjà
    HorizontalCutPanel existingPanel = null;
    for (Component comp : dynamicRightPanel.getComponents()) {
      if (comp instanceof HorizontalCutPanel) {
        existingPanel = (HorizontalCutPanel) comp;
        break;
      }
    }

    // Si le panneau n'existe pas, en créer un nouveau
    if (existingPanel == null) {
      dynamicRightPanel.removeAll();
      existingPanel = new HorizontalCutPanel(controller, drawingPanel);
      dynamicRightPanel.add(existingPanel, BorderLayout.CENTER);
      dynamicRightPanel.revalidate();
    }

    // Mettre à jour la coupe sélectionnée
    existingPanel.setSelectedCoupe();
    dynamicRightPanel.repaint();
  }
  private void updateCoupeBordure() {
    BorderCutPanel existingPanel = null;
    for (Component comp : dynamicRightPanel.getComponents()) {
      if (comp instanceof BorderCutPanel) {
        existingPanel = (BorderCutPanel) comp;
        break;
      }
    }

    // Si le panneau n'existe pas, en créer un nouveau
    if (existingPanel == null) {
      dynamicRightPanel.removeAll();
      existingPanel = new BorderCutPanel(controller, drawingPanel);
      dynamicRightPanel.add(existingPanel, BorderLayout.CENTER);
      dynamicRightPanel.revalidate();
    }

    // Mettre à jour la coupe sélectionnée
    existingPanel.setSelectedCoupe();
    dynamicRightPanel.repaint();
  }
  private void updateCoupeRectangle() {
    RectangularCutPanel existingPanel = null;
    for (Component comp : dynamicRightPanel.getComponents()) {
      if (comp instanceof RectangularCutPanel) {
        existingPanel = (RectangularCutPanel) comp;
        break;
      }
    }

    // Si le panneau n'existe pas, en créer un nouveau
    if (existingPanel == null) {
      dynamicRightPanel.removeAll();
      existingPanel = new RectangularCutPanel(controller, drawingPanel);
      dynamicRightPanel.add(existingPanel, BorderLayout.CENTER);
      dynamicRightPanel.revalidate();
    }

    // Mettre à jour la coupe sélectionnée
    existingPanel.setSelectedCoupe();
    dynamicRightPanel.repaint();
  }


  // Ajuste la hauteur du panneau inférieur
  private void adjustBottomPanelHeight(int height) {
    bottomContainer.setPreferredSize(new Dimension(getWidth(), height));
    revalidate();
    repaint();
  }

  public void updateLineCount() {
    lineCountLabel.setText("Nombre de coupe: " + drawingPanel.getCoupeCount());
    repaint();
  }

  // Met à jour l'affichage de l'outil actif
  private void updateActiveToolLabel(OutilDTO toolDTO) {
    activeToolLabel.setText(String.format("Outil actif : %s (Largeur : %.2f mm)", toolDTO.nom(), toolDTO.largeur()));
  }

  private void resetModes() {
    drawingPanel.resetAllModes();

    setDefaultRightPanel();

    resetButtons(Map.of(
      verticalLineButton, "Coupe Verticale",
      horizontalLineButton, "Coupe Horizontale",
      rectangularCutButton, "Coupe Rectangulaire",
      selectLineButton, "Sélectionner Coupe"));
  }

  private void resetButtons(Map<JButton, String> buttonTextMap) {
    buttonTextMap.forEach((button, baseText) -> {
      if (button != null && !button.getText().startsWith("Activer ")) {
        button.setText("Activer " + baseText);
      }
    });
  }


  public DrawingPanel getDrawingPanel() {
    return drawingPanel;
  }
}
