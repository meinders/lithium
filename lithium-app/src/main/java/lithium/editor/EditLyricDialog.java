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

import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.catalog.*;

/**
 * A dialog allowing the user to edit a lyric.
 *
 * @version 0.9 (2006.03.10)
 * @author Gerrit Meinders
 */
public class EditLyricDialog extends JDialog
{
	private Lyric lyric;
	private boolean readOnly;

	private Action okAction;
	private Action cancelAction;

	private JTextField numberField;
	private JTextField titleField;
	private JCheckBox originalTitleCheck;
	private JTextField originalTitleField;
	private JTextArea textArea;
	private JTextArea copyrightsArea;

	public EditLyricDialog(Window parent, Lyric lyric)
	{
		this(parent, lyric, false);
	}

	public EditLyricDialog(Window parent, Lyric lyric, boolean readOnly)
	{
		this(Resources.get().getString("editLyricDialog.title"), parent, lyric,
		        readOnly);
	}

	protected EditLyricDialog(String title, Window parent, Lyric lyric,
	        boolean readOnly)
	{
		super(parent, title, ModalityType.MODELESS);
		this.lyric = lyric;
		this.readOnly = readOnly;

		createActions();
		setContentPane(createContentPane());
		pack();
	}

	public Lyric getLyric()
	{
		return lyric;
	}

	protected boolean validateNumber(int number)
	{
		return number > 0;
	}

	protected boolean validateTitle(String title)
	{
		return title != null && title.length() > 0;
	}

	protected void performOkAction(int number, String title, String text,
	        String originalTitle, String copyrights)
	{
		if (lyric == null)
		{
			lyric = new DefaultLyric(number, title);
		}
		else
		{
			lyric.setTitle(title);
		}
		lyric.setOriginalTitle(originalTitle);
		lyric.setText(text);
		lyric.setCopyrights(copyrights);
	}

	private void createActions()
	{
		okAction = new AbstractAction(Resources.get().getString("ok"))
		{
			public void actionPerformed(ActionEvent e)
			{
				int number;
				try
				{
					number = Integer.parseInt(numberField.getText());
				}
				catch (NumberFormatException ex)
				{
					JOptionPane.showInternalMessageDialog(EditLyricDialog.this,
					        Resources.get().getString(
					                "editLyricDialog.invalidNumber"),
					        getTitle(), JOptionPane.WARNING_MESSAGE);
					numberField.requestFocus();
					return;
				}

				if (!validateNumber(number))
				{
					JOptionPane.showInternalMessageDialog(EditLyricDialog.this,
					        Resources.get().getString(
					                "editLyricDialog.existingNumber"),
					        getTitle(), JOptionPane.WARNING_MESSAGE);
					numberField.requestFocus();
					return;
				}

				String title = titleField.getText();
				if (!validateTitle(title))
				{
					JOptionPane.showInternalMessageDialog(
					        EditLyricDialog.this,
					        Resources.get().getString("editLyricDialog.noTitle"),
					        getTitle(), JOptionPane.WARNING_MESSAGE);
					titleField.requestFocus();
					return;
				}

				String originalTitle = originalTitleField.getText();
				if (originalTitle.length() == 0)
					originalTitle = null;

				String text = textArea.getText();
				String copyrights = copyrightsArea.getText();

				performOkAction(number, title, text, originalTitle, copyrights);
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

		panel.add(createForm());
		panel.add(createTextPanel());
		panel.add(createCopyrightsPanel());
		panel.add(createButtonPanel());

		return panel;
	}

	private JPanel createForm()
	{
		JPanel panel = new JPanel(new SpringLayout());
		panel.setAlignmentX(Component.RIGHT_ALIGNMENT);

		numberField = new JTextField();
		titleField = new JTextField();
		originalTitleField = new JTextField();

		if (lyric != null)
		{
			numberField.setText(String.valueOf(lyric.getNumber()));
			numberField.setEditable(false);
			titleField.setText(lyric.getTitle());
			String originalTitle = lyric.getOriginalTitle();
			if (originalTitle == null)
			{
				originalTitle = "";
			}
			originalTitleField.setText(originalTitle);
		}
		if (readOnly)
		{
			numberField.setEditable(false);
			titleField.setEditable(false);
			originalTitleField.setEditable(false);
		}

		panel.add(new JLabel(Resources.get().getString(
		        "editLyricDialog.numberLabel")));
		panel.add(numberField);
		panel.add(new JLabel(Resources.get().getString(
		        "editLyricDialog.titleLabel")));
		panel.add(titleField);
		panel.add(new JLabel(Resources.get().getString(
		        "editLyricDialog.originalTitleLabel")));
		panel.add(originalTitleField);

		SpringUtilities.makeCompactGrid(panel, 3, 2, 0, 0, 5, 5);

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
		if (lyric != null)
		{
			textArea.setText(lyric.getText());
			textArea.setCaretPosition(0);
		}
		textArea.setEditable(!readOnly);
		JScrollPane textAreaScroller = new JScrollPane(textArea,
		        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(textAreaScroller, BorderLayout.CENTER);

		return panel;
	}

	private JPanel createCopyrightsPanel()
	{
		JPanel panel = new JPanel();
		panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createTitledBorder(Resources.get().getString(
		        "editLyricDialog.copyrightsBorderTitle")));

		copyrightsArea = new JTextArea(3, 40);
		if (lyric != null)
		{
			copyrightsArea.setText(lyric.getCopyrights());
			copyrightsArea.setCaretPosition(0);
		}
		copyrightsArea.setEditable(!readOnly);
		JScrollPane copyrightsAreaScroller = new JScrollPane(copyrightsArea,
		        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
		        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		panel.add(copyrightsAreaScroller, BorderLayout.CENTER);

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
