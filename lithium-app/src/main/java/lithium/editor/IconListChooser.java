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

import lithium.*;
import lithium.imagebrowser.*;

/**
 * Allows the user to make a selection from an icon list.
 *
 * @author Gerrit Meinders
 * @param <T> value type of the icon list chooser
 */
public class IconListChooser<T> {
	private final String title;

	private IconListModel<T> listModel;

	private Dimension preferredSize;

	public IconListChooser(String title) {
		this.title = title;
		listModel = new IconListModel<T>();
		preferredSize = new Dimension(300, 200);
	}

	public Dimension getPreferredSize() {
		return preferredSize;
	}

	public void setPreferredSize(Dimension preferredSize) {
		this.preferredSize = preferredSize;
	}

	public T showSelectionDialog(Window parent) {
		IconListDialog dialog = new IconListDialog(parent);
		dialog.setVisible(true);
		return dialog.isCancelled() ? null : dialog.getSelectedValue();
	}

	public void add(T option, ImageIcon icon) {
		add(option, icon, icon.getDescription());
	}

	public void add(T option, Icon icon, String description) {
		listModel.add(option, icon, description);
	}

	public void remove(T option) {
		listModel.remove(option);
	}

	public class IconListDialog extends JDialog {
		private JIconList<T> list;

		private Action okAction;

		private Action cancelAction;

		private boolean cancelled = true;

		public IconListDialog(Window parent) {
			super(parent, title,
			        ModalityType.APPLICATION_MODAL);
			init();
		}

		public boolean isCancelled() {
			return cancelled;
		}

		public T getSelectedValue() {
			return list.getSelectedValue();
		}

		private void init() {
			createActions();
			setContentPane(createContentPane());
			pack();
			Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
			setLocation((screen.width - getWidth()) / 2,
			        (screen.height - getHeight()) / 2);
		}

		private void createActions() {
			okAction = new AbstractAction(Resources.get().getString("ok")) {
				public void actionPerformed(ActionEvent e) {
					cancelled = false;
					dispose();
				}
			};
			okAction.setEnabled(false);

			cancelAction = new AbstractAction(Resources.get().getString(
			        "cancel")) {
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
			list = new JIconList<T>(listModel);
			list.setPreferredSize(preferredSize);

			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					okAction.setEnabled(true);
				}
			});
			list.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					if (e.getClickCount() > 1) {
						if (okAction.isEnabled()) {
							okAction.actionPerformed(null);
						}
					}
				}
			});

			JScrollPane scroller = new JScrollPane(list,
			        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
			        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
}
