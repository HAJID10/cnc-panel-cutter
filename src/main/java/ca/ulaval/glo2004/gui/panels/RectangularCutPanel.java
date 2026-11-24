package ca.ulaval.glo2004.gui.panels;

import java.awt.Point;

import javax.swing.JTextField;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.CutDto;

/** Rectangular cut given by its top-left corner and its size. */
public class RectangularCutPanel extends CutEditorPanel {
    private final JTextField x = new JTextField();
    private final JTextField y = new JTextField();
    private final JTextField width = new JTextField();
    private final JTextField height = new JTextField();

    public RectangularCutPanel(CncController controller, Runnable onChange) {
        super("Rectangular cut", controller, onChange);
        addRow("Left (mm):", x);
        addRow("Top (mm):", y);
        addRow("Width (mm):", width);
        addRow("Height (mm):", height);
        setHint("Or click two opposite corners on the panel.");
    }

    @Override
    protected void clearFields() {
        x.setText("");
        y.setText("");
        width.setText("");
        height.setText("");
    }

    @Override
    protected void fillFields(CutDto cut) {
        x.setText(String.valueOf(cut.origin().x));
        y.setText(String.valueOf(cut.origin().y));
        width.setText(String.valueOf(cut.width()));
        height.setText(String.valueOf(cut.height()));
    }

    @Override
    protected void create() {
        Point topLeft = new Point(readMm(x), readMm(y));
        Point opposite = new Point(topLeft.x + readMm(width), topLeft.y + readMm(height));
        controller.addRectangularCut(topLeft, opposite, selectedToolId());
    }

    @Override
    protected void update(CutDto cut) {
        controller.editCut(cut.id(), new Point(readMm(x), readMm(y)), readMm(width), readMm(height), selectedToolId());
    }
}
