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
import java.io.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.*;
import javax.swing.text.rtf.*;

import com.github.meinders.common.*;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.catalog.*;
import lithium.gui.*;
import lithium.io.*;
import org.apache.poi.hwpf.extractor.*;

import static javax.swing.Action.*;

/**
 * A dialog allowing the user to select a lyric.
 *
 * @author Gerrit Meinders
 */
public class SelectLyricDialog extends JInternalFrameEx
{
	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	private final ResourceUtilities resources = Resources.get("selectLyricDialog");

	private Playlist playlist;

	private Action addAction;

	private Action closeAction;

	private Action addWordDocumentAction;

	private Action addPlainTextAction;

	private Action addEmptyDocumentAction;

	private JTabbedPane tabbedPane;

	private JComboBox bundleCombo;

	private JTextField numberField;

	private BibleRefEditor quoteEditor;

	/**
	 * Constructs a new lyric selection dialog.
	 *
	 * @param parent the dialog's parent frame
	 * @param playlist the playlist to add selected items to
	 */
	public SelectLyricDialog(JInternalFrameEx parent, Playlist playlist)
	{
		super(parent, null);
		this.playlist = playlist;
		init();
		addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameActivated(InternalFrameEvent e)
			{
				numberField.requestFocus();
				removeInternalFrameListener(this);
			}
		});
	}

	private void init()
	{
		createActions();
		setContentPane(createContentPane());
		setPreferredSize(new Dimension(400, 250));
		setTitle(resources.getString("dialogTitle"));
		pack();
		show();
	}

	private void createActions()
	{
		addAction = new AbstractAction(resources.getString("add"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (tabbedPane.getSelectedIndex() == 0)
				{
					addLyric();
				}
				else if (tabbedPane.getSelectedIndex() == 1)
				{
					addQuote();
				}
				else if (tabbedPane.getSelectedIndex() == 2)
				{
					addWord();
				}
			}
		};
		addAction.putValue(MNEMONIC_KEY, resources.getMnemonic("add"));
		addAction.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_ENTER, 0));

		closeAction = new AbstractAction(resources.getString("close"))
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};
		closeAction.putValue(MNEMONIC_KEY, resources.getMnemonic("close"));
		closeAction.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_ESCAPE, 0));

		addWordDocumentAction = new AbstractAction(
		        resources.getString("wordDocument"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addWord();
				dispose();
			}
		};

		addPlainTextAction = new AbstractAction(
		        resources.getString("plainText"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addPlainText();
				dispose();
			}
		};

		addEmptyDocumentAction = new AbstractAction(
		        resources.getString("newDocument"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addDocument(resources.getString("newDocumentContent"));
				dispose();
			}
		};
	}

	private void addLyric()
	{
		String bundle = (String) bundleCombo.getSelectedItem();

		String numberText = numberField.getText();
		if (numberText.length() == 0)
		{
			// ignore if number field is empty; no warning needed
			return;
		}

		int number = 0;
		try
		{
			number = Integer.parseInt(numberText);
		}
		catch (NumberFormatException ex)
		{
			// show message
			JOptionPane.showInternalMessageDialog(SelectLyricDialog.this,
			        resources.getString("invalidNumber"),
			        SelectLyricDialog.this.getTitle(),
			        JOptionPane.WARNING_MESSAGE);
			numberField.requestFocus();
			return;
		}

		addLyric(bundle, number);

		numberField.setText("");
		numberField.requestFocus();
	}

	private void addLyric(String bundle, int number)
	{
		Catalog catalog = CatalogManager.getCatalog();
		LyricRef lyricRef = new LyricRef(bundle, number);
		Lyric lyric = catalog.getLyric(lyricRef);
		if (lyric == null)
		{
			// show message
			JOptionPane.showInternalMessageDialog(SelectLyricDialog.this,
			        resources.getString("lyricNotFound", bundle, number),
			        SelectLyricDialog.this.getTitle(),
			        JOptionPane.WARNING_MESSAGE);
			numberField.requestFocus();

		}
		else
		{
			playlist.add(new PlaylistItem(lyricRef));
		}
	}

	private void addQuote()
	{
		BibleRef ref = quoteEditor.getValue();
		if (ref != null)
		{
			playlist.add(new PlaylistItem(ref));
		}
	}

	private void addWord()
	{
		JFileChooser chooser = FileChoosers.createFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(new ExtensionFileFilter(
		        resources.getString("wordDocument"), "doc"));

		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			try
			{
				final BufferedInputStream in = new BufferedInputStream(
				        new FileInputStream(file));

				// typical MS Office document signature:
				// (byte)0xd0 , (byte)0xcf, (byte)0x11, (byte)0xe0

				final CharSequence text;
				if (isRTFFile(in))
				{
					final RTFEditorKit editorKit = new RTFEditorKit();
					final Document document = editorKit.createDefaultDocument();
					try
					{
						editorKit.read(in, document, 0);
						text = document.getText(0, document.getLength());
					}
					catch (BadLocationException e)
					{
						throw new IOException(e);
					}

				}
				else
				{
					StringBuilder buffer = new StringBuilder();
					WordExtractor extractor = new WordExtractor(in);
					String[] paragraphs = extractor.getParagraphText();
					for (String paragraph : paragraphs)
					{
						buffer.append(paragraph.trim());
						buffer.append('\n');
					}
					text = buffer;
				}

				addDocument(text);

			}
			catch (Exception e)
			{
				String title = resources.getString("ioException.title");
				String message = resources.getString("ioException.message",
				        e.getLocalizedMessage());
				JOptionPane.showInternalMessageDialog(this, message, title,
				        JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void addPlainText()
	{
		JFileChooser chooser = FileChoosers.createFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		chooser.addChoosableFileFilter(new ExtensionFileFilter(
		        resources.getString("plainText"), "txt"));

		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			try
			{
				addDocument(TextInput.readPlainText(file));

			}
			catch (Exception e)
			{
				String title = resources.getString("ioException.title");
				String message = resources.getString("ioException.message",
				        e.getLocalizedMessage());
				JOptionPane.showInternalMessageDialog(this, message, title,
				        JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	private void addDocument(CharSequence text)
	{
		playlist.add(new PlaylistItem(text));
	}

	/**
	 * Returns whether the given stream contains an RTF file starting at its
	 * current position. The given input stream must support mark/reset.
	 *
	 * @param in the input stream to read from
	 * @return whether the given stream contains an RTF file.
	 * @throws IOException if the stream doesn't support mark/reset.
	 */
	private boolean isRTFFile(final InputStream in) throws IOException
	{
		if (!in.markSupported())
		{
			throw new IOException("mark not supported");
		}
		final String signature = "{\\rtf1";
		in.mark(signature.length());

		boolean result = true;
		for (int i = 0; i < signature.length(); i++)
		{
			if (signature.charAt(i) != in.read())
			{
				result = false;
				break;
			}
		}

		in.reset();
		return result;
	}

	private JPanel createContentPane()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		tabbedPane = createSelectionForms();
		panel.add(tabbedPane);
		panel.add(createButtonPanel(), BorderLayout.SOUTH);

		return panel;
	}

	private JTabbedPane createSelectionForms()
	{
		JTabbedPane tabbedPane = new JTabbedPane();
		tabbedPane.add(resources.getString("selectByNumber"),
		        createLyricSelectionForm());
		tabbedPane.add(resources.getString("bibleRef"),
		        createQuoteSelectionForm());
		tabbedPane.add(resources.getString("text"), createWordSelectionForm());
		return tabbedPane;
	}

	private JPanel createLyricSelectionForm()
	{
		final ResourceUtilities resources = Resources.get("selectLyricDialog");

		// create combo with bundle names
		JLabel bundleLabel = new JLabel(resources.getString("label",
		        resources.getString("bundle")));
		Catalog catalog = CatalogManager.getCatalog();
		TreeSet<String> bundleNames = new TreeSet<String>();
		for (Group bundle : catalog.getBundles())
		{
			bundleNames.add(bundle.getName());
		}
		bundleCombo = new JComboBox(bundleNames.toArray());
		String defaultBundle = ConfigManager.getConfig().getDefaultBundle();
		if (defaultBundle != null)
		{
			bundleCombo.setSelectedItem(defaultBundle);
		}

		// focus number field after selecting from bundle combo
		bundleCombo.addPopupMenuListener(new PopupMenuListener()
		{
			public void popupMenuCanceled(PopupMenuEvent e)
			{
				// ignored
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{
				// ignored
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
				numberField.requestFocus();
			}
		});

		// number field
		JLabel numberLabel = new JLabel(resources.getString("label",
		        resources.getString("number")));
		numberField = new JTextField(5);

		JPanel numberFieldLeft = new JPanel();
		numberFieldLeft.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 0));
		numberFieldLeft.add(numberField);

		// selection form panel
		JPanel form = new JPanel();
		form.setLayout(new SpringLayout());
		form.setAlignmentX(Component.RIGHT_ALIGNMENT);
		form.add(bundleLabel);
		form.add(bundleCombo);
		form.add(numberLabel);
		form.add(numberFieldLeft);
		SpringUtilities.makeCompactGrid(form, 2, 2, 0, 0, 5, 5);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(form, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		return panel;
	}

	private JPanel createQuoteSelectionForm()
	{
		quoteEditor = new BibleRefEditor();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setAlignmentX(RIGHT_ALIGNMENT);
		panel.add(quoteEditor, BorderLayout.NORTH);
		return panel;
	}

	private JPanel createWordSelectionForm()
	{
		JPanel buttons = new JPanel();
		buttons.setAlignmentX(Component.LEFT_ALIGNMENT);
		buttons.add(new JButton(addWordDocumentAction));
		buttons.add(new JButton(addPlainTextAction));
		buttons.add(new JButton(addEmptyDocumentAction));

		// selection form panel
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(new MultiLineLabel(resources.getString("text.about"), 2));
		panel.add(buttons);
		panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		return panel;
	}

	private JPanel createButtonPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new FlowLayout(FlowLayout.TRAILING, 0, 0));
		panel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));

		// escape closes dialog
		InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = getActionMap();
		Object closeKey = closeAction.getValue(Action.NAME);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), closeKey);
		actionMap.put(closeKey, closeAction);

		JButton addButton = new JButton(addAction);
		panel.add(addButton);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(new JButton(closeAction));

		setDefaultButton(addButton);

		return panel;
	}
}
