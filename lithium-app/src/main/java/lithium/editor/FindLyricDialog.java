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
import java.beans.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.catalog.*;
import lithium.search.*;

/**
 * A dialog allowing the user to search for lyrics.
 *
 * @author Gerrit Meinders
 */
public class FindLyricDialog extends JInternalFrameEx
{
	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	private static final int FIND_CONTENT_TAB = 0;

	private static final int FIND_BIBLE_REF_TAB = 1;

	private Playlist playlist;

	private Action findAction;

	private Action findBibleRefAction;

	private Action findContentAction;

	private Action closeAction;

	private Action searchResultAction;

	private JTabbedPane searchMethodTabs;

	private JTextField queryField;

	private JCheckBox titleCheckbox;

	private JCheckBox textCheckbox;

	private JCheckBox originalTitleCheckbox;

	private JCheckBox copyrightsCheckbox;

	private JRadioButton oneRadioButton;

	private JRadioButton allRadioButton;

	private JRadioButton exactRadioButton;

	private SearchResultDialog resultDialog;

	private BibleRefEditor bibleRefEditor;

	/**
	 * Constructs a new lyric finding dialog.
	 *
	 * @param parent the parent frame of the dialog
	 * @param playlist the playlist to add found items to
	 */
	public FindLyricDialog(JInternalFrameEx parent, Playlist playlist)
	{
		super(parent, Resources.get().getString("findLyricDialog.dialogTitle"));
		this.playlist = playlist;
		init();
	}

	private void init()
	{
		setResizable(true);
		setResizable(false);
		setClosable(true);
		setMaximizable(false);
		setIconifiable(false);

		createActions();
		setContentPane(createContentPane());
		pack();
		setVisible(true);
	}

	private void createActions()
	{
		findAction = new AbstractAction(Resources.get().getString(
		        "findLyricDialog.find"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				try
				{
					switch (searchMethodTabs.getSelectedIndex())
					{
					case FIND_CONTENT_TAB:
						findContentAction.actionPerformed(e);
						break;
					case FIND_BIBLE_REF_TAB:
						findBibleRefAction.actionPerformed(e);
						break;
					default:
						assert false : "Invalid tab index: "
						        + searchMethodTabs.getSelectedIndex();
					}
				}
				finally
				{
					setCursor(Cursor.getDefaultCursor());
				}
			}
		};

		findBibleRefAction = new AbstractAction(Resources.get().getString(
		        "findLyricDialog.find"))
		{
			public void actionPerformed(ActionEvent e)
			{
				findBibleRef();
			}
		};

		findContentAction = new AbstractAction(Resources.get().getString(
		        "findLyricDialog.find"))
		{
			public void actionPerformed(ActionEvent e)
			{
				findContent();
			}
		};

		if (playlist == null)
		{
			searchResultAction = new AbstractAction(Resources.get().getString(
			        "findLyricDialog.open"))
			{
				public void actionPerformed(ActionEvent e)
				{
					Collection<SearchResult> searchResults = resultDialog.getSelectedResults();
					if (searchResults.isEmpty())
					{
						// show "empty selection" message
						JOptionPane.showInternalMessageDialog(resultDialog,
						        Resources.get().getString(
						                "findLyricDialog.emptySelection"),
						        resultDialog.getTitle(),
						        JOptionPane.WARNING_MESSAGE);
					}
					else
					{
						EditorFrame editor = EditorFrame.getInstance();
						for (SearchResult searchResult : searchResults)
						{
							Lyric lyric = CatalogManager.getCatalog().getLyric(
							        searchResult.getLyricRef());
							EditLyricDialog dialog = new EditLyricDialog(
							        editor, lyric);
							dialog.setVisible(true);
						}
					}
				}
			};
		}
		else
		{
			searchResultAction = new AbstractAction(Resources.get().getString(
			        "findLyricDialog.add"))
			{
				public void actionPerformed(ActionEvent e)
				{
					Collection<SearchResult> searchResults = resultDialog.getSelectedResults();
					if (searchResults.isEmpty())
					{
						// show "empty selection" message
						JOptionPane.showInternalMessageDialog(resultDialog,
						        Resources.get().getString(
						                "findLyricDialog.emptySelection"),
						        resultDialog.getTitle(),
						        JOptionPane.WARNING_MESSAGE);
					}
					else
					{
						for (SearchResult searchResult : searchResults)
						{
							playlist.add(new PlaylistItem(
							        searchResult.getLyricRef()));
						}
					}
				}
			};
		}

		closeAction = new AbstractAction(Resources.get().getString(
		        "findLyricDialog.close"))
		{
			public void actionPerformed(ActionEvent e)
			{
				dispose();
			}
		};
	}

