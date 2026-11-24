package ca.ulaval.glo2004.GUI;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ca.ulaval.glo2004.Afficheur.AfficheurCoupe;
import ca.ulaval.glo2004.Afficheur.AfficheurPanneau;
import ca.ulaval.glo2004.GUI.gestionnaires.*;
import ca.ulaval.glo2004.controller.CNC_Controller;
import ca.ulaval.glo2004.dto.CoupeDTO;
import ca.ulaval.glo2004.dto.PanneauDTO;

public class DrawingPanel extends JPanel {
    private final CNC_Controller controller;
    private final GestionnaireCoupeVerticale gestionnaireVerticale;
    private final GestionnaireCoupeHorizontale gestionnaireHorizontale;
    private final GestionnaireCoupeRectangle gestionnaireRectangulaire;
    private final GestionnaireCoupeBordure gestionnaireBordure;
    private final GestionnaireCoupeL gestionnaireL;
    private int gridSpacing = 20;
    private PanneauDTO panneauDTO;
    private CoupeDTO selectedCoupe = null;
    private boolean isVerticalMode = false;
    private boolean isHorizontalMode = false;
    private boolean isSelectMode = false;
    private boolean isRectangularMode = false;
    private boolean isBorderMode = false;
    private boolean isLMode = false;
    private boolean showGrid = false;
    private static final int EDGE_MARGIN = 11; // 3mm in pixels (approx)
    private Point dragStart = null;
    private Point premierClic = null; // Pour le premier clic d'une coupe rectangulaire
    private int offsetX = 0;
    private int offsetY = 0;
    private double scaleFactor = 1.0;
    private Point dragOffset = null;

    public DrawingPanel(CNC_Controller controller) {
        this.controller = controller;
        this.panneauDTO = controller.getPanneauDTO();
        this.gestionnaireVerticale = new GestionnaireCoupeVerticale(controller, this);
        this.gestionnaireHorizontale = new GestionnaireCoupeHorizontale(controller, this);
        this.gestionnaireRectangulaire = new GestionnaireCoupeRectangle(controller, this);
        this.gestionnaireBordure = new GestionnaireCoupeBordure(controller, this);
        this.gestionnaireL = new GestionnaireCoupeL(controller, this);
        setBackground(new Color(94, 94, 94));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Point clickPoint = new Point(
                  (int) ((e.getX() - offsetX) / scaleFactor),
                  (int) ((e.getY() - offsetY) / scaleFactor)
                );

                if (isSelectMode) {
                    var selectedCut = controller.contientCoupe(clickPoint);
                    if (selectedCut.isPresent()) {
                        controller.selectionnerCoupe(selectedCut.get().id());
                        var selectedCoupe = controller.getCoupeSelectionnee();
                        selectedCoupe.ifPresent(coupeDTO -> {
                            dragOffset = new Point(
                              clickPoint.x - (int) coupeDTO.origine().getX(),
                              clickPoint.y - (int) coupeDTO.origine().getY()
                            );
                        });
                        updateSelectedCutState(selectedCoupe.get());
                    }
                } else {
                    controller.deselectionnerToutesLesCoupes();
                    repaint();
                    if (isVerticalMode) {
                        gestionnaireVerticale.ajouterCoupeVerticale(clickPoint);
                    } else if (isHorizontalMode) {
                        gestionnaireHorizontale.ajouterCoupeHorizontale(clickPoint);
                    } else if (isRectangularMode) {
                        gestionnaireRectangulaire.handleRectangularCutClick(clickPoint);
                    } else if (isBorderMode) {
                        //gestionnaireBordure.handleBorderCutClick(clickPoint);
                    } else if (isLMode) {
                        gestionnaireL.handleLCutClick(clickPoint);
                    } else {
                        dragStart = e.getPoint();
                    }
                }
                repaint();
            }



