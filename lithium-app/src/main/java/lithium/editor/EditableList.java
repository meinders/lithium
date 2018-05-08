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

import com.github.meinders.common.*;
import lithium.*;

public class EditableList extends JPanel {
    private Action addAction;

    private Action editAction;

    private Action removeAction;

    private ActionListener addActionImpl;

    private ActionListener editActionImpl;

    private ActionListener removeActionImpl;

    public EditableList(JComponent component, boolean showEditButton) {
        super(new BorderLayout());

        if (component == null) {
            throw new NullPointerException("component");
        }

        JScrollPane scrollPane = new JScrollPane(component,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        JPanel buttonPanel = new JPanel(null);
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

        ResourceUtilities res = Resources.get();

        addAction = new AbstractAction(res.getString("add")) {
            public void actionPerformed(ActionEvent e) {
                if (addActionImpl != null) {
                    addActionImpl.actionPerformed(e);
                }
            }
        };
        addAction.putValue(Action.MNEMONIC_KEY, res.getMnemonic("edit"));

        if (showEditButton) {
            editAction = new AbstractAction(res.getString("edit")) {
                public void actionPerformed(ActionEvent e) {
                    if (editActionImpl != null) {
                        editActionImpl.actionPerformed(e);
                    }
                }
            };
            editAction.putValue(Action.MNEMONIC_KEY, res.getMnemonic("edit"));
        } else {
            editAction = null;
        }

        removeAction = new AbstractAction(res.getString("remove")) {
            public void actionPerformed(ActionEvent e) {
                if (removeActionImpl != null) {
                    removeActionImpl.actionPerformed(e);
                }
            }
        };
        removeAction.putValue(Action.MNEMONIC_KEY, res.getMnemonic("remove"));

        final ListSelectionModel selectionModel;
        final boolean selected;
        if (component instanceof JTable) {
            JTable table = (JTable) component;
            selectionModel = table.getSelectionModel();
            selected = selectionModel.getMinSelectionIndex() != -1;
        } else if (component instanceof JList) {
            JList list = (JList) component;
            selectionModel = list.getSelectionModel();
            selected = selectionModel.getMinSelectionIndex() != -1;
        } else {
            selectionModel = null;
            selected = false;
        }

        if (editAction != null) {
            editAction.setEnabled(selected);
        }
        removeAction.setEnabled(selected);

        if (selectionModel != null) {
            ListSelectionListener listener = new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    if (!e.getValueIsAdjusting()) {
                        boolean selected = (selectionModel
                                .getMinSelectionIndex() != -1);
                        if (editAction != null) {
                            editAction.setEnabled(selected);
                        }
                        removeAction.setEnabled(selected);
                    }
                }
            };
            selectionModel.addListSelectionListener(listener);
        }

        Dimension buttonSpacing = new Dimension(5, 5);

        buttonPanel.add(Box.createGlue());
        buttonPanel.add(new JButton(addAction));

        if (showEditButton) {
            buttonPanel.add(Box.createRigidArea(buttonSpacing));
            buttonPanel.add(new JButton(editAction));
        }

        buttonPanel.add(Box.createRigidArea(buttonSpacing));
        buttonPanel.add(new JButton(removeAction));
        buttonPanel.add(Box.createGlue());

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.PAGE_END);
    }

    public EditableList(JComponent component) {
        this(component, true);
    }

    public void setAddAction(ActionListener addAction) {
        this.addActionImpl = addAction;
    }

    public void setEditAction(ActionListener editActionImpl) {
        this.editActionImpl = editActionImpl;
    }

    public void setRemoveAction(ActionListener removeActionImpl) {
        this.removeActionImpl = removeActionImpl;
    }
}
