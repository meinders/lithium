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
import javax.swing.event.*;

import lithium.Announcement.*;
import lithium.*;
import lithium.imagebrowser.*;

/**
 * A dialog allowing the user to select a certain type of announcement preset
 * parameter.
 *
 * @version 0.9 (2006.08.26)
 * @author Gerrit Meinders
 */
public class ParameterTypeDialog extends JDialog {
    private JIconList<Class<?>> list;

    private Action okAction;

    private Action cancelAction;

    private Class<?> selectedValue;

    private boolean cancelled = true;

    public ParameterTypeDialog(Window parent) {
        super(parent, Resources.get().getString(
                "announcements.selectParameterType"),
                ModalityType.APPLICATION_MODAL);
        init();
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public Class<?> getSelectedValue() {
        return selectedValue;
    }

    private void init() {
        createActions();
        setContentPane(createContentPane());
        pack();
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screen.width - getWidth()) / 2,
                (screen.height - getHeight()) / 2);

        // add icons for each parameter type
        list.add(TextParameter.class, new ImageIcon(getClass().getResource(
                "/images/text.png"), Resources.get().getString(
                "announcements.textParameter")));
        list.add(DateParameter.class, new ImageIcon(getClass().getResource(
                "/images/datetime.png"), Resources.get().getString(
                "announcements.dateParameter")));
    }

    private void createActions() {
        okAction = new AbstractAction(Resources.get().getString("ok")) {
            {
                setEnabled(false);
            }

            public void actionPerformed(ActionEvent e) {
                selectedValue = list.getSelectedValue();
                cancelled = false;
                dispose();
            }
        };

        cancelAction = new AbstractAction(Resources.get().getString("cancel")) {
            public void actionPerformed(ActionEvent e) {
                cancelled = true;
                dispose();
            }
        };
    }

    private JPanel createContentPane() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(createIconList(), BorderLayout.CENTER);
        panel.add(createButtonPanel(), BorderLayout.SOUTH);
        return panel;
    }

    private JComponent createIconList() {
        list = new JIconList<Class<?>>();
        list.setPreferredSize(new Dimension(200, 100));

        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                okAction.setEnabled(true);
            }
        });
        list.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() > 1) {
                    if (okAction.isEnabled()) {
                        okAction.actionPerformed(null);
                    }
                }
            }
        });

        JScrollPane scroller = new JScrollPane(list);
        return scroller;
    }

    private JPanel createButtonPanel() {
        JButton okButton = new JButton(okAction);
        JButton cancelButton = new JButton(cancelAction);
        getRootPane().setDefaultButton(okButton);

        JPanel panel = new JPanel();
        panel.add(okButton);
        panel.add(cancelButton);

        return panel;
    }
}
