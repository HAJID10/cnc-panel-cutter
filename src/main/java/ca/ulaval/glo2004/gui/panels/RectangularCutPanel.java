package ca.ulaval.glo2004.gui.panels;

import java.awt.geom.Point2D;

import javax.swing.JTextField;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.gui.Units;

/** Rectangular cut given by its top-left corner and its size. */
public class RectangularCutPanel extends CutEditorPanel {
    private final JTextField x = new JTextField();
    private final JTextField y = new JTextField();
    private final JTextField width = new JTextField();
    private final JTextField height = new JTextField();

    public RectangularCutPanel(CncController controller, Runnable onChange) {
        super("Rectangular cut", controller, onChange);
        addRow(Units.label("Left"), x);
        addRow(Units.label("Top"), y);
        addRow(Units.label("Width"), width);
        addRow(Units.label("Height"), height);
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
        x.setText(Units.format(cut.origin().x));
        y.setText(Units.format(cut.origin().y));
        width.setText(Units.format(cut.width()));
        height.setText(Units.format(cut.height()));
    }

    @Override
    protected void create() {
        Point2D.Double topLeft = new Point2D.Double(readMm(x), readMm(y));
        Point2D.Double opposite = new Point2D.Double(topLeft.x + readMm(width), topLeft.y + readMm(height));
        controller.addRectangularCut(topLeft, opposite, selectedToolId(), selectedDepth());
    }

    @Override
    protected void update(CutDto cut) {
        controller.editCut(cut.id(), new Point2D.Double(readMm(x), readMm(y)), readMm(width), readMm(height), selectedToolId(), selectedDepth());
    }
}
