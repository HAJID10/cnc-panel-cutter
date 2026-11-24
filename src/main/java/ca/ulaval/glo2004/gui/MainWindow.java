package ca.ulaval.glo2004.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.dto.PanelDto;
import ca.ulaval.glo2004.dto.ToolDto;
import ca.ulaval.glo2004.gui.DrawingPanel.Mode;
import ca.ulaval.glo2004.gui.panels.BorderCutPanel;
import ca.ulaval.glo2004.gui.panels.CutEditorPanel;
import ca.ulaval.glo2004.gui.panels.RectangularCutPanel;
import ca.ulaval.glo2004.gui.panels.StraightCutPanel;
import ca.ulaval.glo2004.gui.panels.ToolPanel;

/**
 * Main window: drawing area in the centre, cut and tool tabs at the bottom, and a side panel on the right that
 * shows the editor matching the current mode or the selected cut.
 */
public class MainWindow extends JFrame {
    private final CncController controller;
    private final DrawingPanel drawingPanel;
    private final ToolPanel toolPanel;
    private final JPanel sidePanel = new JPanel(new BorderLayout());
    private JPanel defaultSide;
    private final JLabel status = new JLabel(" ");
    private final JLabel activeTool = new JLabel();
    private final Map<Mode, JToggleButton> modeButtons = new EnumMap<>(Mode.class);
    private final ButtonGroup modeGroup = new ButtonGroup();
    private final Map<CutType, CutEditorPanel> editors = new EnumMap<>(CutType.class);

    // What the side panel currently shows, to avoid rebuilding it (and losing typed values) on every refresh.
    private Object sideKey;

    public MainWindow() {
        super("CNC Panel Cutter");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 820);
        setLocationRelativeTo(null);

        ToolDto saw = new ToolDto(UUID.randomUUID(), "Default bit", 4.0, true);
        controller = new CncController(new PanelDto(UUID.randomUUID(), 914.4, 1219.2, 18.0, new ArrayList<>(), List.of(saw)));
        drawingPanel = new DrawingPanel(controller, this::refresh);
        toolPanel = new ToolPanel(controller, this::refresh);

        setJMenuBar(new AppMenuBar(this, controller));
        setLayout(new BorderLayout());
        add(drawingPanel, BorderLayout.CENTER);

