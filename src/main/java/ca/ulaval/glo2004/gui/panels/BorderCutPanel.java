package ca.ulaval.glo2004.gui.panels;

import javax.swing.JTextField;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.CutDto;

/** Border cut: the final size of the panel, always centred. */
public class BorderCutPanel extends CutEditorPanel {
    private final JTextField width = new JTextField();
    private final JTextField length = new JTextField();

    public BorderCutPanel(CncController controller, Runnable onChange) {
        super("Border cut", controller, onChange);
        addRow("Final width (mm):", width);
        addRow("Final length (mm):", length);
        setHint("The border is always centred on the panel.");
    }

    @Override
    protected void clearFields() {
        width.setText("");
        length.setText("");
    }

    @Override
    protected void fillFields(CutDto cut) {
        width.setText(String.valueOf(cut.width()));
        length.setText(String.valueOf(cut.height()));
    }

    @Override
    protected void create() {
        controller.addBorderCut(readMm(width), readMm(length), selectedToolId());
    }

    @Override
    protected void update(CutDto cut) {
        controller.editCut(cut.id(), null, readMm(width), readMm(length), selectedToolId());
    }
}
