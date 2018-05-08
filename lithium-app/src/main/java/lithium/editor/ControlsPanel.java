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
import java.text.*;
import java.util.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.RowSorter.*;
import javax.swing.event.*;
import javax.swing.table.*;

import com.github.meinders.common.*;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.catalog.*;
import lithium.display.*;
import lithium.io.*;
import lithium.search.*;

/**
 * A user interface for controlling the program from full-screen mode, featuring
 * lyric selection and search, the active playlist and several other controls.
 *
 * @since 0.3
 * @author Gerrit Meinders
 */
public class ControlsPanel extends JPanel implements PropertyChangeListener
{
	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	private ViewModel model;

	private Playlist playlist;

	private JComboBox bundleCombo;

	private JTextField numberField;

	private JList playlistList;

	private boolean playlistListUpdating;

	private DefaultListModel playlistListModel;

	private JTable searchResultTable;

	private JCheckBox scrollBarVisibleCheck;

	private JCheckBox dividerVisibleCheck;

	private JPanel playlistTab;

	private JPanel searchResultTab;

	private JPanel configurationTab;

	private JPanel selectTab;

	private JPanel miscellaneousTab;

	private JTabbedPane leftTabbedPane;

	private JTabbedPane rightTabbedPane;

	private Action controlsVisibleAction;

	private Action selectAction;

	private Action editAction;

	private Action selectFirstAction;

	private Action selectPreviousAction;

	private Action selectNextAction;

	private Action selectLastAction;

	private Action backgroundVisibleAction;

	private Action contentVisibleAction;

	private Action minimizeAction;

	private Action exitAction;

	private Action scrollBarVisibleAction;

	private Action dividerVisibleAction;

	/**
	 * Constructs a new controls panel that operates on the given lyric view
	 * model.
	 *
	 * @param model the lyric view model
	 */
	public ControlsPanel(ViewModel model)
	{
		super();
		this.model = model;

		model.addPropertyChangeListener(this);
		ConfigManager configManager = ConfigManager.getInstance();
		configManager.addPropertyChangeListener(this);

		PlaylistManager.getPropertyChangeSupport().addPropertyChangeListener(
		        PlaylistManager.PLAYLIST_PROPERTY, this);

		createActions();
		createKeyBindings();
		createComponents();

		final Playlist playlist = model.getPlaylist();
		if (playlist != null)
		{
			setPlaylist(playlist);
		}
	}

	private void setPlaylist(Playlist playlist)
	{
		if (playlist == null)
		{
			throw new NullPointerException("playlist");
		}

		if (this.playlist != null)
		{
			this.playlist.removePropertyChangeListener(this);
			this.playlist.getSelectionModel().removePropertyChangeListener(this);
		}

		this.playlist = playlist;
		playlist.addPropertyChangeListener(this);
		playlist.getSelectionModel().addPropertyChangeListener(this);

		updatePlaylistListModel();
		updateSelectionControls();
	}

	@Override
	public boolean requestFocusInWindow()
	{
		boolean returnValue = super.requestFocusInWindow();
		leftTabbedPane.setSelectedComponent(selectTab);
		numberField.requestFocusInWindow();
		return returnValue;
	}

	private class Legacy
	{
		private void select(String text)
		{
			if (text.length() > 0)
			{
				BibleRef bibleRef = null;
				try
				{
					bibleRef = BibleRefParser.parse(text);
				}
				catch (Exception ex)
				{
					// bibleRef retains its null value
				}

				if (bibleRef != null)
				{
					selectBibleRef(bibleRef);

				}
				else
				{
					Pattern bundleNumberPattern = Pattern.compile(
					        "([^0-9\\s]+)?\\s*([0-9]+)",
					        Pattern.CASE_INSENSITIVE);
					Matcher bundleNumberMatcher = bundleNumberPattern.matcher(text);

					if (bundleNumberMatcher.find())
					{
						String bundleName;
						if (bundleNumberMatcher.group(1) == null)
						{
							bundleName = (String) bundleCombo.getSelectedItem();
						}
						else
						{
							String bundleIdentifier = bundleNumberMatcher.group(1);
							bundleName = bundleIdentifier;
							for (Group bundle : CatalogManager.getCatalog().getBundles())
							{
								String name = bundle.getName();
								if (name.toLowerCase().startsWith(
								        bundleIdentifier.toLowerCase()))
								{
									bundleName = name;
									break;
								}
							}
						}

						int number = Integer.parseInt(bundleNumberMatcher.group(2));

						selectNumber(bundleName, number);
					}
					else
					{
						search(numberField.getText());
					}
				}
			}
		}