        buildSidePanels();
        sidePanel.setPreferredSize(new Dimension(330, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        add(sidePanel, BorderLayout.EAST);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Cuts", createCutTab());
        tabs.addTab("Tools", toolPanel);
        tabs.setPreferredSize(new Dimension(0, 170));
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(tabs, BorderLayout.CENTER);
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        bottom.add(status, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        bindKeys();

        // Fit the panel the first time the drawing area gets a size.
        drawingPanel.addComponentListener(new ComponentAdapter() {
            private boolean fitted;

            @Override
            public void componentResized(ComponentEvent e) {
                if (!fitted) {
                    fitted = true;
                    drawingPanel.fitToWindow();
                }
            }
        });
        refresh();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Native look (Windows, macOS, GTK); the default Metal look is used if it is not available.
                javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
            } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException e) {
                // keep the default look
            }
            new MainWindow().setVisible(true);
        });
    }

    private JPanel createCutTab() {
        JPanel tab = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addModeButton(tab, "Select / move", Mode.SELECT);
        addModeButton(tab, "Vertical", Mode.VERTICAL);
        addModeButton(tab, "Horizontal", Mode.HORIZONTAL);
        addModeButton(tab, "Rectangle", Mode.RECTANGULAR);
        addModeButton(tab, "L-shape", Mode.L);
        addModeButton(tab, "Border", Mode.BORDER);
        JButton delete = new JButton("Delete selected cut");
        delete.addActionListener(e -> deleteSelected());
        tab.add(delete);
        return tab;
    }

    /** Mode buttons work like radio buttons; clicking the active one again turns it off. */
    private void addModeButton(JPanel tab, String text, Mode mode) {
        JToggleButton button = new JToggleButton(text);
        button.addActionListener(e -> {
            if (drawingPanel.getMode() == mode) {
                modeGroup.clearSelection();
                setMode(Mode.NONE);
            } else {
                setMode(mode);
            }
        });
        modeGroup.add(button);
        modeButtons.put(mode, button);
        tab.add(button);
    }

    private void setMode(Mode mode) {
        drawingPanel.setMode(mode);
        if (mode != Mode.SELECT) {
            controller.deselectAll();
        }
        refresh();
    }

    /** Creates the side panels. Called again when the unit changes, because their labels show the unit. */
    private void buildSidePanels() {
        editors.put(CutType.VERTICAL, new StraightCutPanel(CutType.VERTICAL, controller, this::refresh));
        editors.put(CutType.HORIZONTAL, new StraightCutPanel(CutType.HORIZONTAL, controller, this::refresh));
        editors.put(CutType.RECTANGULAR, new RectangularCutPanel(controller, this::refresh));
        editors.put(CutType.BORDER, new BorderCutPanel(controller, this::refresh));
        defaultSide = createDefaultSide();
        sideKey = null; // forces the side panel to be shown again
    }

    /** Called by the View menu. */
    public void unitsChanged() {
        buildSidePanels();
        refresh();
    }

    private JPanel createDefaultSide() {
        JPanel panel = new JPanel(new BorderLayout(6, 6));
        panel.setBorder(BorderFactory.createTitledBorder("View"));

        JCheckBox grid = new JCheckBox("Show grid (cuts snap to it when dragged)", drawingPanel.isShowGrid());
        grid.addActionListener(e -> drawingPanel.setShowGrid(grid.isSelected()));
        // The spinner shows the spacing in the current unit; the drawing panel keeps it in mm.
        double step = Units.current() == Units.Unit.MM ? 1 : 0.125;
        double shown = Math.round(Units.toDisplay(drawingPanel.getGridSpacing()) / step) * step;
        JSpinner spacing = new JSpinner(new SpinnerNumberModel(Math.max(step, shown), step, 1000, step));
        spacing.addChangeListener(e -> drawingPanel.setGridSpacing(Units.toMm((Double) spacing.getValue())));
        JPanel spacingRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        spacingRow.add(new JLabel(Units.label("Grid spacing")));
        spacingRow.add(spacing);

        JButton fit = new JButton("Fit panel to window");
        fit.addActionListener(e -> drawingPanel.fitToWindow());

        JPanel rows = new JPanel(new java.awt.GridLayout(0, 1, 4, 4));
        rows.add(activeTool);
        rows.add(grid);
        rows.add(spacingRow);
        rows.add(fit);
        rows.add(new JLabel("<html><i>Wheel: zoom. Drag an empty spot: pan.</i></html>"));
        panel.add(rows, BorderLayout.NORTH);
        return panel;
    }

    private void bindKeys() {
        JComponent root = getRootPane();
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
        root.getActionMap().put("delete", action(this::deleteSelected));
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "cancel");
        root.getActionMap().put("cancel", action(drawingPanel::cancelPending));
    }

    private static AbstractAction action(Runnable runnable) {
        return new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                runnable.run();
            }
        };
    }

    private void deleteSelected() {
        controller.getSelectedCut().ifPresent(cut -> controller.deleteCut(cut.id()));
        refresh();
    }

    /** Called after a new panel is created or a file is opened. */
    public void panelReplaced() {
        refresh();
        drawingPanel.fitToWindow();
    }

    /** Brings every part of the window up to date with the controller. */
    public void refresh() {
        PanelDto panel = controller.getPanel();
        Optional<CutDto> selected = controller.getSelectedCut();

        Optional<ToolDto> tool = controller.getDefaultTool();
        activeTool.setText(tool.map(t -> "Tool for new cuts: " + Units.describe(t)).orElse("No tool for new cuts"));
        status.setText(String.format("Panel %s × %s × %s  |  %d cut(s)%s", Units.format(panel.width()),
                Units.format(panel.length()), Units.withUnit(panel.thickness()), panel.cuts().size(),
                selected.map(c -> "  |  Selected: " + c.type() + " at " + DrawingPanel.point(c.origin())).orElse("")));

        toolPanel.refresh();
        updateSidePanel(panel, selected);
        drawingPanel.repaint();
    }

    /** Shows the editor of the selected cut, the creation form of the current mode, or the view settings. */
    private void updateSidePanel(PanelDto panel, Optional<CutDto> selected) {
        Mode mode = drawingPanel.getMode();
        Object key;
        JComponent content;
        if (mode == Mode.SELECT && selected.isPresent() && editors.containsKey(selected.get().type())) {
            CutDto cut = selected.get();
            key = cut; // records compare by value: a moved cut gives a new key and refills the form
            content = editors.get(cut.type());
            if (!Objects.equals(key, sideKey)) {
                ((CutEditorPanel) content).showEdit(cut);
            }
        } else if (mode != Mode.NONE && mode != Mode.SELECT && editors.containsKey(CutType.valueOf(mode.name()))) {
            key = List.of(mode, panel.tools()); // a tool change must refresh the tool list of the form
            content = editors.get(CutType.valueOf(mode.name()));
            if (!Objects.equals(key, sideKey)) {
                ((CutEditorPanel) content).showCreate();
            }
        } else {
            key = "default";
            content = defaultSide;
        }
        if (!Objects.equals(key, sideKey)) {
            sideKey = key;
            sidePanel.removeAll();
            sidePanel.add(content, BorderLayout.NORTH);
            sidePanel.revalidate();
            sidePanel.repaint();
        }
    }
}
