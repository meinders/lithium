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

public class EditTextDialog extends JDialog
{
	private PlaylistItem item;

	private boolean readOnly;

	private Action okAction;

	private Action cancelAction;

	private JTextArea textArea;

	public EditTextDialog(Window parent, PlaylistItem item)
	{
		this(parent, item, false);
	}

	public EditTextDialog(Window parent, PlaylistItem item, boolean readOnly)
	{
		this(Resources.get().getString("textEditor.title"), parent, item,
		        readOnly);
	}

	protected EditTextDialog(String title, Window parent, PlaylistItem item,
	        boolean readOnly)
	{
		super(parent, title, ModalityType.MODELESS);
		this.item = item;
		this.readOnly = readOnly;

		createActions();
		setContentPane(createContentPane());
		pack();
	}

	public PlaylistItem getItem()
	{
		return item;
	}

	protected void performOkAction(String text)
	{
		item.setValue(text);

		/*
		 * Update display. FIXME This is a hack ofcourse; use
		 * PlaylistItemChanged events or something!
		 */
		Playlist playlist = PlaylistManager.getPlaylist();
		if (playlist != null)
		{
			PlaylistSelectionModel selectionModel = playlist.getSelectionModel();
			if (selectionModel.getSelectedItem() == item)
			{
				selectionModel.setSelectedValue("");
				selectionModel.setSelectedItem(item);
			}
		}
	}

	private void createActions()
	{
		okAction = new AbstractAction(Resources.get().getString("ok"))
		{
			public void actionPerformed(ActionEvent e)
			{
				String text = textArea.getText();
				performOkAction(text);
				dispose();
			}
		};

		String cancelTitle;
		if (readOnly)
		{
			cancelTitle = Resources.get().getString("close");
		}
		else
		{
			cancelTitle = Resources.get().getString("cancel");
		}
		cancelAction = new AbstractAction(cancelTitle)
		{
			{
				putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				        KeyEvent.VK_ESCAPE, 0));
			}

			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};
	}

	private JPanel createContentPane()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		panel.add(createTextPanel());
		panel.add(createButtonPanel());

		return panel;
	}

	private JPanel createTextPanel()
	{
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(Resources.get().getString(
		        "editLyricDialog.textBorderTitle")));

		textArea = new JTextArea(10, 40);
		if (item != null)
		{
			textArea.setText(item.getValue().toString());
			textArea.setCaretPosition(0);
		}
		textArea.setEditable(!readOnly);
		JScrollPane textAreaScroller = new JScrollPane(textArea,
		        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(textAreaScroller, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createButtonPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));

		if (!readOnly)
		{
			JButton okButton = new JButton(okAction);
			panel.add(okButton);
			panel.add(Box.createHorizontalStrut(5));
			getRootPane().setDefaultButton(okButton);
		}
		panel.add(new JButton(cancelAction));

		return panel;
	}
}