		private void selectNumber(String bundleName, int number)
		{
			try
			{
				LyricRef selection = new LyricRef(bundleName, number);
				Lyric lyric = CatalogManager.getCatalog().getLyric(selection);
				if (lyric == null)
				{
					try
					{
						// show message
						showWarningDialog(Resources.get().getString(
						        "selectLyricDialog.lyricNotFound",
						        selection.getBundle(), selection.getNumber()));
					}
					catch (Exception ex)
					{
						ex.printStackTrace();
					}
					numberField.requestFocusInWindow();
					return;
				}
				else
				{
					playlist.getSelectionModel().setSelectedValue(selection);
					model.setContentVisible(true);
				}
			}
			catch (NumberFormatException ex)
			{
				// ex.printStackTrace();
			}
			catch (IllegalArgumentException ex)
			{
				// ex.printStackTrace();
			}
			numberField.setText("");
			numberField.requestFocusInWindow();
		}

		/**
		 * Performs a search for the currently entered search query and displays
		 * the result in the search results list.
		 */
		private void search(String words)
		{
			SearchQuery query = new AdvancedContentSearchQuery(words,
			        ContentSearchQuery.Method.ALL_WORDS, true, true, true, true);

			ProgressDialog<Collection<SearchResult>> progressDialog;
			if (getTopLevelAncestor() instanceof JFrame)
			{
				JFrame parent = (JFrame) getTopLevelAncestor();
				String title = Resources.get().getString("ellipsis",
				        Resources.get().getString("FindLyricDialog.searching"));
				progressDialog = new ProgressDialog<Collection<SearchResult>>(
				        parent, title);
			}
			else
			{
				throw new AssertionError(getTopLevelAncestor()
				        + " not instanceof JFrame");
			}
			LyricFinder finder = new LyricFinder(query)
			{
				@Override
				public void finished()
				{
					setCursor(Cursor.getDefaultCursor());
					showSearchResults(get());
				}
			};
			progressDialog.setWorker(finder);
			finder.start();
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		}

