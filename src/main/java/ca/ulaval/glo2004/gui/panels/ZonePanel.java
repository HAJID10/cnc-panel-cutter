package ca.ulaval.glo2004.gui.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.ZoneDto;
import ca.ulaval.glo2004.gui.Units;

/** Creates a forbidden zone from exact values, or edits / deletes the selected one. */
public class ZonePanel extends JPanel {
    private final CncController controller;
    private final Runnable onChange;
    private final JTextField x = new JTextField();
    private final JTextField y = new JTextField();
    private final JTextField width = new JTextField();
    private final JTextField height = new JTextField();
    private final JButton action = new JButton("Add");
    private final JButton delete = new JButton("Delete zone");
    private ZoneDto editing; // null while creating

    public ZonePanel(CncController controller, Runnable onChange) {
        super(new BorderLayout(6, 6));
        this.controller = controller;
        this.onChange = onChange;
        setBorder(BorderFactory.createTitledBorder("Forbidden zone"));

        JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
        form.add(new JLabel(Units.label("Left")));
        form.add(x);
        form.add(new JLabel(Units.label("Top")));
        form.add(y);
        form.add(new JLabel(Units.label("Width")));
        form.add(width);
        form.add(new JLabel(Units.label("Height")));
        form.add(height);

        JPanel buttons = new JPanel(new GridLayout(0, 1, 6, 6));
        buttons.add(action);
        buttons.add(delete);
        buttons.add(new JLabel("<html><i>The tool may never touch this area (clamps, screws…). "
                + "Or click two opposite corners on the panel.</i></html>"));

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.add(form, BorderLayout.NORTH);
        content.add(buttons, BorderLayout.CENTER);
        add(content, BorderLayout.NORTH);

        action.addActionListener(e -> run(this::apply));
        delete.addActionListener(e -> run(() -> controller.deleteZone(editing.id())));
    }

    public void showCreate() {
        editing = null;
        action.setText("Add");
        delete.setVisible(false);
        for (JTextField f : new JTextField[]{x, y, width, height}) {
            f.setText("");
        }
    }

    public void showEdit(ZoneDto zone) {
        editing = zone;
        action.setText("Apply");
        delete.setVisible(true);
        x.setText(Units.format(zone.origin().x));
        y.setText(Units.format(zone.origin().y));
        width.setText(Units.format(zone.width()));
        height.setText(Units.format(zone.height()));
    }

    private void apply() {
        Point2D.Double topLeft = new Point2D.Double(Units.parse(x.getText()), Units.parse(y.getText()));
        double w = Units.parse(width.getText());
        double h = Units.parse(height.getText());
        if (editing == null) {
            controller.addZone(topLeft, new Point2D.Double(topLeft.x + w, topLeft.y + h));
        } else {
            controller.editZone(editing.id(), topLeft, w, h);
        }
    }

    private void run(Runnable change) {
        try {
            change.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Invalid value", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Not allowed", JOptionPane.WARNING_MESSAGE);
        }
        onChange.run();
    }
}
