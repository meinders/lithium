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
import java.util.*;
import javax.swing.*;

import lithium.*;
import lithium.catalog.*;

/**
 * A dialog allowing the user to edit a bundle.
 *
 * @version 0.9 (2006.02.15)
 * @author Gerrit Meinders
 */
public class EditBundleDialog extends JInternalFrameEx {
    private Group bundle;

    private Action okAction;
    private Action cancelAction;

    private JComboBox nameCombo;
    private JTextField versionField;

    public EditBundleDialog(JInternalFrameEx parent, Group bundle) {
        this(Resources.get().getString("editBundleDialog.title"), parent, bundle);
    }

    protected EditBundleDialog(String title, JInternalFrameEx parent,
            Group bundle) {
        super(parent, title);
        this.bundle = bundle;
        init();
    }

    public Group getBundle() {
        return bundle;
    }

    protected boolean validateName(String name) {
        return name != null && name.length() > 0;
    }

    protected boolean validateVersion(String version) {
        return version != null && version.length() > 0;
    }

    protected void performOkAction(String name, String version) {
        bundle.setName(name);
        bundle.setVersion(version);
    }

    private void init() {
        createActions();
        setContentPane(createContentPane());
        pack();
        show();
    }

    private void createActions() {
        okAction = new AbstractAction(Resources.get().getString("ok")) {
            public void actionPerformed(ActionEvent e) {
                String name = (String) nameCombo.getSelectedItem();
                if (!validateName(name)) {
                    JOptionPane.showInternalMessageDialog(
                            EditBundleDialog.this,
                            Resources.get().getString("editBundleDialog.noName"),
                            getTitle(),
                            JOptionPane.WARNING_MESSAGE);
                    nameCombo.requestFocus();
                    return;
                }

                String version = versionField.getText();
                if (!validateVersion(version)) {
                    JOptionPane.showInternalMessageDialog(
                            EditBundleDialog.this,
                            Resources.get().getString("editBundleDialog.noVersion"),
                            getTitle(),
                            JOptionPane.WARNING_MESSAGE);
                    versionField.requestFocus();
                    return;
                }

                performOkAction(name, version);
                dispose();
            }};

        cancelAction = new AbstractAction(Resources.get().getString("cancel")) {
            {
                putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
            }
            public void actionPerformed(ActionEvent e) {
                dispose();
            }};
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        panel.add(createForm());
        panel.add(createButtonPanel());

        return panel;
    }

    private JPanel createForm() {
        // get bundle names
        TreeSet<String> bundleNames = new TreeSet<String>();
        boolean existingBundleName = false;
        for (Group existingBundle : CatalogManager.getCatalog().getBundles()) {
            bundleNames.add(existingBundle.getName());
            if (existingBundle.getName().equals(bundle.getName())) {
                existingBundleName = true;
            }
        }

        // name combo
        JLabel nameLabel = new JLabel(Resources.get().getString(
                "editBundleDialog.nameLabel"));
        nameCombo = new JComboBox(bundleNames.toArray(new String[0]));
        if (!existingBundleName) {
            nameCombo.addItem(bundle.getName());
        }
        nameCombo.setSelectedItem(bundle.getName());
        nameCombo.setEditable(true);

        // version field
        JLabel versionLabel = new JLabel(Resources.get().getString(
                "editBundleDialog.versionLabel"));
        versionField = new JTextField(10);
        versionField.setText(bundle.getVersion());

        // arrange labels and fields using panels
        JPanel namePanel = new JPanel();
        namePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.LINE_AXIS));
        namePanel.add(nameLabel);
        namePanel.add(Box.createHorizontalStrut(10));
        namePanel.add(nameCombo);

        JPanel versionPanel = new JPanel();
        versionPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        versionPanel.setLayout(new BoxLayout(versionPanel, BoxLayout.LINE_AXIS));
        versionPanel.add(versionLabel);
        versionPanel.add(Box.createHorizontalStrut(10));
        versionPanel.add(versionField);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.add(namePanel);
        panel.add(versionPanel);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
        panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
        panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));

        JButton okButton = new JButton(okAction);
        panel.add(okButton);
        panel.add(Box.createHorizontalStrut(5));
        panel.add(new JButton(cancelAction));
        setDefaultButton(okButton);

        return panel;
    }
}
