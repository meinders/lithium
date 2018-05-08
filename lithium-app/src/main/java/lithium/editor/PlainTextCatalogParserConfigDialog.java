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

package lithium.editor;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import lithium.*;
import lithium.io.catalog.*;

/**
 * A configuration dialog for the plain-text catalog parser.
 *
 * @version 0.9 (2006.02.07)
 * @author Gerrit Meinders
 */
public class PlainTextCatalogParserConfigDialog extends JDialog {
    public static boolean showConfigurationDialog(JFrame parent,
            PlainTextCatalogParser parser) {
        PlainTextCatalogParserConfigDialog dialog =
                new PlainTextCatalogParserConfigDialog(parent, parser);
        boolean cancelled = dialog.isCancelled();
        dialog.dispose();
        return cancelled;
    }

    private PlainTextCatalogParser parser;

    private Action okAction;
    private Action cancelAction;

    private JCheckBox renumberCheckbox;
    private JLabel renumberStartAtLabel;
    private JTextField renumberStartAtField;
    private JCheckBox sequentiallyCheckbox;

    private boolean cancelled = true;

    public PlainTextCatalogParserConfigDialog(JFrame parent,
            PlainTextCatalogParser parser) {
        super(parent, Resources.get().getString(
                "plainTextCatalogParser.configDialog"), true);
        this.parser = parser;
        init();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    private void init() {
        setDefaultCloseOperation(HIDE_ON_CLOSE);
        createActions();
        setContentPane(createContentPane());

        pack();
        Rectangle parent = getParent().getBounds();
        setLocation(parent.x + (parent.width - getWidth()) / 2,
                parent.y + (parent.height - getHeight()) / 2);
        setVisible(true);
    }

    private void createActions() {
        okAction = new AbstractAction(Resources.get().getString("ok")) {
            public void actionPerformed(ActionEvent e) {
                // set renumberStartAt
                if (renumberCheckbox.isSelected()) {
                    try {
                        int startAt = Integer.parseInt(
                                renumberStartAtField.getText());
                        parser.setRenumberStartAt(true, startAt);
                    } catch (NumberFormatException ex) {
                        showWarning(ex);
                        return;
                    }

                    // set renumberSequentially
                    parser.setRenumberSequentially(
                            sequentiallyCheckbox.isSelected());
                } else {
                    parser.setRenumberStartAt(false);
                }

                cancelled = false;
                setVisible(false);
            }};

        cancelAction = new AbstractAction(Resources.get().getString("cancel")) {
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
                setVisible(false);
            }};
    }

    private void showWarning(Exception e) {
        JOptionPane.showMessageDialog(this, e, Resources.get().getString(
                "plainTextCatalogParser.warning"),
                JOptionPane.WARNING_MESSAGE);
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(createSettingsPanel());
        panel.add(Box.createVerticalStrut(5));
        panel.add(createButtonPanel());
        return panel;
    }

    private JPanel createSettingsPanel() {
        renumberCheckbox = new JCheckBox(Resources.get().getString(
                "plainTextCatalogParser.renumber"));
        renumberCheckbox.setSelected(parser.getRenumberStartAt() != null);
        renumberCheckbox.setAlignmentX(LEFT_ALIGNMENT);
        renumberCheckbox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                renumberStartAtLabel.setEnabled(renumberCheckbox.isSelected());
                renumberStartAtField.setEnabled(renumberCheckbox.isSelected());
                sequentiallyCheckbox.setEnabled(renumberCheckbox.isSelected());
            }});

        sequentiallyCheckbox = new JCheckBox(Resources.get().getString(
                "plainTextCatalogParser.sequentially"));
        sequentiallyCheckbox.setSelected(parser.isRenumberSequentially());
        sequentiallyCheckbox.setAlignmentX(LEFT_ALIGNMENT);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(renumberCheckbox);
        panel.add(createRenumberStartAtPanel());
        panel.add(sequentiallyCheckbox);
        panel.setAlignmentX(RIGHT_ALIGNMENT);
        return panel;
    }

    private JPanel createRenumberStartAtPanel() {
        renumberStartAtField = new JTextField(5);
        Integer renumberStartAt = parser.getRenumberStartAt();
        if (renumberStartAt == null) {
            renumberStartAtField.setText("1");
        } else {
            renumberStartAtField.setText("" + renumberStartAt);
        }
        renumberStartAtField.setEnabled(renumberStartAt != null);

        renumberStartAtLabel = new JLabel(Resources.get().getString(
                "plainTextCatalogParser.label", Resources.get().getString(
                "plainTextCatalogParser.renumberFrom")));
        renumberStartAtLabel.setLabelFor(renumberStartAtField);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(renumberStartAtLabel);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(renumberStartAtField);
        panel.add(Box.createHorizontalGlue());
        panel.setAlignmentX(LEFT_ALIGNMENT);
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.add(Box.createHorizontalGlue());
        panel.add(new JButton(okAction));
        panel.add(Box.createHorizontalStrut(5));
        panel.add(new JButton(cancelAction));
        panel.setAlignmentX(RIGHT_ALIGNMENT);
        return panel;
    }
}


