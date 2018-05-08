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
import java.net.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;
import javax.xml.stream.*;

import com.github.meinders.common.*;
import com.github.meinders.common.FileFilter;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.catalog.*;
import lithium.gui.*;
import lithium.io.*;
import lithium.io.catalog.*;

/**
 * A dialog allowing the user to edit a catalog.
 *
 * @author Gerrit Meinders
 */
public class CatalogEditor extends JInternalFrameEx implements Editor,
        MenuSource, Savable, Exportable, ListSelectionListener,
        TreeSelectionListener, PropertyChangeListener
{

	/** The file filters available to save the editor's contents. */
	private static final FileFilter[] SAVE_FILTERS = /*
													  * { new
													  * ExtensionFileFilter(
													  * Resources
													  * .get().getString
													  * ("catalogEditor.catalog"
													  * ), "xml") };
													  */
	FilterManager.getFilters( FilterType.CATALOG);

	/** The file filters available to export the editor's contents. */
	private static final FileFilter[] EXPORT_FILTERS = {
	        new ExtensionFileFilter(Resources.get().getString(
	                "catalogEditor.plainText"), "txt"),
	        new ExtensionFileFilter(Resources.get().getString("filter.HTML"),
	                "html"),
	        new ExtensionFileFilter(Resources.get().getString(
	                "filter.htmlArchive"), "zip")
	};

	private EditorContext editorContext;

	/** The file that currently opened by the editor, if any. */
	private File file;

	/**
	 * The file filter that was last used to load or save the contents of the
	 * editor.
	 */
	private FileFilter filter;

	/** The catalog being edited. */
	private MutableCatalog catalog;

	/** The tree model listing all groups in the catalog. */
	private CatalogTreeModel treeModel;

	/** The table model listing all lyrics in the currently selected group. */
	private CatalogTableModel lyricTableModel;

	/** The menu item showing the auto-load state of the catalog. */
	private JCheckBoxMenuItem autoLoadItem;

	private JTree groupTree;

	private JTable lyricTable;

	/** The action of adding a bundle to the catalog. */
	private Action addBundleAction;

	/** The action of editing the selected bundle. */
	private Action editBundleAction;

	/** The action of removing the selected bundle. */
	private Action removeBundleAction;

	/** The action of adding a lyric to the selected bundle. */
	private Action addLyricAction;

	/** The action of editing the selected lyric. */
	private Action editLyricAction;

	/** The action of removing the selected lyric. */
	private Action removeLyricAction;

	/** The action of changing the auto-load state of the catalog. */
	private Action autoLoadAction;

	/** The action of merging the catalog with another. */
	private Action mergeAction;

	/** The action of comparing the catalog to another. */
	private Action compareAction;

	private final Collection<SelectionListener> selectionListeners;

	/** Constructs a new catalog editor, editing an empty catalog. */
	public CatalogEditor()
	{
		this(new DefaultCatalog(), null, null);
	}

	/**
	 * Constructs a new catalog editor, editing the given catalog.
	 *
	 * @param catalog the catalog to be edited
	 */
	public CatalogEditor(MutableCatalog catalog)
	{
		this(catalog, null, null);
	}

	/**
	 * Constructs a new catalog editor, editing the given catalog, which was
	 * loaded from the given file using the given file filter (allowing the
	 * catalog to be saved in the same format).
	 *
	 * @param catalog the catalog to be edited
	 * @param file the file from which the catalog was loaded
	 * @param filter the file filter that was used to load the catalog
	 */
	public CatalogEditor(MutableCatalog catalog, File file, FileFilter filter)
	{
		super((JInternalFrameEx) null, "");
		assert file == null ? filter == null : filter != null;

		selectionListeners = new ArrayList<SelectionListener>();
		addSelectionListener(new SelectionListener()
		{
			public void selectionChanged(Editor source)
			{
				updateActionStates();
			}
		});

		setCatalog(catalog);
		setFile(file);
		setFileFilter(filter);
		createComponents();
	}

	public EditorContext getEditorContext()
	{
		return editorContext;
	}

	public void setEditorContext(EditorContext editorContext)
	{
		this.editorContext = editorContext;
	}

	public void addSelectionListener(SelectionListener listener)
	{
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener)
	{
		selectionListeners.remove(listener);
	}

	protected void fireSelectionChange()
	{
		for (SelectionListener listener : selectionListeners)
		{
			listener.selectionChanged(this);
		}
	}

	/**
	 * Returns the catalog currently being edited by the editor.
	 *
	 * @return the catalog
	 */
	public MutableCatalog getCatalog()
	{
		return catalog;
	}

	/**
	 * Sets the catalog being edited by the editor.
	 *
	 * @param catalog the catalog to be edited
	 */
	private void setCatalog(MutableCatalog catalog)
	{
		this.catalog = catalog;
		CatalogManager.open(catalog);
		catalog.addPropertyChangeListener(this);
	}

	public JMenu[] getMenus()
	{
		autoLoadItem = new JCheckBoxMenuItem(autoLoadAction);
		setAutoLoadState();

		JMenu menu = new JMenu(Resources.get().getString(
		        "catalogEditor.catalog"));
		menu.setMnemonic(Resources.get().getMnemonic("catalogEditor.catalog"));
		menu.add(addBundleAction);
		menu.add(editBundleAction);
		menu.add(removeBundleAction);
		menu.addSeparator();
		menu.add(addLyricAction);
		menu.add(editLyricAction);
		menu.add(removeLyricAction);
		menu.addSeparator();
		menu.add(autoLoadItem);
		/*
		 * TODO: enable when merge/compare are implemented menu.addSeparator();
		 * menu.add(mergeAction); menu.add(compareAction);
		 */

		return new JMenu[] {
			menu
		};
	}

	public File getFile()
	{
		return file;
	}

	public FileFilter getFileFilter()
	{
		return filter;
	}

	/**
	 * Sets the file from which the contents of the editor was loaded.
	 *
	 * @param file the file
	 */
	private void setFile(File file)
	{
		this.file = file;
		setTitle();
		if (autoLoadAction != null)
		{
			setAutoLoadState();
		}
	}

	/**
	 * Sets the file filter that was used when the contents of the editor was
	 * loaded.
	 *
	 * @param filter the file filter
	 */
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

	public void save(File file, FileFilter filter) throws SaveException
	{
		assert file != null;
		assert filter != null;
		try
		{
			// TODO: this seems too complicated
			boolean saved = false;
			for (FileFilter knownFilter : SAVE_FILTERS)
			{
				if (knownFilter == filter)
				{
					CatalogIO.write(catalog, file);
					saved = true;
					break;
				}
			}
			if (!saved)
			{
				for (FileFilter knownFilter : FilterManager.getFilters( FilterType.CATALOG))
				{
					if (knownFilter == filter)
					{
						CatalogIO.write(catalog, file);
						saved = true;
						break;
					}
				}
			}
			if (!saved)
			{
				assert false : "unknown file filter: " + filter;
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
		try
		{
			if (filter.equals(EXPORT_FILTERS[0]))
			{
				PlainTextCatalogBuilder builder = new PlainTextCatalogBuilder(
				        catalog);
				ConfigurableDialog dialog = ConfigurableDialog.createDialog(
				        this, builder);
				if (dialog.showDialog())
				{
					builder.setOutput(new FileWriter(file));
					builder.call();
				}

			}
			else if (filter.equals(EXPORT_FILTERS[1]))
			{
				HtmlExporter exporter = new HtmlExporter();
				exporter.export(catalog, file);

			}
			else if (filter.equals(EXPORT_FILTERS[2]))
			{
				HTMLArchiveExporter exporter = new HTMLArchiveExporter();
				try
				{
					exporter.export(catalog, file);
				}
				catch (XMLStreamException e)
				{
					throw new IOException(e);
				}

			}
			else
			{
				assert false : "Unknown file filter.";
			}
		}
		catch (IOException e)
		{
			throw new ExportException(this, e);
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
		return catalog.isModified();
	}

	public void save() throws SaveException
	{
		save(getFile(), getFileFilter());
	}

	public Collection<Object> getSelectedItems()
	{
		Component focusOwner = getFocusOwner();

		Collection<Object> result = new ArrayList<Object>();

		if (focusOwner == groupTree)
		{
			TreePath[] selectionPaths = groupTree.getSelectionPaths();
			if (selectionPaths != null)
			{
				for (TreePath path : selectionPaths)
				{
					TreeNode node = (TreeNode) path.getLastPathComponent();
					result.add(CatalogTreeModel.getGroup(node));
				}
			}
		}
		else if (focusOwner == lyricTable)
		{
			for (int row : lyricTable.getSelectedRows())
			{
				lyricTableModel.getLyric(row);
			}
		}

		return result;
	}

	private Group getSelectedBundle()
	{
		TreePath leadPath = groupTree.getLeadSelectionPath();
		Group result;
		if (leadPath != null)
		{
			TreeNode node = (TreeNode) leadPath.getLastPathComponent();
			result = CatalogTreeModel.getGroup(node);
		}
		else
		{
			result = null;
		}
		return result;
	}

	private Lyric getSelectedLyric()
	{
		ListSelectionModel selectionModel = lyricTable.getSelectionModel();
		return lyricTableModel.getLyric(selectionModel.getLeadSelectionIndex());
	}

	/**
	 * {@inheritDoc} This method gets called when a bound property is changed.
	 *
	 * @param e A PropertyChangeEvent object describing the event source and the
	 *            property that has changed.
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		String property = e.getPropertyName();
		if (property == MutableCatalog.MODIFIED_PROPERTY)
		{
			setTitle();
		}
	}

	public void valueChanged(TreeSelectionEvent e)
	{
		fireSelectionChange();
	}

	public void valueChanged(ListSelectionEvent e)
	{
		fireSelectionChange();
	}

	// TODO: implement catalog merging
	private void merge()
	{
		assert false : "Not implemented";
	}

	// TODO: implement catalog comparison
	private void compare()
	{
		assert false : "Not implemented";

		JFileChooser chooser = FileChoosers.createFileChooser();
		chooser.setCurrentDirectory(ConfigManager.getLyricsFolder());
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.addChoosableFileFilter(FilterManager.getCombinedFilter( FilterType.CATALOG));

		int option = chooser.showDialog(this, "catalogEditor.compare");
		if (option == JFileChooser.APPROVE_OPTION)
		{
			File file = chooser.getSelectedFile();
			Catalog otherCatalog;
			try
			{
				otherCatalog = CatalogManager.getCatalog(file.toURI().toURL());
			}
			catch (MalformedURLException e)
			{
				throw new AssertionError(e);
			}
			catch (Exception e)
			{
				// TODO: show exception dialog
				return;
			}

			Set<Group> allBundles = new LinkedHashSet<Group>();
			allBundles.addAll(catalog.getBundles());
			allBundles.addAll(otherCatalog.getBundles());
			for (Group bundle : allBundles)
			{
				Group myBundle = catalog.getBundle(bundle.getName());
				Group otherBundle = otherCatalog.getBundle(bundle.getName());
				// TODO: Comparison of two catalogs.
				/*
				 * Currently not implemented, cuz the design hasn't been worked
				 * out really well yet.
				 */
			}
		}
	}

	private void updateActionStates()
	{
		Collection<Object> selection = getSelectedItems();

		boolean groupsSelected = false;
		boolean lyricsSelected = false;
		for (Object selected : selection)
		{
			if (selected instanceof Group)
			{
				groupsSelected = true;
			}
			else if (selected instanceof Lyric)
			{
				lyricsSelected = true;
			}
		}

		addLyricAction.setEnabled(groupsSelected);
		editBundleAction.setEnabled(groupsSelected);
		removeBundleAction.setEnabled(groupsSelected);
		editLyricAction.setEnabled(lyricsSelected);
		removeLyricAction.setEnabled(lyricsSelected);
	}

	/** Creates the user interface components that make up the editor. */
	private void createComponents()
	{
		setResizable(true);
		setClosable(true);
		setMaximizable(true);
		setIconifiable(true);

		createActions();
		setContentPane(createContentPane());
		addInternalFrameListener(new InternalFrameAdapter()
		{
			@Override
			public void internalFrameClosed(InternalFrameEvent e)
			{
				CatalogManager.close(catalog);
			}
		});
		setTitle();

		pack();
		setVisible(true);
	}

	/**
	 * Sets the title of the editor to provide information about the editor's
	 * contents.
	 */
	private void setTitle()
	{
		String title;
		if (file == null)
		{
			title = Resources.get().getString("catalogEditor.untitled");
		}
		else
		{
			title = file.getName();
		}
		if (catalog.isModified())
		{
			title += "*";
		}
		setTitle(title);
	}

	/** Creates the actions used by the editor. */
	private void createActions()
	{
		addBundleAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.newBundle"))
		{
			public void actionPerformed(ActionEvent e)
			{
				AddBundleDialog dialog = new AddBundleDialog(
				        CatalogEditor.this, getCatalog());
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
		addBundleAction.setEnabled(true);

		editBundleAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.editBundle"))
		{
			public void actionPerformed(ActionEvent e)
			{
				EditBundleDialog dialog = new EditBundleDialog(
				        CatalogEditor.this, getSelectedBundle());
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
		editBundleAction.setEnabled(false);

		removeBundleAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.removeBundle"))
		{
			public void actionPerformed(ActionEvent e)
			{
				Group selection = getSelectedBundle();
				if (selection != null)
				{
					int choice = JOptionPane.showInternalConfirmDialog(
					        CatalogEditor.this, Resources.get().getString(
					                "catalogEditor.confirmRemoveBundle",
					                selection), Resources.get().getString(
					                "catalogEditor.removeBundle"),
					        JOptionPane.YES_NO_CANCEL_OPTION,
					        JOptionPane.QUESTION_MESSAGE);
					if (choice == JOptionPane.YES_OPTION)
					{
						catalog.removeBundle(selection);
					}
				}
			}
		};
		removeBundleAction.setEnabled(false);

		addLyricAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.addLyric"))
		{
			{
				setEnabled(false);
			}

			public void actionPerformed(ActionEvent e)
			{
				Group selection = getSelectedBundle();
				if (selection == null)
				{
					setEnabled(false);
				}
				else
				{
					AddLyricDialog dialog = new AddLyricDialog(
					        (Window) getTopLevelAncestor(), selection);
					dialog.setVisible(true);
				}
			}
		};

		editLyricAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.editLyric"))
		{
			{
				setEnabled(false);
			}

			public void actionPerformed(ActionEvent e)
			{
				Lyric selection = getSelectedLyric();
				assert selection != null : "Action should have been disabled.";

				EditLyricDialog dialog = new EditLyricDialog(
				        (Window) getTopLevelAncestor(), selection);
				dialog.setVisible(true);
			}
		};

		removeLyricAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.removeLyric"))
		{
			{
				setEnabled(false);
			}

			public void actionPerformed(ActionEvent e)
			{
				Group selectedBundle = getSelectedBundle();
				Lyric selectedLyric = getSelectedLyric();
				int choice = JOptionPane.showInternalConfirmDialog(
				        CatalogEditor.this, Resources.get().getString(
				                "catalogEditor.confirmRemoveLyric",
				                selectedLyric), Resources.get().getString(
				                "catalogEditor.removeLyric"),
				        JOptionPane.YES_NO_CANCEL_OPTION,
				        JOptionPane.QUESTION_MESSAGE);
				if (choice == JOptionPane.YES_OPTION)
				{
					selectedBundle.removeLyric(selectedLyric.getNumber());
				}
			}
		};

		autoLoadAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.autoLoad"))
		{
			{
				setEnabled(false);
			}

			public void actionPerformed(ActionEvent e)
			{
				Config config = ConfigManager.getConfig();
				Collection<URL> catalogs = config.getCatalogURLs();
				try
				{
					URL url = file.toURI().toURL();
					boolean autoLoad = catalogs.contains(url);
					assert (autoLoad != autoLoadItem.isSelected()) : "GUI inconsistency";
					if (autoLoad)
					{
						// disable autoLoad
						config.removeCatalogURL(url);
					}
					else
					{
						// enable autoLoad
						config.addCatalogURL(url);
					}
				}
				catch (MalformedURLException ex)
				{
					// should have been prevented
					throw new AssertionError(ex);
				}

				try
				{
					ConfigManager.writeConfig();
				}
				catch (IOException ex)
				{
					// failed to save config
					String message = Resources.get().getString(
					        "config.writeFailed", ex.getLocalizedMessage());
					String title = Resources.get().getString(
					        "catalogEditor.autoLoad");
					JOptionPane.showMessageDialog(CatalogEditor.this, message,
					        title, JOptionPane.WARNING_MESSAGE);
					ex.printStackTrace();
					return;
				}

				setAutoLoadState();
			}
		};

		mergeAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.ellipsis",
		        Resources.get().getString("catalogEditor.merge")))
		{
			{
				setEnabled(false);
			}

			public void actionPerformed(ActionEvent e)
			{
				merge();
			}
		};

		compareAction = new AbstractAction(Resources.get().getString(
		        "catalogEditor.ellipsis",
		        Resources.get().getString("catalogEditor.compare")))
		{
			{
				setEnabled(false);
			}

			public void actionPerformed(ActionEvent e)
			{
				compare();
			}
		};
	}

	/**
	 * Creates the content pane.
	 *
	 * @return the created component
	 */
	private JPanel createContentPane()
	{
		JPanel contentPane = new JPanel();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(createCatalogView(), BorderLayout.CENTER);
		return contentPane;
	}

	/**
	 * Creates the catalog view, containing both a tree and table to view the
	 * contents of the catalog.
	 *
	 * @return the created component
	 */
	private JComponent createCatalogView()
	{
		treeModel = new CatalogTreeModel(catalog);
		JTree tree = new JTree(treeModel);
		tree.getSelectionModel().addTreeSelectionListener(this);

		lyricTableModel = new CatalogTableModel(catalog);
		tree.addTreeSelectionListener(lyricTableModel);
		final JTable table = new JTable(lyricTableModel);

		table.setShowGrid(false);
		table.setIntercellSpacing(new Dimension(0, 0));
		TableCellRenderer paddedDefaultRenderer = new PaddedCellRenderer(
		        new DefaultTableCellRenderer(), 1, 1);
		table.setDefaultRenderer(Integer.class, paddedDefaultRenderer);
		table.setDefaultRenderer(String.class, paddedDefaultRenderer);
		table.setRowHeight(table.getRowHeight() + 2);

		table.getSelectionModel().addListSelectionListener(this);

		this.groupTree = tree;
		this.lyricTable = table;

		// update column widths after table structure changes
		lyricTableModel.addTableModelListener(new TableModelListener()
		{
			public void tableChanged(TableModelEvent e)
			{
				if (e.getFirstRow() == TableModelEvent.HEADER_ROW)
				{
					// set column widths (after other listeners are done)
					SwingUtilities.invokeLater(new Runnable()
					{
						public void run()
						{
							for (int i = 0; i < table.getColumnModel().getColumnCount(); i++)
							{
								TableColumn column = table.getColumnModel().getColumn(
								        i);
								switch (i)
								{
								case 0:
									column.setMaxWidth(75);
									break;
								case 1:
									column.setPreferredWidth(400);
									break;
								case 2:
									column.setPreferredWidth(100);
									break;
								}
							}
						}
					});
				}
			}
		});

		JScrollPane treeScroller = new JScrollPane(tree);
		JScrollPane tableScroller = new JScrollPane(table);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(treeScroller);
		splitPane.setRightComponent(tableScroller);
		splitPane.setResizeWeight(0.4);
		splitPane.setDividerLocation((int) (tableScroller.getPreferredSize().getWidth() * 0.4));

		// popup menus
		JPopupMenu treePopup = new JPopupMenu();
		treePopup.add(addBundleAction);
		treePopup.add(editBundleAction);
		treePopup.add(removeBundleAction);
		tree.addMouseListener(new PopupListener(treePopup));
		treeScroller.addMouseListener(new PopupListener(treePopup));

		JPopupMenu tablePopup = new JPopupMenu();
		tablePopup.add(addLyricAction);
		tablePopup.add(editLyricAction);
		tablePopup.add(removeLyricAction);
		table.addMouseListener(new PopupListener(tablePopup));
		tableScroller.addMouseListener(new PopupListener(tablePopup));

		// default actions (double click)
		tree.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() > 1)
				{
					if (getSelectedBundle() != null)
					{
						editBundleAction.actionPerformed(null);
					}
				}
			}
		});

		table.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() > 1)
				{
					editLyricAction.actionPerformed(null);
				}
			}
		});

		return splitPane;
	}

	/**
	 * Retrieves an icon from the given resource.
	 *
	 * @return the icon, or <code>null</code> if no icon could be retrieved
	 */
	private ImageIcon getIcon(String resource)
	{
		try
		{
			return new ImageIcon(ImageIO.read(getClass().getResource(resource)));
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the selected property of the <code>autoLoadItem</code> to match the
	 * current auto-load state of the editor's contents.
	 */
	private void setAutoLoadState()
	{
		if (file == null)
		{
			autoLoadItem.setSelected(false);
		}
		else
		{
			autoLoadAction.setEnabled(file != null);
			Config config = ConfigManager.getConfig();
			Collection<URL> catalogs = config.getCatalogURLs();
			try
			{
				URL url = file.toURI().toURL();
				autoLoadItem.setSelected(catalogs.contains(url));
			}
			catch (MalformedURLException e)
			{
				autoLoadItem.setSelected(false);
			}
		}
	}

	public boolean isEditorFor(Object edited)
	{
		return catalog == edited;
	}
}
