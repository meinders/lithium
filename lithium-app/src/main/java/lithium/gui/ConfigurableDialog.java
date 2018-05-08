/*
 * Copyright 2013 Gerrit Meinders
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package lithium.gui;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import com.github.meinders.common.*;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.config.*;
import lithium.io.*;

/**
 * A modal dialog that allows the user to set the parameters of a configurable
 * object.
 *
 * @see Configurable
 *
 * @since 0.9
 * @author Gerrit Meinders
 */
public class ConfigurableDialog extends JDialog {
    public static void main(String[] args) {
        Resources.set(new ResourceUtilities("lithium.Resources"));

/*
		ConfigurationSupport configurable = new ConfigurationSupport();
		configurable.addParameter( new Parameter( "a", "String Property", String.class, "This is a default value." ) );
		configurable.addParameter( new Parameter( "b", "String Selection Property", String.class, "Option 2", "Option 1", "Option 2", "Option 3" ) );
		configurable.addParameter( new Parameter( "b", "True Boolean Property", Boolean.class, true ) );
		configurable.addParameter( new Parameter( "b", "False Boolean Property", Boolean.class, false ) );
		configurable.addParameter( new Parameter( "b", "Integer", Integer.class, 14 ) );
		configurable.addParameter( new Parameter( "b", "Double", Double.class, 60.3 ) );
*/

        MapDisplayNameProvider nameProvider = new MapDisplayNameProvider();
        nameProvider.put("timezoneOffset", "Timezone Offset");

        Configurable configurable;
        try {
            configurable = new BeanConfigurationSupport(nameProvider, new Date());
        } catch (IntrospectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }

        JFrame parent = new JFrame();
        ConfigurableDialog dialog = new ConfigurableDialog(parent, configurable);
        if (dialog.showDialog()) {
            System.out.println("dialog closed normally");
        } else {
            System.out.println("dialog cancelled");
        }
        for (Parameter parameter : configurable.getParameters()) {
            System.out.printf("%s = %s\n", parameter.getDisplayName(),
                    configurable.getParameterValue(parameter));
        }
        parent.dispose();
    }

    public static ConfigurableDialog createDialog(Component parent,
            Configurable configurable) {
        Window parentWindow = getAncestorWindow(parent);
        if (parentWindow instanceof Frame) {
            return new ConfigurableDialog((Frame) parentWindow, configurable);
        } else if (parentWindow instanceof Dialog) {
            return new ConfigurableDialog((Dialog) parentWindow, configurable);
        } else {
            throw new AssertionError(
                    "parent must have a Frame or Dialog ancestor");
        }
    }

    private static Window getAncestorWindow(Component component) {
        Component ancestor = component;
        while (ancestor != null && !(ancestor instanceof Window)) {
            ancestor = ancestor.getParent();
        }
        return (Window) ancestor;
    }

    /** The object being configured. */
    private Configurable configurable;

    /** A working copy of the object being configured. */
    private ConfigurationSupport workingCopy;

    /** Whether the dialog is cancelled. */
    private boolean cancelled;

    /** The action for applying and closing the dialog. */
    private Action okAction;

    /** The action for cancelling the dialog. */
    private Action cancelAction;

    /**
     * Constructs a new dialog for configuring the given configurable object.
     *
     * @param parent the dialog's parent window
     * @param configurable the object to be configured
     */
    protected ConfigurableDialog(Frame parent, Configurable configurable) {
        super(parent, "Configure something", true);
        this.configurable = configurable;
        workingCopy = new ConfigurationSupport(configurable);
        configureDialog();
    }

    /**
     * Constructs a new dialog for configuring the given configurable object.
     *
     * @param parent the dialog's parent window
     * @param configurable the object to be configured
     */
    protected ConfigurableDialog(Dialog parent, Configurable configurable) {
        super(parent, "Configure something", true);
        this.configurable = configurable;
        workingCopy = new ConfigurationSupport(configurable);
        configureDialog();
    }

    /**
     * Shows the configuration dialog.
     *
     * @return whether the dialog was closed using the OK button
     */
    public boolean showDialog() {
        cancelled = true;
        setVisible(true);
        return !cancelled;
    }

