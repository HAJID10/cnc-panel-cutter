package ca.ulaval.glo2004.gui.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.CutDto;
import ca.ulaval.glo2004.gui.Units;
import ca.ulaval.glo2004.dto.ToolDto;

/**
 * Side panel used to create a cut from exact values, or to edit the selected cut. Subclasses add their own fields
 * and say what "create" and "update" mean; this class handles the tool choice, the button and error messages.
 */
public abstract class CutEditorPanel extends JPanel {
    protected final CncController controller;
    private final Runnable onChange;
    private final JPanel form = new JPanel(new GridLayout(0, 2, 6, 6));
    private final JComboBox<ToolDto> toolBox = new JComboBox<>();
    private final JTextField depth = new JTextField();
    private final JButton actionButton = new JButton("Add");
    private final JLabel hint = new JLabel();
    private CutDto editing; // null while creating

    protected CutEditorPanel(String title, CncController controller, Runnable onChange) {
        super(new BorderLayout(6, 6));
        this.controller = controller;
        this.onChange = onChange;
        setBorder(BorderFactory.createTitledBorder(title));

        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        JPanel toolRow = new JPanel(new GridLayout(2, 2, 6, 6));
        toolRow.add(new JLabel("Tool:"));
        toolRow.add(toolBox);
        toolRow.add(new JLabel("Depth (" + Units.symbol() + "):"));
        toolRow.add(depth);
        depth.setToolTipText("The panel thickness cuts right through; less makes a groove.");
        bottom.add(toolRow, BorderLayout.NORTH);
        bottom.add(actionButton, BorderLayout.CENTER);
        bottom.add(hint, BorderLayout.SOUTH);

        JPanel content = new JPanel(new BorderLayout(6, 6));
        content.add(form, BorderLayout.NORTH);
        content.add(bottom, BorderLayout.CENTER);
        add(content, BorderLayout.NORTH);

        actionButton.addActionListener(e -> onAction());
    }

    /** Adds a labelled field to the form. */
    protected void addRow(String label, JComponent field) {
        form.add(new JLabel(label));
        form.add(field);
    }

    protected void setHint(String text) {
        hint.setText("<html><i>" + text + "</i></html>");
    }

    /** Shows the panel for creating a new cut. */
    public void showCreate() {
        editing = null;
        actionButton.setText("Add");
        reloadTools(controller.getDefaultTool().map(ToolDto::id).orElse(null));
        depth.setText(Units.format(controller.getPanel().thickness()));
        clearFields();
    }

    /** Shows the panel for editing an existing cut. */
    public void showEdit(CutDto cut) {
        editing = cut;
        actionButton.setText("Apply");
        reloadTools(cut.tool().id());
        depth.setText(Units.format(cut.depth()));
        fillFields(cut);
    }

    private void reloadTools(UUID selectedId) {
        toolBox.removeAllItems();
        for (ToolDto tool : controller.getPanel().tools()) {
            toolBox.addItem(tool);
            if (tool.id().equals(selectedId)) {
                toolBox.setSelectedItem(tool);
            }
        }
    }

    /** Depth typed in the form. */
    protected Double selectedDepth() {
        return readMm(depth);
    }

    protected UUID selectedToolId() {
        ToolDto tool = (ToolDto) toolBox.getSelectedItem();
        return tool == null ? null : tool.id();
    }

    private void onAction() {
        try {
            if (editing == null) {
                create();
                clearFields();
            } else {
                update(editing);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers.", "Invalid value", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Not allowed", JOptionPane.WARNING_MESSAGE);
        }
        onChange.run();
    }

    /** Reads a length typed by the user, in millimetres. */
    protected static double readMm(JTextField field) {
        return Units.parse(field.getText());
    }

    protected abstract void clearFields();

    protected abstract void fillFields(CutDto cut);

    protected abstract void create();

    protected abstract void update(CutDto cut);
}
