package ca.ulaval.glo2004.gui.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.util.UUID;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import ca.ulaval.glo2004.controller.CncController;
import ca.ulaval.glo2004.dto.ToolDto;
import ca.ulaval.glo2004.gui.Units;

/** List of tools, with buttons to add, change and remove them. The default tool is used for new cuts. */
public class ToolPanel extends JPanel {
    private final CncController controller;
    private final Runnable onChange;
    private final DefaultListModel<ToolDto> model = new DefaultListModel<>();
    private final JList<ToolDto> list = new JList<>(model);
    private final JButton setDefault = new JButton("Use for new cuts");
    private final JButton changeWidth = new JButton("Change width…");
    private final JButton remove = new JButton("Remove");

    public ToolPanel(CncController controller, Runnable onChange) {
        super(new BorderLayout(6, 6));
        this.controller = controller;
        this.onChange = onChange;

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> l, Object value, int index, boolean selected, boolean focus) {
                ToolDto tool = (ToolDto) value;
                String text = Units.describe(tool) + (tool.isDefault() ? "  — default" : "");
                return super.getListCellRendererComponent(l, text, index, selected, focus);
            }
        });
        list.addListSelectionListener(e -> updateButtons());
        add(new JScrollPane(list), BorderLayout.CENTER);

        JTextField name = new JTextField(10);
        JTextField width = new JTextField(5);
        JButton add = new JButton("Add tool");
        JPanel create = new JPanel(new FlowLayout(FlowLayout.LEFT));
        create.setBorder(BorderFactory.createTitledBorder("New tool"));
        create.add(new JLabel("Name:"));
        create.add(name);
        create.add(new JLabel(Units.label("Width")));
        create.add(width);
        create.add(add);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttons.add(setDefault);
        buttons.add(changeWidth);
        buttons.add(remove);
        // List on the left (it gets the whole height); creation form and buttons stacked on the right.
        JPanel right = new JPanel(new java.awt.GridLayout(2, 1));
        right.add(create);
        right.add(buttons);
        add(right, BorderLayout.EAST);

        add.addActionListener(e -> run(() -> {
            controller.addTool(new ToolDto(UUID.randomUUID(), name.getText(), Units.parse(width.getText()), false));
            name.setText("");
            width.setText("");
        }));
        setDefault.addActionListener(e -> selected(tool -> controller.setDefaultTool(tool.id())));
        changeWidth.addActionListener(e -> selected(tool -> {
            String answer = JOptionPane.showInputDialog(this, "New width for " + tool.name() + " (" + Units.symbol() + "):", Units.format(tool.width()));
            if (answer != null) {
                controller.setToolWidth(tool.id(), Units.parse(answer));
            }
        }));
        remove.addActionListener(e -> selected(tool -> controller.removeTool(tool.id())));

        refresh();
    }

    /** Runs an action on the selected tool. */
    private void selected(java.util.function.Consumer<ToolDto> action) {
        ToolDto tool = list.getSelectedValue();
        if (tool != null) {
            run(() -> action.accept(tool));
        }
    }

    /** Runs a change and shows a message if it is refused. */
    private void run(Runnable action) {
        try {
            action.run();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid width.", "Invalid value", JOptionPane.WARNING_MESSAGE);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Not allowed", JOptionPane.WARNING_MESSAGE);
        }
        onChange.run();
    }

    /** Reloads the list from the controller, keeping the selection. */
    public void refresh() {
        ToolDto selected = list.getSelectedValue();
        model.clear();
        for (ToolDto tool : controller.getPanel().tools()) {
            model.addElement(tool);
            if (selected != null && tool.id().equals(selected.id())) {
                list.setSelectedIndex(model.size() - 1);
            }
        }
        updateButtons();
    }

    private void updateButtons() {
        boolean any = list.getSelectedValue() != null;
        setDefault.setEnabled(any);
        changeWidth.setEnabled(any);
        remove.setEnabled(any);
    }
}