    /**
     * Configures the dialog's settings and creates the components that make up
     * the dialog's user interface.
     */
    private void configureDialog() {
        createActions();
        setContentPane(createContentPane());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();

        Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
                getGraphicsConfiguration());

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        screen.width -= screenInsets.left + screenInsets.right;
        screen.height -= screenInsets.top + screenInsets.bottom;
        setLocation(screenInsets.left + (screen.width - getWidth()) / 2,
                screenInsets.top + (screen.height - getHeight()) / 2);
    }

    private void createActions() {
        okAction = new AbstractAction("OK") {
            public void actionPerformed(ActionEvent e) {
                cancelled = false;
                workingCopy.applyTo(configurable);
                dispose();
            }
        };

        cancelAction = new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(createFieldsPanel(), BorderLayout.CENTER);
        panel.add(createButtons(), BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
        return panel;
    }

    private JPanel createFieldsPanel() {
        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        Set<Parameter> parameters = workingCopy.getParameters();
        SpringLayout layout = new SpringLayout();
        panel.setLayout(layout);
        for (Parameter parameter : parameters) {
            JComponent editor = createParameterEditor(parameter);
            JLabel label = new JLabel(Resources.get().getString("label",
                    parameter.getDisplayName()));
            label.setLabelFor(editor);
            panel.add(label);
            panel.add(editor);
        }
        panel.add(Box.createRigidArea(new Dimension(0, 0)));
        SpringUtilities
                .makeCompactGrid(panel, parameters.size(), 2, 0, 0, 5, 5);
        return panel;
    }

    private JComponent createParameterEditor(Parameter parameter) {
        if (parameter.getType() == String.class) {
            return createStringParameterEditor(parameter);
        } else if (parameter.getType() == Boolean.class) {
            return createBooleanParameterEditor(parameter);
        } else if (Number.class.isAssignableFrom(parameter.getType())) {
            return createNumberParameterEditor(parameter);
        } else if (parameter.getType() == int.class
                || parameter.getType() == short.class
                || parameter.getType() == long.class
                || parameter.getType() == float.class
                || parameter.getType() == double.class) {
            return createNumberParameterEditor(parameter);
        } else {
            return new JLabel("Unsupported property type: "
                    + parameter.getType());
        }
    }

    private JComponent createStringParameterEditor(final Parameter parameter) {
        String[] values = (String[]) parameter.getValues();
        JComponent component;

        if (values == null) {
            String initialValue = (String) workingCopy
                    .getParameterValue(parameter);
            final JTextField field = new JTextField(initialValue);
            field.getDocument().addDocumentListener(new DocumentListener() {
                public void insertUpdate(DocumentEvent e) {
                    workingCopy.setParameterValue(parameter, field.getText());
                }

                public void removeUpdate(DocumentEvent e) {
                    workingCopy.setParameterValue(parameter, field.getText());
                }

                public void changedUpdate(DocumentEvent e) {
                    workingCopy.setParameterValue(parameter, field.getText());
                }
            });
            component = field;

        } else {
            final JComboBox combo = new JComboBox(values);
            combo.setSelectedItem(workingCopy.getParameterValue(parameter));
            combo.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    workingCopy.setParameterValue(parameter, combo
                            .getSelectedItem());
                }
            });
            component = combo;
        }

        return component;
    }

    private JComponent createBooleanParameterEditor(final Parameter parameter) {
        boolean initial = (Boolean) workingCopy.getParameterValue(parameter);
        final JCheckBox check = new JCheckBox();
        check.setAction(new AbstractAction(parameter.getDisplayName()) {
            public void actionPerformed(ActionEvent e) {
                workingCopy.setParameterValue(parameter, check.isSelected());
            }
        });
        check.setSelected(initial);
        check.setAlignmentX(LEFT_ALIGNMENT);
        return check;
    }

    private JComponent createNumberParameterEditor(final Parameter parameter) {
        Number initialValue = (Number) workingCopy.getParameterValue(parameter);
        JFormattedTextField field = new JFormattedTextField(initialValue);
        field.addPropertyChangeListener("value", new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                workingCopy.setParameterValue(parameter, e.getNewValue());
            }
        });
        field.setHorizontalAlignment(JTextField.TRAILING);
        field.setAlignmentX(LEFT_ALIGNMENT);
        return field;
    }

    private JPanel createButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createHorizontalGlue());
        panel.add(Box.createRigidArea(new Dimension(5, 5)));
        panel.add(new JButton(okAction));
        panel.add(Box.createRigidArea(new Dimension(5, 5)));
        panel.add(new JButton(cancelAction));
        panel.add(Box.createRigidArea(new Dimension(5, 5)));
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }
}