	private void findContent()
	{
		String words = queryField.getText().trim();
		if (words.length() == 0)
		{
			// show message
			JOptionPane.showInternalMessageDialog(this,
			        Resources.get().getString("findLyricDialog.emptyQuery"),
			        getTitle(), JOptionPane.WARNING_MESSAGE);
			queryField.requestFocus();
			return;
		}

		boolean searchTitle = titleCheckbox.isSelected();
		boolean searchText = textCheckbox.isSelected();
		boolean searchOriginalTitle = originalTitleCheckbox.isSelected();
		boolean searchCopyrights = copyrightsCheckbox.isSelected();

		ContentSearchQuery.Method method;
		if (oneRadioButton.isSelected())
		{
			method = ContentSearchQuery.Method.ANY_WORD;
		}
		else if (allRadioButton.isSelected())
		{
			method = ContentSearchQuery.Method.ALL_WORDS;
		}
		else if (exactRadioButton.isSelected())
		{
			method = ContentSearchQuery.Method.EXACT_PHRASE;
		}
		else
		{
			assert false;
			return;
		}

		SearchQuery query = new AdvancedContentSearchQuery(words, method,
		        searchTitle, searchText, searchOriginalTitle, searchCopyrights);

		ProgressDialog<Collection<SearchResult>> progressDialog;
		if (getTopLevelAncestor() instanceof JFrame)
		{
			JFrame parent = (JFrame) getTopLevelAncestor();
			String title = Resources.get().getString("ellipsis",
			        Resources.get().getString("FindLyricDialog.searching"));
			progressDialog = new ProgressDialog<Collection<SearchResult>>(
			        parent, title);
			progressDialog.setVisible(true);
		}
		else
		{
			assert false : getTopLevelAncestor() + " not instanceof JFrame";
			return;
		}
		LyricFinder finder = new LyricFinder(query)
		{
			@Override
			public void finished()
			{
				showResultDialog(get());
			}
		};
		progressDialog.setWorker(finder);
		finder.start();
	}

	private void findBibleRef()
	{
		BibleRef bibleRef = bibleRefEditor.getValue();
		SearchQuery query = new BibleSearchQuery(bibleRef);

		ProgressDialog<Collection<SearchResult>> progressDialog;
		if (getTopLevelAncestor() instanceof JFrame)
		{
			progressDialog = new ProgressDialog<Collection<SearchResult>>(
			        (JFrame) getTopLevelAncestor(), Resources.get().getString(
			                "ellipsis",
			                Resources.get().getString(
			                        "FindLyricDialog.searching")));
		}
		else
		{
			assert false : getTopLevelAncestor() + " not instanceof JFrame";
			return;
		}
		LyricFinder finder = new LyricFinder(query)
		{
			@Override
			public void finished()
			{
				showResultDialog(get());
			}
		};
		progressDialog.setWorker(finder);
		finder.start();
	}

	private void showResultDialog(Collection<SearchResult> results)
	{
		assert SwingUtilities.isEventDispatchThread() : "Method must be called from EventDispatchThread";

		resultDialog = new SearchResultDialog(this, results, searchResultAction);
		JDesktopPaneEx desktop = (JDesktopPaneEx) getDesktopPane();
		desktop.add(resultDialog, JDesktopPaneEx.CENTER);
		try
		{
			resultDialog.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			// accept veto
		}
	}

	private JPanel createContentPane()
	{
		searchMethodTabs = new JTabbedPane();
		searchMethodTabs.addTab(Resources.get().getString(
		        "findLyricDialog.contentSearch"), createFindContentPanel());
		searchMethodTabs.addTab(Resources.get().getString(
		        "findLyricDialog.bibleSearch"), createFindBibleRefPanel());
		searchMethodTabs.setAlignmentX(RIGHT_ALIGNMENT);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(searchMethodTabs);
		panel.add(createButtonPanel());

		return panel;
	}

	private JPanel createFindBibleRefPanel()
	{
		bibleRefEditor = new BibleRefEditor();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setAlignmentX(RIGHT_ALIGNMENT);
		panel.add(bibleRefEditor, BorderLayout.NORTH);
		return panel;
	}

