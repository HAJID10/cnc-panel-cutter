package ca.ulaval.glo2004.gui.panels;

import java.awt.geom.Point2D;

import javax.swing.JTextField;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.gui.Units;

/**
 * L-shaped cut given by its start point and its corner. The horizontal arm runs from the corner back to the
 * start's x, the vertical arm from the corner back to the start's y, so the L can point in any direction.
 */
public class LCutPanel extends CutEditorPanel {
    private final JTextField startX = new JTextField();
    private final JTextField startY = new JTextField();
    private final JTextField cornerX = new JTextField();
    private final JTextField cornerY = new JTextField();

    public LCutPanel(CncController controller, Runnable onChange) {
        super("L-shaped cut", controller, onChange);
        addRow(Units.label("Start x"), startX);
        addRow(Units.label("Start y"), startY);
        addRow(Units.label("Corner x"), cornerX);
        addRow(Units.label("Corner y"), cornerY);
        setHint("Or click the start, then the corner, on the panel.");
    }

    @Override
    protected void clearFields() {
        startX.setText("");
        startY.setText("");
        cornerX.setText("");
        cornerY.setText("");
    }

    @Override
    protected void fillFields(CutDto cut) {
        startX.setText(Units.format(cut.origin().x));
        startY.setText(Units.format(cut.origin().y));
        cornerX.setText(Units.format(cut.destination().x));
        cornerY.setText(Units.format(cut.destination().y));
    }

    private Point2D.Double start() {
        return new Point2D.Double(readMm(startX), readMm(startY));
    }

    private Point2D.Double corner() {
        return new Point2D.Double(readMm(cornerX), readMm(cornerY));
    }

    @Override
    protected void create() {
        controller.addLCut(start(), corner(), selectedToolId(), selectedDepth());
    }

    @Override
    protected void update(CutDto cut) {
        controller.editLCut(cut.id(), start(), corner(), selectedToolId(), selectedDepth());
    }
}
