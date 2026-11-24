package ca.ulaval.glo2004.gui;

import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

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

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.PanelDto;

/** File, Edit and View menus. */
public class AppMenuBar extends JMenuBar {
    private static final String EXTENSION = "json";

    private final MainWindow window;
    private final CncController controller;

    public AppMenuBar(MainWindow window, CncController controller) {
        this.window = window;
        this.controller = controller;

        JMenu file = new JMenu("File");
        file.add(item("New panel…", KeyEvent.VK_N, this::newPanel));
        file.add(item("Open…", KeyEvent.VK_O, this::open));
        file.add(item("Save…", KeyEvent.VK_S, this::save));
        add(file);

        JMenu edit = new JMenu("Edit");
        edit.add(item("Undo", KeyEvent.VK_Z, () -> {
            controller.undo();
            window.refresh();
        }));
        edit.add(item("Redo", KeyEvent.VK_Y, () -> {
            controller.redo();
            window.refresh();
        }));
        add(edit);

        // Display unit: the model and the files stay in millimetres.
        JMenu view = new JMenu("View");
        javax.swing.ButtonGroup units = new javax.swing.ButtonGroup();
        for (Units.Unit unit : Units.Unit.values()) {
            javax.swing.JRadioButtonMenuItem choice = new javax.swing.JRadioButtonMenuItem(
                    unit == Units.Unit.MM ? "Millimetres" : "Inches", Units.current() == unit);
            choice.addActionListener(e -> {
                Units.setCurrent(unit);
                window.unitsChanged();
            });
            units.add(choice);
            view.add(choice);
        }
        add(view);
    }

    /** Menu item with a Ctrl+key shortcut. */
    private static JMenuItem item(String text, int key, Runnable action) {
        JMenuItem item = new JMenuItem(text);
        item.setAccelerator(KeyStroke.getKeyStroke(key, InputEvent.CTRL_DOWN_MASK));
        item.addActionListener(e -> action.run());
        return item;
    }

    private JFileChooser chooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("CNC Panel Cutter projects (*." + EXTENSION + ")", EXTENSION));
        return chooser;
    }

    private void save() {
        JFileChooser chooser = chooser();
        if (chooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String path = chooser.getSelectedFile().getPath();
        if (!path.toLowerCase().endsWith("." + EXTENSION)) {
            path += "." + EXTENSION;
        }
        if (new File(path).exists() && JOptionPane.showConfirmDialog(window, "Replace the existing file?", "Save",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            controller.save(path);
        } catch (IOException e) {
            error("Could not save the project: " + e.getMessage());
        }
    }

    private void open() {
        JFileChooser chooser = chooser();
        if (chooser.showOpenDialog(window) != JFileChooser.APPROVE_OPTION) {
            return;
        }
        try {
            controller.load(chooser.getSelectedFile().getPath());
            window.panelReplaced();
        } catch (IOException | IllegalArgumentException e) {
            error("Could not open the project: " + e.getMessage());
        }
    }

    /** Asks for the dimensions of a new panel. The current tools are kept. */
    private void newPanel() {
        PanelDto current = controller.getPanel(); // the current panel gives the starting values
        JTextField length = new JTextField(Units.format(current.length()));
        JTextField width = new JTextField(Units.format(current.width()));
        JTextField thickness = new JTextField(Units.format(current.thickness()));

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        form.add(new JLabel("Length (top to bottom):"));
        form.add(length);
        form.add(new JLabel("Width (left to right):"));
        form.add(width);
        form.add(new JLabel("Thickness:"));
        form.add(thickness);

        if (JOptionPane.showConfirmDialog(window, form, "New panel (" + Units.symbol() + ")", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION) {
            return;
        }
        try {
            controller.newPanel(new PanelDto(UUID.randomUUID(), Units.parse(length.getText()),
                    Units.parse(width.getText()), Units.parse(thickness.getText()), new ArrayList<>(), current.tools()));
            window.panelReplaced();
        } catch (NumberFormatException e) {
            error("Please enter valid numbers.");
        } catch (IllegalArgumentException e) {
            error(e.getMessage());
        }
    }

    private void error(String message) {
        JOptionPane.showMessageDialog(window, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