	private JPanel createFindContentPanel()
	{
		queryField = new JTextField();
		addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameActivated(InternalFrameEvent e)
			{
				queryField.requestFocus();
			}
		});

		JPanel queryPanel = new JPanel();
		queryPanel.setLayout(new BoxLayout(queryPanel, BoxLayout.LINE_AXIS));
		queryPanel.setAlignmentX(LEFT_ALIGNMENT);
		queryPanel.add(new JLabel(Resources.get().getString(
		        "findLyricDialog.label",
		        Resources.get().getString("findLyricDialog.searchTerms"))));
		queryPanel.add(Box.createHorizontalStrut(10));
		queryPanel.add(queryField);

		JPanel contentTypeLabelPanel = new JPanel();
		contentTypeLabelPanel.setLayout(new BoxLayout(contentTypeLabelPanel,
		        BoxLayout.LINE_AXIS));
		contentTypeLabelPanel.setAlignmentX(LEFT_ALIGNMENT);
		contentTypeLabelPanel.add(new JLabel(Resources.get().getString(
		        "findLyricDialog.label",
		        Resources.get().getString("findLyricDialog.lookAt"))));

		titleCheckbox = new JCheckBox(Resources.get().getString(
		        "findLyricDialog.title"), true);
		textCheckbox = new JCheckBox(Resources.get().getString(
		        "findLyricDialog.lyrics"), true);
		originalTitleCheckbox = new JCheckBox(Resources.get().getString(
		        "findLyricDialog.originalTitle"));
		copyrightsCheckbox = new JCheckBox(Resources.get().getString(
		        "findLyricDialog.copyrights"));

		JPanel contentTypePanel = new JPanel();
		contentTypePanel.setLayout(new BoxLayout(contentTypePanel,
		        BoxLayout.LINE_AXIS));
		contentTypePanel.setAlignmentX(LEFT_ALIGNMENT);
		contentTypePanel.add(titleCheckbox);
		contentTypePanel.add(Box.createHorizontalStrut(11));
		contentTypePanel.add(textCheckbox);
		contentTypePanel.add(Box.createHorizontalStrut(11));
		contentTypePanel.add(originalTitleCheckbox);
		contentTypePanel.add(Box.createHorizontalStrut(11));
		contentTypePanel.add(copyrightsCheckbox);

		JLabel methodLabel = new JLabel(Resources.get().getString(
		        "findLyricDialog.label",
		        Resources.get().getString("findLyricDialog.searchMethod")));
		methodLabel.setAlignmentY(TOP_ALIGNMENT);

		oneRadioButton = new JRadioButton(Resources.get().getString(
		        "findLyricDialog.atLeastOneWord"));
		allRadioButton = new JRadioButton(Resources.get().getString(
		        "findLyricDialog.allWords"), true);
		exactRadioButton = new JRadioButton(Resources.get().getString(
		        "findLyricDialog.exactPhrase"));

		ButtonGroup methodGroup = new ButtonGroup();
		methodGroup.add(oneRadioButton);
		methodGroup.add(allRadioButton);
		methodGroup.add(exactRadioButton);

		JPanel methodOptionsPanel = new JPanel();
		methodOptionsPanel.setLayout(new BoxLayout(methodOptionsPanel,
		        BoxLayout.PAGE_AXIS));
		methodOptionsPanel.setAlignmentY(TOP_ALIGNMENT);
		methodOptionsPanel.add(oneRadioButton);
		methodOptionsPanel.add(allRadioButton);
		methodOptionsPanel.add(exactRadioButton);

		JPanel methodPanel = new JPanel();
		methodPanel.setLayout(new BoxLayout(methodPanel, BoxLayout.LINE_AXIS));
		methodPanel.setAlignmentX(LEFT_ALIGNMENT);
		methodPanel.add(methodLabel);
		methodPanel.add(Box.createHorizontalStrut(12));
		methodPanel.add(methodOptionsPanel);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		panel.setAlignmentX(RIGHT_ALIGNMENT);
		panel.add(queryPanel);
		panel.add(Box.createVerticalStrut(11));
		panel.add(contentTypeLabelPanel);
		panel.add(contentTypePanel);
		panel.add(Box.createVerticalStrut(11));
		panel.add(methodPanel);
		return panel;
	}

	private JPanel createButtonPanel()
	{
		final JButton findButton = new JButton(findAction);
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				getRootPane().setDefaultButton(findButton);
			}
		});

		// escape closes dialog
		InputMap inputMap = getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
		ActionMap actionMap = getActionMap();
		Object closeKey = closeAction.getValue(Action.NAME);
		inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), closeKey);
		actionMap.put(closeKey, closeAction);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 5));
		panel.setAlignmentX(RIGHT_ALIGNMENT);
		panel.add(findButton);
		panel.add(Box.createHorizontalStrut(5));
		panel.add(new JButton(closeAction));
		return panel;
	}
}
