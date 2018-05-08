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
import java.io.*;
import java.util.*;
import java.util.List;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.GroupLayout.*;
import javax.swing.event.*;
import javax.swing.text.*;

import com.github.meinders.common.*;
import com.github.meinders.common.FileFilter;
import lithium.*;
import lithium.catalog.*;
import lithium.display.*;
import lithium.gui.*;
import lithium.io.*;

import static javax.swing.Action.*;

/**
 * An editor allowing the user to edit a playlist.
 *
 * @version 0.9 (2006.12.26)
 * @author Gerrit Meinders
 */
public class PlaylistEditor extends JInternalFrameEx implements Editor,
        MenuSource, Savable, Exportable, PropertyChangeListener,
        ListSelectionListener
{

	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	private static final FileFilter[] SAVE_FILTERS = { FilterManager.getCombinedFilter( FilterType.PLAYLIST) };

	private static final FileFilter[] EXPORT_FILTERS = { new ExtensionFileFilter(
	        Resources.get().getString("playlistEditor.plainText"), "txt") };

	private EditorContext editorContext;

	private File file;

	private FileFilter filter;

	private Playlist playlist;

	private JList list;

	private Action addAction;

	private Action addImagesAction;

	private Action findAction;

	private Action removeAction;

	private Action moveUpAction;

	private Action moveDownAction;

	private Action playAction;

	private Action editLyricAction;

	private Action quickAddAction;

	private JTextField quickAddField;

	private JToolBar toolBar;

	private ItemSettingsPanel itemSettings;

	/**
	 * Constructs a new playlist editor, editing an empty playlist.
	 */
	public PlaylistEditor()
	{
		this(new Playlist(), null, null);
	}

	/**
	 * Constructs a new playlist editor, editing the specified playlist.
	 *
	 * @param playlist the playlist to be edited
	 */
	public PlaylistEditor(Playlist playlist)
	{
		this(playlist, null, null);
	}

	/**
	 * Constructs a new playlist editor, editing the specified playlist.
	 *
	 * @param playlist the playlist to be edited
	 * @param file the file that the playlist is stored in
	 * @param filter the file filter identifying the file type of the stored
	 *            playlist
	 */
	public PlaylistEditor(Playlist playlist, File file, FileFilter filter)
	{
		super((JInternalFrameEx) null, "");
		setPlaylist(playlist);
		setFile(file);
		setFileFilter(filter);
		init();
	}

	/**
	 * Returns the playlist being edited.
	 *
	 * @return the playlist being edited
	 */
	public Playlist getPlaylist()
	{
		return playlist;
	}

	public JMenu[] getMenus()
	{
		JMenu[] menus = new JMenu[1];

		final ResourceUtilities resources = Resources.get("playlistEditor");
		JMenu menu = new JMenu(resources.getString("playlist"));
		menu.setMnemonic(resources.getMnemonic("playlist"));
		menu.add(playAction);
		menu.addSeparator();
		menu.add(addAction);
		menu.add(removeAction);
		JMenu subMenu = new JMenu(resources.getString("move"));
		subMenu.setMnemonic(resources.getMnemonic("move"));
		subMenu.add(moveUpAction);
		subMenu.add(moveDownAction);
		menu.add(subMenu);
		menu.addSeparator();
		menu.add(findAction);
		menus[0] = menu;

		return menus;
	}

	public File getFile()
	{
		return file;
	}

	public FileFilter getFileFilter()
	{
		return filter;
	}

	private void setFile(File file)
	{
		this.file = file;
		setTitle();
	}

	private void setFileFilter(FileFilter filter)
	{
		this.filter = filter;
	}

	public FileFilter[] getSaveFilters()
	{
		return SAVE_FILTERS;
	}

	public FileFilter[] getExportFilters()
	{
		return EXPORT_FILTERS;
	}

	/**
	 * Opens a search dialog associated with the editor.
	 */
	public void find()
	{
		FindLyricDialog dialog = new FindLyricDialog(PlaylistEditor.this,
		        playlist);
		getDesktopPane().add(dialog);
		dialog.setVisible(true);
		try
		{
			dialog.setSelected(true);
		}
		catch (PropertyVetoException ex)
		{
			// accept veto
		}
	}

	public void save(File file, FileFilter filter) throws SaveException
	{
		assert file != null;
		assert filter != null;
		try
		{
			if (filter.equals(SAVE_FILTERS[0]))
			{
				PlaylistIO.write(playlist, file);
			}
			else
			{
				assert false : "Unknown file filter.";
			}
		}
		catch (IOException e)
		{
			throw new SaveException(this, e);
		}
		setFile(file);
		setFileFilter(filter);
	}

	public void export(File file, FileFilter filter) throws ExportException
	{
		assert file != null;
		assert filter != null;
		if (filter.equals(EXPORT_FILTERS[0]))
		{
			assert false : "not implemented";
			// TODO: extension: Export to plain text
		}
		else
		{
			assert false : "Unknown file filter.";
		}
	}

	public boolean isSaved()
	{
		assert getFile() == null ? getFileFilter() == null
		        : getFileFilter() != null;
		return getFile() != null;
	}

	public boolean isModified()
	{
		return playlist.isModified();
	}

	public void save() throws SaveException
	{
		save(getFile(), getFileFilter());
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		String property = e.getPropertyName();
		if (property == Playlist.ITEMS_PROPERTY)
		{
			updateList();
		}
		else if (property == Playlist.MODIFIED_PROPERTY)
		{
			setTitle();
		}
	}

	public void valueChanged(ListSelectionEvent e)
	{
		if (list.getSelectedIndex() == -1)
		{
			moveUpAction.setEnabled(false);
			moveDownAction.setEnabled(false);
			removeAction.setEnabled(false);
		}
		else
		{
			if (list.getModel().getSize() <= 1)
			{
				moveUpAction.setEnabled(false);
				moveDownAction.setEnabled(false);
			}
			else
			{
				moveUpAction.setEnabled(list.getSelectedIndex() > 0);
				moveDownAction.setEnabled(list.getSelectedIndex() < list.getModel().getSize() - 1);
			}
			if (list.getModel().getSize() > 0)
			{
				removeAction.setEnabled(list.getSelectedIndex() != -1);
			}
			else
			{
				removeAction.setEnabled(false);
			}
		}
	}

	private void setPlaylist(Playlist playlist)
	{
		assert playlist != null;
		if (this.playlist != null)
		{
			this.playlist.removePropertyChangeListener(this);
		}
		this.playlist = playlist;
		playlist.addPropertyChangeListener(this);
	}

	private void init()
	{
		setResizable(true);
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);

		createActions();
		setContentPane(createContentPane());
		pack();
		show();
	}

	private void setTitle()
	{
		String title;
		if (file == null)
		{
			title = Resources.get("playlistEditor").getString("untitled");
		}
		else
		{
			title = file.getName();
		}
		if (playlist.isModified())
		{
			title += "*";
		}
		setTitle(title);
	}

	private void createActions()
	{
		final ResourceUtilities resources = Resources.get("playlistEditor");
		addAction = new AbstractAction(resources.getString("add"))
		{
			public void actionPerformed(ActionEvent e)
			{
				SelectLyricDialog dialog = new SelectLyricDialog(
				        PlaylistEditor.this, playlist);
				getDesktopPane().add(dialog);
				try
				{
					dialog.setSelected(true);
				}
				catch (PropertyVetoException ex)
				{
					// accept veto
				}
			}
		};
		addAction.putValue(MNEMONIC_KEY, resources.getMnemonic("add"));
		addAction.putValue(SHORT_DESCRIPTION, resources.getString("add.tip"));
		addAction.setEnabled(true);

		addImagesAction = new AbstractAction(resources.getString("addImages"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				addImages();
			}
		};
		addImagesAction.putValue(SHORT_DESCRIPTION,
		        resources.getString("addImages.tip"));

		findAction = new AbstractAction(resources.getString("find"))
		{
			public void actionPerformed(ActionEvent e)
			{
				find();
			}
		};
		findAction.putValue(MNEMONIC_KEY, resources.getMnemonic("find"));
		findAction.putValue(SHORT_DESCRIPTION, resources.getString("find.tip"));
		findAction.setEnabled(true);

		removeAction = new AbstractAction(resources.getString("remove"))
		{
			public void actionPerformed(ActionEvent e)
			{
				int index = list.getSelectedIndex();
				assert index != -1 : "This action should have been disabled.";
				playlist.remove(index);
			}
		};
		removeAction.putValue(MNEMONIC_KEY, resources.getMnemonic("remove"));
		removeAction.putValue(SHORT_DESCRIPTION,
		        resources.getString("remove.tip"));
		removeAction.setEnabled(false);

		moveUpAction = new AbstractAction(resources.getString("up"))
		{
			public void actionPerformed(ActionEvent e)
			{
				int index = list.getSelectedIndex();
				if (index > 0)
				{
					playlist.swap(index, index - 1);
					list.setSelectedIndex(index - 1);
					list.ensureIndexIsVisible(index - 1);
				}
			}
		};
		moveUpAction.putValue(MNEMONIC_KEY, resources.getMnemonic("up"));
		moveUpAction.putValue(SHORT_DESCRIPTION, resources.getString("up.tip"));
		moveUpAction.setEnabled(false);

		moveDownAction = new AbstractAction(resources.getString("down"))
		{
			public void actionPerformed(ActionEvent e)
			{
				int index = list.getSelectedIndex();
				if (index < playlist.getLength())
				{
					playlist.swap(index, index + 1);
					list.setSelectedIndex(index + 1);
					list.ensureIndexIsVisible(index + 1);
				}
			}
		};
		moveDownAction.putValue(MNEMONIC_KEY, resources.getMnemonic("down"));
		moveDownAction.putValue(SHORT_DESCRIPTION,
		        resources.getString("down.tip"));
		moveDownAction.setEnabled(false);

		playAction = new AbstractAction(resources.getString("play"))
		{
			public void actionPerformed(ActionEvent e)
			{
				PlaylistManager.setPlaylist(playlist);
				FullScreenDisplay.start();

				EditorFrame editor = EditorFrame.getInstance();
				editor.showControls();
			}
		};
		playAction.putValue(MNEMONIC_KEY, resources.getMnemonic("play"));
		playAction.putValue(SHORT_DESCRIPTION, resources.getString("play.tip"));
		playAction.setEnabled(true);

		editLyricAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.editLyric"))
		{
			public void actionPerformed(ActionEvent e)
			{
				editPlaylistItem(playlist.getItem(list.getSelectedIndex()));
			}
		};
		editLyricAction.setEnabled(false);
	}

	private void editPlaylistItem(PlaylistItem item)
	{
		if (item != null)
		{
			Object value = item.getValue();
			if (value instanceof LyricRef)
			{
				LyricRef ref = (LyricRef) value;
				Lyric lyric = CatalogManager.getCatalog().getLyric(ref);
				if (lyric == null)
				{
					assert false : "Action should have been disabled.";
				}
				else
				{
					editLyric(lyric);
				}

			}
			else if (value instanceof Lyric)
			{
				editLyric((Lyric) value);

			}
			else if (value instanceof CharSequence)
			{
				EditTextDialog dialog = new EditTextDialog(
				        (Window) getTopLevelAncestor(), item);
				dialog.setVisible(true);

			}
			else
			{
				System.out.println("No support for editing playlist item of type "
				        + value.getClass());
			}
		}
	}

	private void editLyric(Lyric lyric)
	{
		EditLyricDialog dialog = new EditLyricDialog(
		        (Window) getTopLevelAncestor(), lyric, true);
		dialog.setVisible(true);
	}

	private void addImages()
	{
		ResourceUtilities resources = Resources.get();

		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setSelectedFiles(new File[0]);
		fileChooser.setMultiSelectionEnabled(true);
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.resetChoosableFileFilters();
		fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
		        resources.getString("media.images"),
		        ImageIO.getReaderFileSuffixes()));

		Dimension preferredSize = new Dimension(600, 500);
		fileChooser.setPreferredSize(preferredSize);

		GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();
		Rectangle screenBounds = graphicsConfiguration.getBounds();
		fileChooser.setLocation(
		        (int) (screenBounds.getWidth() - preferredSize.getWidth()) / 2,
		        (int) (screenBounds.getHeight() - preferredSize.getHeight()) / 2);

		int result = fileChooser.showOpenDialog(getTopLevelAncestor());
		if (result == JFileChooser.APPROVE_OPTION)
		{
			for (File file : fileChooser.getSelectedFiles())
			{
				playlist.add(new PlaylistItem(new ImageRef(file)));
			}
		}
	}

	private JPanel createContentPane()
	{
		list = new JList();
		list.addListSelectionListener(this);
		list.setCellRenderer(new PlaylistCellRenderer());
		ReorderListener.addToList(list, playlist);
		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() > 1)
				{
					if (!list.isSelectionEmpty())
					{
						editLyricAction.actionPerformed(null);
					}
				}
			}
		});
		updateList();

		ListSelectionModel selectionModel = list.getSelectionModel();
		selectionModel.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				Object[] selectedValues = list.getSelectedValues();
				if ((selectedValues == null) || (selectedValues.length == 0))
				{
					itemSettings.setPlaylistItems(Collections.<PlaylistItem> emptySet());
				}
				else
				{
					List<?> list = Arrays.asList(selectedValues);
					itemSettings.setPlaylistItems((List<PlaylistItem>) list);
				}
			}
		});

		JScrollPane scrollPane = new JScrollPane(list);
		scrollPane.setPreferredSize(new Dimension(400, 300));

		toolBar = createToolBar();
		toolBar.setPreferredSize(new Dimension(500, 20));

		itemSettings = new ItemSettingsPanel();

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(toolBar, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);
		panel.add(itemSettings, BorderLayout.SOUTH);
		return panel;
	}

	private void updateList()
	{
		PlaylistItem[] items = playlist.getItems().toArray(new PlaylistItem[0]);
		list.setListData(items);
	}

	private JToolBar createToolBar()
	{
		quickAddField = new JTextField();
		quickAddAction = new QuickAddAction(quickAddField)
		{
			@Override
			protected void playlistItemSelected(PlaylistItem item)
			{
				playlist.add(item);
			}
		};

		quickAddField.setBorder(BorderFactory.createCompoundBorder(
		        quickAddField.getBorder(), BorderFactory.createEmptyBorder(0,
		                5, 0, 5)));
		quickAddField.addActionListener(quickAddAction);

		JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		toolBar.setFloatable(false);
		toolBar.add(addAction);
		toolBar.add(addImagesAction);
		toolBar.add(removeAction);
		toolBar.addSeparator();
		toolBar.add(moveUpAction);
		toolBar.add(moveDownAction);
		toolBar.addSeparator();
		toolBar.add(quickAddField);
		toolBar.add(findAction);
		toolBar.addSeparator();
		toolBar.add(playAction);
		return toolBar;
	}

	private Icon getIcon(String name)
	{
		return editorContext == null ? null : editorContext.getIcon(name);
	}

	public EditorContext getEditorContext()
	{
		return editorContext;
	}

	public void setEditorContext(EditorContext editorContext)
	{
		this.editorContext = editorContext;

		addAction.putValue(SMALL_ICON, getIcon("general/Add16.gif"));
		addAction.putValue(LARGE_ICON_KEY, getIcon("general/Add24.gif"));
		// addImagesAction.putValue(SMALL_ICON,
		// getIcon("custom/AddImages16.gif"));
		addImagesAction.putValue(LARGE_ICON_KEY,
		        getIcon("custom/AddImages24.gif"));
		findAction.putValue(SMALL_ICON, getIcon("general/Find16.gif"));
		findAction.putValue(LARGE_ICON_KEY, getIcon("general/Find24.gif"));
		removeAction.putValue(SMALL_ICON, getIcon("general/Delete16.gif"));
		removeAction.putValue(LARGE_ICON_KEY, getIcon("general/Delete24.gif"));
		moveUpAction.putValue(SMALL_ICON, getIcon("navigation/Up16.gif"));
		moveUpAction.putValue(LARGE_ICON_KEY, getIcon("navigation/Up24.gif"));
		moveDownAction.putValue(SMALL_ICON, getIcon("navigation/Down16.gif"));
		moveDownAction.putValue(LARGE_ICON_KEY,
		        getIcon("navigation/Down24.gif"));
		playAction.putValue(SMALL_ICON, getIcon("media/Play16.gif"));
		playAction.putValue(LARGE_ICON_KEY, getIcon("media/Play24.gif"));

		remove(toolBar);
		toolBar = createToolBar();
		add(toolBar, BorderLayout.NORTH);
		revalidate();
	}

	public Collection<Object> getSelectedItems()
	{
		return Collections.emptySet();
	}

	public boolean isEditorFor(Object edited)
	{
		return playlist == edited;
	}

	private static class ItemSettingsPanel extends JPanel
	{
		private final JLabel preview;

		private final JLabel title;

		private final JTextField transitionDelay;

		private final Set<PlaylistItem> playlistItems;

		public ItemSettingsPanel()
		{
			playlistItems = new HashSet<PlaylistItem>();

			preview = new JLabel();
			preview.setOpaque(true);
			preview.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

			title = new JLabel("title");
			transitionDelay = new JTextField(3);
			Document document = transitionDelay.getDocument();
			document.addDocumentListener(new DocumentListener()
			{
				@Override
				public void changedUpdate(DocumentEvent e)
				{
					update();
				}

				@Override
				public void insertUpdate(DocumentEvent e)
				{
					update();
				}

				@Override
				public void removeUpdate(DocumentEvent e)
				{
					update();
				}

				private void update()
				{
					String text = transitionDelay.getText();
					int transitionDelay = 0;
					boolean valid = true;
					try
					{
						double number = Math.round(Double.parseDouble(text) * 1000.0);
						if (Double.isNaN(number) || Double.isInfinite(number)
						        || number < 0)
						{
							valid = false;
						}
						else
						{
							transitionDelay = (int) number;
						}
					}
					catch (NumberFormatException e)
					{
						valid = false;
					}

					JTextField component = ItemSettingsPanel.this.transitionDelay;
					component.setBackground(Color.WHITE);
					if (valid)
					{
						for (PlaylistItem playlistItem : playlistItems)
						{
							playlistItem.setTransitionDelay(transitionDelay);
						}
					}
					else if (!text.isEmpty())
					{
						component.setBackground(new Color(0xffe0c0));
					}
				}
			});

			GroupLayout layout = new GroupLayout(this);
			layout.setAutoCreateContainerGaps(true);
			layout.setAutoCreateGaps(true);
			layout.setLayoutStyle(new LayoutStyle()
			{
				@Override
				public int getContainerGap(JComponent component, int position,
				        Container parent)
				{
					return 5;
				}

				@Override
				public int getPreferredGap(JComponent component1,
				        JComponent component2, ComponentPlacement type,
				        int position, Container parent)
				{
					return 5;
				}
			});
			setLayout(layout);

			SequentialGroup horizontal = layout.createSequentialGroup();
			ParallelGroup vertical = layout.createParallelGroup(GroupLayout.Alignment.CENTER);

			layout.setHorizontalGroup(horizontal);
			layout.setVerticalGroup(vertical);

			horizontal.addComponent(preview, 120, 120, 120);
			vertical.addComponent(preview, 90, 90, 90);

			ParallelGroup settingsColumn = layout.createParallelGroup(GroupLayout.Alignment.CENTER);
			SequentialGroup settingsRows = layout.createSequentialGroup();

			horizontal.addGroup(settingsColumn);
			vertical.addGroup(settingsRows);

			// settingsColumn.addComponent(title);
			// settingsRows.addComponent(title);

			SequentialGroup columns = layout.createSequentialGroup();
			settingsColumn.addGroup(columns);

			ParallelGroup labels = layout.createParallelGroup();
			ParallelGroup fields = layout.createParallelGroup();
			columns.addGroup(labels);
			columns.addGroup(fields);
			columns.addGap(0, 0, Short.MAX_VALUE);

			{
				ParallelGroup row = layout.createBaselineGroup(false, true);
				settingsRows.addGroup(row);

				JLabel label = new JLabel("Duur:");
				label.setLabelFor(transitionDelay);

				labels.addComponent(label);
				row.addComponent(label);

				fields.addComponent(transitionDelay);
				row.addComponent(transitionDelay);
			}

			setPlaylistItemsImpl(Collections.<PlaylistItem> emptySet());
		}

		public void setPlaylistItems(Collection<PlaylistItem> playlistItems)
		{
			if (!this.playlistItems.equals(Arrays.asList(playlistItems)))
			{
				setPlaylistItemsImpl(playlistItems);
			}
		}

		private void setPlaylistItemsImpl(Collection<PlaylistItem> playlistItems)
		{
			this.playlistItems.clear();
			if (playlistItems != null)
			{
				this.playlistItems.addAll(playlistItems);
			}

			boolean enabled = (playlistItems.size() >= 1);
			setEnabled(enabled);
			title.setEnabled(enabled);
			transitionDelay.setEnabled(enabled);

			if (enabled)
			{
				double delaySeconds;

				if (playlistItems.size() == 1)
				{
					PlaylistItem playlistItem = playlistItems.iterator().next();
					title.setText(playlistItem.getTitle());

					Object value = playlistItem.getValue();
					// TODO: preview

					delaySeconds = playlistItem.getTransitionDelay() / 1000.0;

				}
				else
				{
					title.setText(playlistItems.size() + " items");

					int delayTotal = 0;
					int delayCount = 0;
					for (PlaylistItem playlistItem : playlistItems)
					{
						int delay = playlistItem.getTransitionDelay();
						if (delay > 0)
						{
							delayTotal += delay;
							delayCount++;
						}
					}

					delaySeconds = delayCount == 0 ? 0 : delayTotal
					        / (delayCount * 1000.0);
					transitionDelay.setText(String.valueOf(delaySeconds));
				}

				transitionDelay.setText(String.valueOf(delaySeconds));

			}
			else
			{
				title.setText("");
				transitionDelay.setText("");
			}
		}

	}
}