		private void selectBibleRef(BibleRef ref)
		{
			playlist.getSelectionModel().setSelectedValue(ref);
			model.setContentVisible(true);
			numberField.setText("");
			numberField.requestFocusInWindow();
		}
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getPropertyName() == PlaylistSelectionModel.SELECTED_ITEM_PROPERTY)
		{
			updateSelectionControls();

			EditorFrame editor = EditorFrame.getInstance();
			editor.showControls();

		}
		else if (e.getPropertyName() == Playlist.ITEMS_PROPERTY)
		{
			updatePlaylistListModel();
			updateSelectionControls();

		}
		else if (e.getPropertyName() == PlaylistManager.PLAYLIST_PROPERTY)
		{
			if (PlaylistManager.getPlaylist() != null)
			{
				setPlaylist(PlaylistManager.getPlaylist());
			}
			else
			{
				setPlaylist(new Playlist());
			}

		}
		else if (e.getPropertyName() == ViewModel.CONTROLS_VISIBLE_PROPERTY)
		{
			if (model.isControlsVisible())
			{
				numberField.requestFocusInWindow();
			}

		}
		else if (e.getPropertyName() == ConfigManager.CONFIG_PROPERTY)
		{
			Config config = ConfigManager.getConfig();
			scrollBarVisibleCheck.setSelected(config.isScrollBarVisible());
			dividerVisibleCheck.setSelected(config.isDividerVisible());
		}
	}

	private void setContent(Object content)
	{
		if (content instanceof PlaylistItem)
		{
			PlaylistItem item = (PlaylistItem) content;
			playlist.getSelectionModel().setSelectedItem(item);
		}
		else
		{
			model.setContent(content);
		}

		model.setContentVisible(true);

		if (EditorFrame.isStarted())
		{
			EditorFrame editor = EditorFrame.getInstance();
			editor.showControls();
		}
	}

	private int showConfirmDialog(String message, String title)
	{
		int choice = JOptionPane.showConfirmDialog(this, message, title,
		        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		return choice;
	}

	private void showWarningDialog(String message)
	{
		ApplicationDescriptor application = Application.getInstance().getDescriptor();
		JOptionPane.showMessageDialog(this, message, application.getTitle(),
		        JOptionPane.WARNING_MESSAGE);
	}

	private void createActions()
	{
		Icon firstIcon = new ImageIcon(getClass().getResource(
		        "/toolbarButtonGraphics/media/Rewind16.gif"));
		Icon previousIcon = new ImageIcon(getClass().getResource(
		        "/toolbarButtonGraphics/media/StepBack16.gif"));
		Icon nextIcon = new ImageIcon(getClass().getResource(
		        "/toolbarButtonGraphics/media/StepForward16.gif"));
		Icon lastIcon = new ImageIcon(getClass().getResource(
		        "/toolbarButtonGraphics/media/FastForward16.gif"));

		// show/hide the ControlsPanel
		controlsVisibleAction = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				model.setControlsVisible(!model.isControlsVisible());
			}
		};

		// playlist selection actions
		selectFirstAction = new AbstractAction("", firstIcon)
		{
			public void actionPerformed(ActionEvent e)
			{
				playlist.getSelectionModel().selectFirst();
			}
		};
		selectFirstAction.setEnabled(false);

		selectPreviousAction = new AbstractAction("", previousIcon)
		{
			public void actionPerformed(ActionEvent e)
			{
				playlist.getSelectionModel().selectPrevious();
			}
		};
		selectPreviousAction.setEnabled(false);

		selectNextAction = new AbstractAction("", nextIcon)
		{
			public void actionPerformed(ActionEvent e)
			{
				playlist.getSelectionModel().selectNext();
			}
		};
		selectNextAction.setEnabled(false);

		selectLastAction = new AbstractAction(null, lastIcon)
		{
			public void actionPerformed(ActionEvent e)
			{
				playlist.getSelectionModel().selectLast();
			}
		};
		selectLastAction.setEnabled(false);

		// configuration panel actions
		scrollBarVisibleAction = new AbstractAction(Resources.get().getString(
		        "controlsPanel.scrollBarVisible"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				boolean scrollBarVisible;
				if (source instanceof JCheckBox)
				{
					scrollBarVisible = ((JCheckBox) source).isSelected();
				}
				else
				{
					throw new AssertionError("Unexpected source: " + source);
				}
				Config config = ConfigManager.getConfig();
				config.setScrollBarVisible(scrollBarVisible);
			}
		};

		dividerVisibleAction = new AbstractAction(Resources.get().getString(
		        "controlsPanel.dividerVisible"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Object source = e.getSource();
				boolean dividerVisible;
				if (source instanceof JCheckBox)
				{
					dividerVisible = ((JCheckBox) source).isSelected();
				}
				else
				{
					throw new AssertionError("Unexpected source: " + source);
				}
				Config config = ConfigManager.getConfig();
				config.setDividerVisible(dividerVisible);
			}
		};

		// more actions
		editAction = new AbstractAction(null, new ImageIcon(
		        getClass().getResource(
		                "/toolbarButtonGraphics/general/Edit16.gif")))
		{
			public void actionPerformed(ActionEvent e)
			{
				EditorContext editor = EditorFrame.getInstance();

				Playlist playlist = model.getPlaylist();
				if (playlist == null)
				{
					playlist = new Playlist();
					model.setPlaylist(playlist);
				}

				editor.showPlaylist(playlist);
			}
		};
		editAction.putValue(Action.SHORT_DESCRIPTION,
		        Resources.get().getString("playlist.edit.tip"));

		contentVisibleAction = new AbstractAction(Resources.get().getString(
		        "controlsPanel.contentVisible"))
		{
			public void actionPerformed(ActionEvent e)
			{
				model.setContentVisible(!model.isContentVisible());
			}
		};

		backgroundVisibleAction = new AbstractAction(Resources.get().getString(
		        "controlsPanel.blackScreen"))
		{
			public void actionPerformed(ActionEvent e)
			{
				boolean blackScreen = !(model.isBackgroundVisible() || model.isContentVisible());
				model.setBackgroundVisible(blackScreen);
			}
		};

		minimizeAction = new AbstractAction(Resources.get().getString(
		        "controlsPanel.minimize"))
		{
			public void actionPerformed(ActionEvent e)
			{
				int selection = showConfirmDialog(Resources.get().getString(
				        "controlsPanel.confirmMinimize"),
				        Resources.get().getString("controlsPanel.minimize"));
				if (selection == JOptionPane.YES_OPTION)
				{
					JFrame frame = (JFrame) getTopLevelAncestor();
					frame.setExtendedState(JFrame.ICONIFIED);
				}
			}
		};

		exitAction = new AbstractAction(Resources.get().getString(
		        "controlsPanel.exit"))
		{
			public void actionPerformed(ActionEvent e)
			{
				int selection = showConfirmDialog(Resources.get().getString(
				        "controlsPanel.confirmExit"),
				        Resources.get().getString("controlsPanel.exit"));
				if (selection == JOptionPane.YES_OPTION)
				{
					FullScreenDisplay.getInstance().dispose();
				}
			}
		};
	}

	private void showSearchResults(Collection<SearchResult> results)
	{
		searchResultTable.clearSelection();
		DefaultTableModel model = (DefaultTableModel) searchResultTable.getModel();

		// clear model
		for (int i = model.getRowCount() - 1; i >= 0; i--)
		{
			model.removeRow(i);
		}

		if (results.isEmpty())
		{
			model.addRow(new Object[] {
			        Resources.get().getString("controlsPanel.noResults"), "" });
		}
		else
		{
			SearchResult[] resultsArray = results.toArray(new SearchResult[0]);
			Arrays.sort(resultsArray);
			for (SearchResult result : resultsArray)
			{
				model.addRow(new Object[] { result.getLyricRef(),
				        result.getRelevance() });
			}
		}

		rightTabbedPane.setSelectedComponent(searchResultTab);
		numberField.requestFocusInWindow();
	}

	private void createKeyBindings()
	{
		// 'do nothing'-action to override default mappings
		Action doNothing = new AbstractAction()
		{
			public void actionPerformed(ActionEvent e)
			{
				// do nothing
			}
		};
		getActionMap().put("doNothing", doNothing);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "selectFirst");
		getActionMap().put("selectFirst", selectFirstAction);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), "selectPrevious");
		getActionMap().put("selectPrevious", selectPreviousAction);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), "selectNext");
		getActionMap().put("selectNext", selectNextAction);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "selectLast");
		getActionMap().put("selectLast", selectLastAction);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "textVerbergen");
		getActionMap().put("textVerbergen", contentVisibleAction);

		getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "backgroundVisible");
		getActionMap().put("backgroundVisible", backgroundVisibleAction);

		getInputMap(WHEN_FOCUSED).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "none");
		getInputMap(WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
		        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "none");

		if (!EditorFrame.isStarted())
		{
			getInputMap(WHEN_IN_FOCUSED_WINDOW).put(
			        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			        "controlsVisible");
			getActionMap().put("controlsVisible", controlsVisibleAction);
		}
	}

	private void createComponents()
	{
		playlistTab = createPlaylistPanel();
		searchResultTab = createSearchResultPanel();
		configurationTab = createConfigurationPanel();
		selectTab = createSelectAndSearchPanel();
		miscellaneousTab = createMiscellaneousPanel();

		setLayout(new BorderLayout());

		// get screen size of presentation device
		Config config = ConfigManager.getConfig();
		DisplayConfig displayConfig = config.getDisplayConfig(DisplayConfig.PRESENTATION_MODE);
		Rectangle bounds = displayConfig.getGraphicsConfiguration().getBounds();

		if (bounds.getWidth() >= 640)
		{
			add(createComponentsNormal(), BorderLayout.CENTER);
		}
		else
		{
			add(createComponentsCompact(), BorderLayout.CENTER);
		}
		setPreferredSize(new Dimension(0, 150));
	}

	private JPanel createComponentsNormal()
	{
		leftTabbedPane = new JTabbedPane();
		leftTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.selectTab"), selectTab);
		leftTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.configurationTab"), configurationTab);
		leftTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.miscellaneousTab"), miscellaneousTab);

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.PAGE_AXIS));
		leftPanel.add(leftTabbedPane);

		rightTabbedPane = new JTabbedPane();
		rightTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.playlistTab"), playlistTab);
		rightTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.searchResultsTab"), searchResultTab);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.PAGE_AXIS));
		rightPanel.add(rightTabbedPane);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 2, 5, 5));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(leftPanel);
		panel.add(rightPanel);

		return panel;
	}

	private JPanel createComponentsCompact()
	{
		leftTabbedPane = new JTabbedPane();
		rightTabbedPane = leftTabbedPane;

		leftTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.selectTab"), selectTab);
		leftTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.configurationTab"), configurationTab);
		leftTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.miscellaneousTab"), miscellaneousTab);
		rightTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.playlistTab"), playlistTab);
		rightTabbedPane.addTab(Resources.get().getString(
		        "controlsPanel.searchResultsTab"), searchResultTab);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(leftTabbedPane);

		return panel;
	}

	private JPanel createSelectAndSearchPanel()
	{
		// get bundle names
		TreeSet<String> bundleNames = new TreeSet<String>();
		for (Group bundle : CatalogManager.getCatalog().getBundles())
		{
			bundleNames.add(bundle.getName());
		}

		// create combo with unique bundle names
		bundleCombo = new JComboBox(bundleNames.toArray(new String[0]));
		bundleCombo.setEditable(false);
		Group defaultBundle = CatalogManager.getCatalog().getBundle(
		        ConfigManager.getConfig().getDefaultBundle());

		/*
		 * Automatically performs select number action when the combo's popup is
		 * hidden.
		 */
		bundleCombo.addPopupMenuListener(new PopupMenuListener()
		{
			public void popupMenuCanceled(PopupMenuEvent e)
			{ /* ignored */
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e)
			{ /* ignored */
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e)
			{
				selectAction.actionPerformed(null);
			}
		});

		// select default bundle
		if (defaultBundle != null)
		{
			bundleCombo.setSelectedItem(defaultBundle.getName());
		}

		// create components
		numberField = new JTextField(20);
		numberField.setBorder(BorderFactory.createCompoundBorder(
		        numberField.getBorder(), BorderFactory.createEmptyBorder(5, 5,
		                5, 5)));
		selectAction = new QuickAddAction(numberField)
		{
			@Override
			protected void playlistItemSelected(PlaylistItem item)
			{
				playlist.getSelectionModel().setSelectedItem(item);
			}
		};
		selectAction.putValue(Action.NAME, null);
		numberField.addActionListener(selectAction);

		JLabel numberLabel = new JLabel(Resources.get().getString(
		        "controlsPanel.findLabel"));

		/*
		 * lay-out components
		 */
		JPanel panel = new JPanel();
		panel.add(numberLabel);
		panel.add(numberField);
		return panel;
	}

	private JPanel createMiscellaneousPanel()
	{
		JPanel panel = new JPanel();

		panel.add(new JButton(contentVisibleAction));
		panel.add(new JButton(backgroundVisibleAction));
		panel.add(new JButton(minimizeAction));
		panel.add(new JButton(exitAction));

		return panel;
	}

	private JPanel createPlaylistPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		playlistListModel = new DefaultListModel();
		updatePlaylistListModel();
		playlistList = new JList(playlistListModel);
		playlistList.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{
				if (!playlistListUpdating && !playlistList.isSelectionEmpty())
				{
					setContent(playlistListModel.get(playlistList.getLeadSelectionIndex()));
				}
			}
		});
		playlistList.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					setContent(playlistListModel.get(playlistList.getLeadSelectionIndex()));
				}
			}
		});
		playlistList.setCellRenderer(new PlaylistCellRenderer());

		JScrollPane scrollPane = new JScrollPane(playlistList);
		panel.add(scrollPane);
		panel.add(createPlaylistButtonPanel());

		return panel;
	}

	private JPanel createPlaylistButtonPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JButton(selectFirstAction));
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(new JButton(selectPreviousAction));
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(new JButton(selectNextAction));
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(new JButton(selectLastAction));
		panel.add(Box.createHorizontalGlue());
		panel.add(new JButton(editAction));
		return panel;
	}

	private JPanel createSearchResultPanel()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));

		DefaultTableModel model = new DefaultTableModel()
		{
			@Override
			public Class<?> getColumnClass(int columnIndex)
			{
				switch (columnIndex)
				{
				case 1:
					return Double.class;
				default:
					return Object.class;
				}
			}

			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		model.addColumn("content item"); // not visible
		model.addColumn("relevance"); // not visible

		TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(
		        model);
		sorter.setSortKeys(Arrays.asList(new SortKey[] { new SortKey(1,
		        SortOrder.DESCENDING) }));
		searchResultTable = new JTable(model);
		searchResultTable.setShowHorizontalLines(false);
		searchResultTable.setShowVerticalLines(false);
		searchResultTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		searchResultTable.setColumnSelectionAllowed(false);
		searchResultTable.setRowSorter(sorter);
		searchResultTable.getSelectionModel().addListSelectionListener(
		        new ListSelectionListener()
		        {
			        public void valueChanged(ListSelectionEvent e)
			        {
				        ListSelectionModel selectionModel = (ListSelectionModel) e.getSource();
				        Object selected = searchResultTable.getValueAt(
				                selectionModel.getLeadSelectionIndex(), 0);
				        setContent(selected);
			        }
		        });
		searchResultTable.setDefaultRenderer(Object.class,
		        new PlaylistCellRenderer());

		FormatCellRenderer percentRenderer = new FormatCellRenderer(
		        NumberFormat.getPercentInstance());
		percentRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
		searchResultTable.setDefaultRenderer(Double.class, percentRenderer);
		searchResultTable.setTableHeader(null);

		TableColumnModel columnModel = searchResultTable.getColumnModel();
		columnModel.getColumn(1).setMaxWidth(50);

		JScrollPane scrollPane = new JScrollPane(searchResultTable);
		panel.add(scrollPane);

		return panel;
	}

	private JPanel createConfigurationPanel()
	{
		Config config = ConfigManager.getConfig();

		scrollBarVisibleCheck = new JCheckBox(scrollBarVisibleAction);
		scrollBarVisibleCheck.setSelected(config.isScrollBarVisible());

		dividerVisibleCheck = new JCheckBox(dividerVisibleAction);
		dividerVisibleCheck.setSelected(config.isDividerVisible());

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(scrollBarVisibleCheck);
		panel.add(dividerVisibleCheck);
		return panel;
	}

	private void updateSelectionControls()
	{
		if (playlist == null)
		{
			selectFirstAction.setEnabled(false);
			selectPreviousAction.setEnabled(false);
			selectNextAction.setEnabled(false);
			selectLastAction.setEnabled(false);

		}
		else
		{
			PlaylistSelectionModel model = playlist.getSelectionModel();
			boolean isPreviousSelectable = model.isPreviousSelectable();
			boolean isNextSelectable = model.isNextSelectable();

			selectFirstAction.setEnabled(isPreviousSelectable);
			selectPreviousAction.setEnabled(isPreviousSelectable);
			selectNextAction.setEnabled(isNextSelectable);
			selectLastAction.setEnabled(isNextSelectable);

			contentVisibleAction.setEnabled(model.getSelectedItem() != null);

			playlistListUpdating = true;
			playlistList.setSelectedIndex(model.getSelectedIndex());
			playlistListUpdating = false;

			playlistList.ensureIndexIsVisible(playlist.getSelectionModel().getSelectedIndex());
		}
	}

	private void updatePlaylistListModel()
	{
		playlistListModel.clear();
		if (playlist != null)
		{
			for (PlaylistItem item : playlist.getItems())
			{
				playlistListModel.addElement(item);
			}
		}
	}
}
