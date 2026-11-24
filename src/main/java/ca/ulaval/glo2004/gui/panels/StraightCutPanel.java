package ca.ulaval.glo2004.gui.panels;

import java.awt.geom.Point2D;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.domain.CutType;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.gui.Units;
import ca.ulaval.glo2004.dto.PanelDto;

/**
 * Vertical or horizontal cut placed at a distance from a panel edge or from another cut of the same direction.
 * One class serves both directions: only the axis and the labels change.
 */
public class StraightCutPanel extends CutEditorPanel {
    private final CutType type;
    private final JComboBox<String> reference;
    private final JComboBox<CutChoice> otherCut = new JComboBox<>();
    private final JComboBox<String> side;
    private final JTextField distance = new JTextField();

    /** A cut shown in the "existing cut" list. */
    private record CutChoice(CutDto cut, String label) {
        @Override
        public String toString() {
            return label;
        }
    }

    public StraightCutPanel(CutType type, CncController controller, Runnable onChange) {
        super(type == CutType.VERTICAL ? "Vertical cut" : "Horizontal cut", controller, onChange);
        if (type != CutType.VERTICAL && type != CutType.HORIZONTAL) {
            throw new IllegalArgumentException("Only straight cuts.");
        }
        this.type = type;
        boolean vertical = type == CutType.VERTICAL;
        reference = new JComboBox<>(vertical
                ? new String[]{"Left edge", "Right edge", "Existing cut"}
                : new String[]{"Top edge", "Bottom edge", "Existing cut"});
        side = new JComboBox<>(vertical ? new String[]{"Left of it", "Right of it"} : new String[]{"Above it", "Below it"});

        addRow("Measured from:", reference);
        addRow("Existing cut:", otherCut);
        addRow("Side:", side);
        addRow("Distance (mm):", distance);
        setHint("Or click on the panel to place the cut.");

        reference.addActionListener(e -> updateEnabled());
        updateEnabled();
    }

    private boolean vertical() {
        return type == CutType.VERTICAL;
    }

    private void updateEnabled() {
        boolean fromCut = reference.getSelectedIndex() == 2;
        otherCut.setEnabled(fromCut);
        side.setEnabled(fromCut);
    }

    /** Lists the other cuts of the same direction. */
    private void reloadCuts(CutDto exclude) {
        otherCut.removeAllItems();
        List<CutDto> cuts = controller.getPanel().cuts();
        for (CutDto cut : cuts) {
            if (cut.type() == type && (exclude == null || !cut.id().equals(exclude.id()))) {
                String label = (vertical() ? "x = " : "y = ") + Units.withUnit(vertical() ? cut.origin().x : cut.origin().y);
                otherCut.addItem(new CutChoice(cut, label));
            }
        }
    }

    /** Position (x for vertical, y for horizontal) of the cut from the form. */
    private double position() {
        double d = readMm(distance);
        PanelDto panel = controller.getPanel();
        return switch (reference.getSelectedIndex()) {
            case 0 -> d;
            case 1 -> (vertical() ? panel.width() : panel.length()) - d;
            default -> {
                CutChoice choice = (CutChoice) otherCut.getSelectedItem();
                if (choice == null) {
                    throw new IllegalArgumentException("Choose an existing cut first.");
                }
                double base = vertical() ? choice.cut().origin().x : choice.cut().origin().y;
                yield side.getSelectedIndex() == 0 ? base - d : base + d;
            }
        };
    }

    @Override
    protected void clearFields() {
        reloadCuts(null);
        reference.setSelectedIndex(0);
        distance.setText("");
    }

    @Override
    protected void fillFields(CutDto cut) {
        reloadCuts(cut);
        reference.setSelectedIndex(0);
        distance.setText(Units.format(vertical() ? cut.origin().x : cut.origin().y));
    }

    @Override
    protected void create() {
        if (vertical()) {
            controller.addVerticalCut(position(), selectedToolId());
        } else {
            controller.addHorizontalCut(position(), selectedToolId());
        }
    }

    @Override
    protected void update(CutDto cut) {
        double p = position();
        Point2D.Double newOrigin = vertical() ? new Point2D.Double(p, cut.origin().y) : new Point2D.Double(cut.origin().x, p);
        controller.editCut(cut.id(), newOrigin, null, null, selectedToolId());
    }
}