            @Override
            public void mouseReleased(MouseEvent e) {
                if (isSelectMode && dragOffset != null) {
                    var selectedCoupe = controller.getCoupeSelectionnee();
                    selectedCoupe.ifPresent(coupeDTO -> {
                        Point newOrigin = transformPoint(e.getPoint());
                        newOrigin.translate(-dragOffset.x, -dragOffset.y);
//                        newOrigin = snapToGrid(newOrigin, gridSpacing);

                        switch (coupeDTO.typeCoupe()) {
                        case VERTICAL -> newOrigin.y = (int) coupeDTO.origine().getY();
                        case HORIZONTAL -> newOrigin.x = (int) coupeDTO.origine().getX();
                        }

                        controller.deplacerCoupeParId(coupeDTO.id(), newOrigin);
                        var updatedCoupe = controller.contientCoupe(newOrigin);
                        updatedCoupe.ifPresent(DrawingPanel.this::creerToolTip);
                    });
                }
                dragStart = null;
                dragOffset = null;
                repaint();

            }});

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isSelectMode && dragOffset != null) {
                    var selectedCoupe = controller.getCoupeSelectionnee();
                    selectedCoupe.ifPresent(coupeDTO -> {
                        Point newOrigin = transformPoint(e.getPoint());
                        newOrigin.translate(-dragOffset.x, -dragOffset.y);
                        newOrigin = snapToGrid(newOrigin, gridSpacing);

                        switch (coupeDTO.typeCoupe()) {
                        case VERTICAL -> newOrigin.y = (int) coupeDTO.origine().getY();
                        case HORIZONTAL -> newOrigin.x = (int) coupeDTO.origine().getX();
                        }

                        controller.deplacerCoupeParId(coupeDTO.id(), newOrigin);
                        var updatedCoupe = controller.contientCoupe(newOrigin);
                        updatedCoupe.ifPresent(DrawingPanel.this::creerToolTip);
                        repaint();
                    });
                } else if (dragStart != null) {
                    int dx = e.getX() - dragStart.x;
                    int dy = e.getY() - dragStart.y;
                    offsetX += dx;
                    offsetY += dy;
                    dragStart = e.getPoint();
                    repaint();
                }
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Point mousePoint = transformPoint(e.getPoint());
                var coupe = controller.contientCoupe(mousePoint);
                coupe.ifPresent(DrawingPanel.this::creerToolTip);
            }
        });

        addMouseWheelListener(e -> zoom(e.getPoint(), e.getPreciseWheelRotation() > 0 ? 0.9 : 1.1));
    }

    private Point snapToGrid(Point point, int gridSpacing) {
        int snappedX = Math.round(point.x / (float) gridSpacing) * gridSpacing;
        int snappedY = Math.round(point.y / (float) gridSpacing) * gridSpacing;
        return new Point(snappedX, snappedY);
    }

    public void refreshFromController() {
        this.panneauDTO = controller.getPanneauDTO();
        selectedCoupe = null;
        premierClic = null;
        repaint();
    }

    public void setShowGrid(boolean showGrid) {
        this.showGrid = showGrid;
        repaint();
    }

    public boolean isShowGrid() {
        return showGrid;
    }

    public void setGridSpacing(int gridSpacing) {
        this.gridSpacing = gridSpacing;
        repaint();
    }

    public void fitToWindow(Dimension windowSize) {
        double widthScale = windowSize.getWidth() / panneauDTO.largeurPanneau();
        double heightScale = windowSize.getHeight() / panneauDTO.longueurPanneau();
        double marginFactor = 0.6;
        scaleFactor = Math.min(widthScale, heightScale) * marginFactor;
        int scaledPanelWidth = (int) (panneauDTO.largeurPanneau() * scaleFactor);
        int scaledPanelHeight = (int) (panneauDTO.longueurPanneau() * scaleFactor);
        offsetX = (windowSize.width - scaledPanelWidth) / 2 - 200;
        offsetY = (windowSize.height - scaledPanelHeight) / 2 - 90;
        repaint();
    }

    private void zoom(Point mousePoint, double zoomFactor) {
        int mouseX = mousePoint.x;
        int mouseY = mousePoint.y;
        offsetX = (int) (mouseX - zoomFactor * (mouseX - offsetX));
        offsetY = (int) (mouseY - zoomFactor * (mouseY - offsetY));
        scaleFactor *= zoomFactor;
        repaint();
    }

    public void deleteSelectedCoupe() {
        var selectedCoupe = controller.getCoupeSelectionnee();
        if (selectedCoupe.isPresent()) {
            try {
                controller.supprimerCoupeParId(selectedCoupe.get().id());
                repaint();
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Aucune coupe sélectionnée.");
        }
    }

    public void ajouterCoupeVerticale(Point start) {
        this.gestionnaireVerticale.ajouterCoupeVerticale(start);
    }

    public void ajouterCoupeHorizontale(Point start) {
        this.gestionnaireHorizontale.ajouterCoupeHorizontale(start);
    }

    public void resetRectangularSelection() {
        premierClic = null; // Réinitialise le premier clic
    }

    public int getCoupeCount() {
        return controller.getPanneauDTO().coupesPanneauDTO().size();
    }

    private void drawGrid(Graphics2D g2d) {
        Color gridColor = new Color(255, 255, 255, 100);
        g2d.setColor(gridColor);
        g2d.setStroke(new BasicStroke(1));
        double largeurPanneau = panneauDTO.largeurPanneau();
        double longueurPanneau = panneauDTO.longueurPanneau();
        for (int x = 0; x <= largeurPanneau; x += gridSpacing) {
            g2d.drawLine(x, 0, x, (int) longueurPanneau);
        }
        for (int y = 0; y <= longueurPanneau; y += gridSpacing) {
            g2d.drawLine(0, y, (int) largeurPanneau, y);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(offsetX, offsetY);
        g2d.scale(scaleFactor, scaleFactor);
        AfficheurPanneau afficheurPanneau = new AfficheurPanneau();
        afficheurPanneau.dessiner(g2d, controller.getPanneauDTO());
        AfficheurCoupe afficheurCoupe = new AfficheurCoupe();
        for (CoupeDTO coupe : panneauDTO.coupesPanneauDTO()) {
            afficheurCoupe.dessiner(g2d, coupe);
        }
        if (isShowGrid()) {
            drawGrid(g2d);
        }
    }

    @Override
    public void repaint() {
        super.repaint();
        firePropertyChange("repaint", false, true);
    }

    private void updateSelectedCutState(CoupeDTO coupe) {
        creerToolTip(coupe);
        repaint();
    }

    private void creerToolTip(CoupeDTO coupe) {
        String dimensionsInfo = switch (coupe.typeCoupe()) {
            case RECTANGULAR -> String.format("Largeur: %d mm, Longueur: %d mm", coupe.largeur(), coupe.longueur());
            case BORDURE -> String.format("Largeur finale: %d mm, Longueur finale: %d mm", coupe.largeur(), coupe.longueur());
            case L -> String.format("Destination: (%d, %d)", (int) coupe.destination().getX(), (int) coupe.destination().getY());
            case VERTICAL -> String.format("Longueur: %d mm", (int) (coupe.destination().getY() - coupe.origine().getY()));
            case HORIZONTAL -> String.format("Largeur: %d mm", (int) (coupe.destination   ().getX() - coupe.origine().getX()));
        };

        String tooltipText = String.format(
          "<html>" +
          "Type: %s<br>" +
          "Position: (%d, %d)<br>" +
          "%s<br>" +
          "Outil: %s</html>",
          coupe.typeCoupe(),
          (int) coupe.origine().getX(),
          (int) coupe.origine().getY(),
          dimensionsInfo,
          coupe.outilDTO().nom());

        if (!tooltipText.equals(getToolTipText())) {
            setToolTipText(tooltipText);
        }
    }
    //transform screen coordinates to panel coordinates
    private Point transformPoint(Point screenPoint) {
        return new Point(
                (int) ((screenPoint.x - offsetX) / scaleFactor),
                (int) ((screenPoint.y - offsetY) / scaleFactor)
        );
    }

    public void setVerticalMode(boolean isVerticalMode) {
        this.isVerticalMode = isVerticalMode;
    }

    public void setHorizontalMode(boolean isHorizontalMode) {
        this.isHorizontalMode = isHorizontalMode;
    }

    public void setSelectionMode(boolean isSelectMode) {
        this.isSelectMode = isSelectMode;
        repaint();
    }

    public void setRectangularMode(boolean isRectangularMode) {
        this.isRectangularMode = isRectangularMode;
    }

    public void setBorderMode(boolean isBorderMode) {
        this.isBorderMode = isBorderMode;
    }

    public void setLMode(boolean isLMode) {
        this.isLMode = isLMode;
    }
    public void resetAllModes() {
        setVerticalMode(false);
        setHorizontalMode(false);
        setSelectionMode(false);
        setRectangularMode(false);
        setBorderMode(false);
        setLMode(false);
        resetRectangularSelection();
    }

}
