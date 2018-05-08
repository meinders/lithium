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
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;

import com.github.meinders.common.*;
import com.github.meinders.common.FileFilter;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.audio.*;
import lithium.catalog.*;
import lithium.display.*;
import lithium.display.java2d.*;
import lithium.display.opengl.*;
import lithium.gui.*;
import lithium.io.*;
import lithium.io.catalog.*;
import lithium.io.opspro.*;

import static javax.swing.Action.*;
import static javax.swing.JSplitPane.*;

/**
 * <p>
 * This class contains the main part of the editor GUI. It provides the common
 * menus, such as the File menu, and thereby provides access to the other frames
 * and dialogs of the editor GUI. Additionaly, this class handles the extra
 * controls used with full-screen mode on multiple monitors.
 *
 * @see CatalogEditor
 * @see ConfigEditor
 * @see PlaylistEditor
 *
 * @since 0.3
 * @author Gerrit Meinders
 */
public class EditorFrame extends JFrame implements VetoableChangeListener,
        EditorContext
{
	private static final int TAB_DESKTOP = 0;

	private static final int TAB_CONTROLS = 1;

	private static EditorFrame instance = null;

	/**
	 * Runs an instance of the editor frame, or activates an existing instance.
	 */
	public static void start()
	{
		// (optionally) create instance, show it and give it focus
		if (instance == null)
		{
			Config config = ConfigManager.getConfig();
			DisplayConfig displayConfig = config.getDisplayConfig(DisplayConfig.EDITOR_MODE);
			instance = new EditorFrame(displayConfig.getGraphicsConfiguration());
		}

		// warn about older Java versions
		String version = System.getProperty("java.version");
		String optimalVersion = "1.6";
		if (optimalVersion.compareTo(version) >= 0)
		{
			ResourceUtilities res = Resources.get();
			String message = res.getString("java.versionWarning", version,
			        optimalVersion);
			String title = res.getString("java.versionWarningTitle");
			JOptionPane.showMessageDialog(instance, message, title,
			        JOptionPane.WARNING_MESSAGE);
		}

		instance.showAndFocus();
	}

	/**
	 * Returns the current editor frame instance.
	 *
	 * @return the editor frame
	 * @throws IllegalStateException if the editor is not started.
	 */
	public static EditorFrame getInstance()
	{
		if (instance == null)
		{
			throw new IllegalStateException("Editor is not started.");
		}
		return instance;
	}

	/**
	 * Returns whether an editor frame has been started.
	 *
	 * @return {@code true} if an editor was started; {@code false} otherwise
	 */
	public static boolean isStarted()
	{
		return instance != null;
	}

	private JDesktopPaneEx desktop;

	private JScrollPane desktopScroller;

	private JInternalFrame activeFrame;

	private ConfigEditor configEditor;

	private JPanel scrollControlsPanel = null;

	private GLView view = null;

	private ControlsPanel controlsPanel = null;

	private JTabbedPane desktopSwitcher = null;

	private boolean controlsOpen;

	private Action newAction;

	private Action openAction;

	private Action saveAction;

	private Action saveAsAction;

	private Action importOPSProAction;

	private Action importClassicFormatAction;

	private Action importPlainTextCatalogAction;

	private Action exportAction;

	private Action preferencesAction;

	private Action exitAction;

	private Action scrollViewAction;

	private Action findAction;

	private Action aboutAction;

	private final Clipboard localClipboard = new Clipboard("Local");

	/**
	 * Constructs a new editor.
	 */
	private EditorFrame(GraphicsConfiguration gc)
	{
		super(Resources.get().getString("editorFrame.title"), gc);
		init();
	}

	public Clipboard getLocalClipboard()
	{
		return localClipboard;
	}

	/**
	 * Configures the editor frame, setting the menus, content pane, size and
	 * default close operation. This method also calls createActions before any
	 * methods that may depend on it.
	 */
	private void init()
	{
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				if (confirmClosing())
				{
					dispose();
				}
			}
		});

		updateMaximizedBounds();
		addComponentListener(new ComponentAdapter()
		{
			@Override
			public void componentMoved(ComponentEvent e)
			{
				updateMaximizedBounds();
			}
		});

		setIconImage(new ImageIcon(getClass().getResource(
		        "/images/opwViewer32.gif")).getImage());
		createActions();
		setJMenuBar(createMenuBar());
		setContentPane(createContentPane());

		// realise the frame (seems to change the behavior of setExtendedState)
		pack();

		final GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();

		// set size to 80% screen size
		final GraphicsDevice device = graphicsConfiguration.getDevice();
		final Rectangle screen = device.getDefaultConfiguration().getBounds();
		Insets screenInsets = getToolkit().getScreenInsets(
		        graphicsConfiguration);
		screen.width -= screenInsets.left + screenInsets.right;
		screen.height -= screenInsets.top + screenInsets.bottom;
		setSize(screen.width * 8 / 10, screen.height * 8 / 10);
		setLocation(screen.x + screenInsets.left + (screen.width - getWidth())
		        / 2, screen.y + screenInsets.top
		        + (screen.height - getHeight()) / 2);

		setExtendedState(MAXIMIZED_BOTH);

		// set debugging options
		Config config = ConfigManager.getConfig();

		if (config.isEnabled(Config.SHOW_CONTROLS_AT_STARTUP))
		{
			// test controls
			final ViewModel model = new ViewModel();
			model.register(null);
			model.setPlaylist(new Playlist());
			openControls(model);
			addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(WindowEvent e)
				{
					model.unregister(null);
				}
			});
		}

		/*
		 * Since the config editor is rather slow to create, it's better to keep
		 * an instance ready.
		 */
		configEditor = new ConfigEditor(this, config);
	}

	/**
	 * Sets the maximized bounds of the window to take screen insets into
	 * account.
	 */
	private void updateMaximizedBounds()
	{
		final GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();
		final GraphicsDevice device = graphicsConfiguration.getDevice();
		final Rectangle screen = device.getDefaultConfiguration().getBounds();
		Insets screenInsets = Toolkit.getDefaultToolkit().getScreenInsets(
		        graphicsConfiguration);
		final Rectangle maximizedBounds = new Rectangle(screenInsets.left,
		        screenInsets.top,
		        (screen.width - (screenInsets.left + screenInsets.right)),
		        (screen.height - (screenInsets.top + screenInsets.bottom)));
		setMaximizedBounds(maximizedBounds);
	}

	/**
	 * Shows the editor, focuses it and (optionally) sets debug options.
	 */
	private void showAndFocus()
	{
		// show and focus
		setVisible(true);
		requestFocus();
	}

	@Override
	public void dispose()
	{
		if (isControlsOpen())
		{
			closeControls();
		}
		EditorFrame.instance = null;
		super.dispose();
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				System.exit(0);
			}
		});
	}

	private void newFile()
	{
		FileFilter catalogFilter = FilterManager.getCombinedFilter( FilterType.CATALOG);
		FileFilter playlistFilter = FilterManager.getCombinedFilter( FilterType.PLAYLIST);

		IconListChooser<FileFilter> chooser = new IconListChooser<FileFilter>(
		        Resources.get().getString("new"));
		chooser.add(catalogFilter,
		        FilterManager.getFilterIcon( FilterType.CATALOG));
		chooser.add(playlistFilter,
		        FilterManager.getFilterIcon( FilterType.PLAYLIST));
		chooser.setPreferredSize(new Dimension(200, 100));
		FileFilter selectedValue = chooser.showSelectionDialog(this);

		if (selectedValue != null)
		{
			if (selectedValue == catalogFilter)
			{
				addToDesktop(new CatalogEditor());
			}
			else if (selectedValue == playlistFilter)
			{
				addToDesktop(new PlaylistEditor());
			}
			else
			{
				assert false;
			}
		}
	}

	private void openFile()
	{
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileFilter catalogFilter = FilterManager.getCombinedFilter( FilterType.CATALOG);
		FileFilter playlistFilter = FilterManager.getCombinedFilter( FilterType.PLAYLIST);
		fileChooser.addChoosableFileFilter(catalogFilter);
		fileChooser.addChoosableFileFilter(playlistFilter);

		int option = fileChooser.showOpenDialog(EditorFrame.this);
		if (option == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();

			if (fileChooser.isAcceptAllFileFilterUsed())
			{
				// TODO: detect type or something
			}
			else
			{
				// open as selected file type
				FileFilter filter = (FileFilter) fileChooser.getFileFilter();
				if (filter == catalogFilter)
				{
					openCatalog(file, filter);
				}
				else if (filter == playlistFilter)
				{
					openPlaylist(file, filter);
				}
				else
				{
					showExceptionDialog(Resources.get().getString(
					        "editorFrame.unknownType"), new Exception(
					        "FileFilter = " + filter.toString()));
				}
			}
		}
	}

	/**
	 * Opens a catalog from the specified file.
	 *
	 * @param file the file to be opened
	 * @param filter the file filter indicating the file's content type
	 */
	public void openCatalog(File file, FileFilter filter)
	{
		try
		{
			MutableCatalog catalog = CatalogManager.getCatalog(file.toURI().toURL());
			addToDesktop(new CatalogEditor(catalog, file, filter));
			ConfigManager.getConfig().setRecentFile(file);
		}
		catch (Exception e)
		{
			showExceptionDialog(Resources.get().getString(
			        "editorFrame.openCatalogException"), e);
		}
	}

	/**
	 * Opens a playlist from the specified file.
	 *
	 * @param file the file to be opened
	 * @param filter the file filter indicating the file's content type
	 */
	public void openPlaylist(File file, FileFilter filter)
	{
		try
		{
			Playlist playlist = PlaylistIO.read(file);
			showPlaylist(playlist);
		}
		catch (Exception e)
		{
			showExceptionDialog(Resources.get().getString(
			        "editorFrame.openPlaylistException"), e);
		}
	}

	public void showPlaylist(Playlist playlist)
	{
		showPlaylist(playlist, null, null);
	}

	private void showPlaylist(Playlist playlist, File file, FileFilter filter)
	{
		Editor existingEditor = getEditorFor(playlist);
		if (existingEditor == null)
		{
			addToDesktop(new PlaylistEditor(playlist, file, filter));
			if (file != null)
			{
				ConfigManager.getConfig().setRecentFile(file);
			}
		}
		else
		{
			((JInternalFrame) existingEditor).requestFocus();
		}
		showDesktop();
	}

	private Editor getEditorFor(Object edited)
	{
		for (JInternalFrame frame : desktop.getAllFrames())
		{
			if (frame instanceof Editor)
			{
				Editor editor = (Editor) frame;
				if (editor.isEditorFor(edited))
				{
					return editor;
				}
			}
		}
		return null;
	}

	private boolean save()
	{
		assert activeFrame instanceof Savable : "activeFrame is not Savable";
		Savable savable = (Savable) activeFrame;

		if (savable.isSaved())
		{
			try
			{
				savable.save();
				ConfigManager.getConfig().setRecentFile(savable.getFile());
				return true;

			}
			catch (Exception e)
			{
				e.printStackTrace();
				showExceptionDialog(Resources.get().getString(
				        "editorFrame.saveException", savable.getFile()), e);
				return false;
			}
		}
		else
		{
			return saveAs();
		}
	}

	private boolean saveAs()
	{
		assert activeFrame instanceof Savable : "activeFrame is not Savable";
		Savable savable = (Savable) activeFrame;

		// create save dialog
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileFilter[] filters = savable.getSaveFilters();
		for (int i = 0; i < filters.length; i++)
		{
			fileChooser.addChoosableFileFilter(filters[i]);
		}

		// show save dialog
		File file;
		FileFilter filter;
		while (true)
		{
			int option = fileChooser.showSaveDialog(this);
			if (option == JFileChooser.APPROVE_OPTION)
			{
				file = fileChooser.getSelectedFile();
				filter = (FileFilter) fileChooser.getFileFilter();
				file = filter.getAppropriateFile(file);

				// confirm overwriting an existing file
				if (file.exists())
				{
					int selection = JOptionPane.showConfirmDialog(this,
					        Resources.get().getString(
					                "editorFrame.overwriteExisting", file),
					        Resources.get().getString("editorFrame.saveAs"),
					        JOptionPane.YES_NO_CANCEL_OPTION,
					        JOptionPane.WARNING_MESSAGE);
					if (selection == JOptionPane.YES_OPTION)
					{
						break;
					}
					else if (selection == JOptionPane.CANCEL_OPTION)
					{
						return false;
					}
					else
					{
						// while-loop repeats -> user can select another file
					}
				}
				else
				{
					break;
				}
			}
			else
			{
				return false;
			}
		}

		// save
		try
		{
			savable.save(file, filter);
			ConfigManager.getConfig().setRecentFile(savable.getFile());
			return true;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			showExceptionDialog(Resources.get().getString(
			        "editorFrame.saveException", file), e);
			return false;
		}
	}

	private void export()
	{
		assert activeFrame instanceof Exportable : "activeFrame is not Exportable";
		Exportable exportable = (Exportable) activeFrame;

		// create export dialog
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileFilter[] filters = exportable.getExportFilters();
		for (int i = 0; i < filters.length; i++)
		{
			fileChooser.addChoosableFileFilter(filters[i]);
		}

		// show export dialog
		File file;
		FileFilter filter;
		while (true)
		{
			int option = fileChooser.showDialog(this,
			        Resources.get().getString("editorFrame.export"));
			if (option == JFileChooser.APPROVE_OPTION)
			{
				file = fileChooser.getSelectedFile();
				filter = (FileFilter) fileChooser.getFileFilter();
				file = filter.getAppropriateFile(file);

				// confirm overwriting an existing file
				if (file.exists())
				{
					int selection = JOptionPane.showConfirmDialog(this,
					        Resources.get().getString(
					                "editorFrame.overwriteExisting", file),
					        Resources.get().getString("editorFrame.export"),
					        JOptionPane.YES_NO_CANCEL_OPTION,
					        JOptionPane.WARNING_MESSAGE);
					if (selection == JOptionPane.YES_OPTION)
					{
						break;
					}
					else if (selection == JOptionPane.CANCEL_OPTION)
					{
						return;
					}
					else
					{
						// while-loop repeats -> user can select another file
					}
				}
				else
				{
					break;
				}
			}
			else
			{
				return;
			}
		}

		// export
		try
		{
			exportable.export(file, filter);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			showExceptionDialog(Resources.get().getString(
			        "editorFrame.exportException", file), e);
		}
	}

	private void importCatalog(MutableCatalog catalog)
	{
		if (catalog == null)
			throw new NullPointerException("catalog");

		boolean merge = false;
		if (activeFrame != null && activeFrame instanceof CatalogEditor)
		{
			File catalogFile = ((CatalogEditor) activeFrame).getFile();
			String catalogName = catalogFile == null ? Resources.get().getString(
			        "editorFrame.untitled")
			        : catalogFile.toString();
			int choice = JOptionPane.showConfirmDialog(EditorFrame.this,
			        Resources.get().getString("editorFrame.mergeImported",
			                catalogName), Resources.get().getString(
			                "editorFrame.import"), JOptionPane.YES_NO_OPTION,
			        JOptionPane.QUESTION_MESSAGE);
			merge = choice == JOptionPane.YES_OPTION;
		}

		if (merge)
		{
			assert activeFrame instanceof CatalogEditor;
			CatalogEditor frame = ((CatalogEditor) activeFrame);
			DefaultCatalog.merge(frame.getCatalog(), catalog);

		}
		else
		{
			addToDesktop(new CatalogEditor(catalog));
		}
	}

	private void importClassicFormat()
	{
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new ClassicFormatFileFilter());
		fileChooser.setFileView(new ClassicFormatFileView());

		int option = fileChooser.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION)
		{
			final File root = fileChooser.getSelectedFile();

			ProgressDialog<MutableCatalog> progressDialog = new ProgressDialog<MutableCatalog>(
			        EditorFrame.this, Resources.get().getString(
			                "editorFrame.importing"));
			progressDialog.setVisible(true);

			ClassicFormatParser worker = new ClassicFormatParser(root);
			progressDialog.setWorker(worker);
			worker.addWorkerListener(new WorkerListener<MutableCatalog>()
			{
				public void stateChanged(WorkerEvent<MutableCatalog> e)
				{
					switch (e.getType())
					{
					case FINISHED:
						MutableCatalog catalog = e.getSource().get();
						if (catalog != null)
						{
							importCatalog(catalog);
						}
					}
				}
			});

			progressDialog.setWorker(worker);
			worker.start();
		}
	}

	private void importOPSPro()
	{
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(false);
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fileChooser.setFileFilter(new ExtensionFileFilter(
		        "OPSPro/Access Database", "mdb"));
		fileChooser.setFileView(new ClassicFormatFileView());

		int option = fileChooser.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION)
		{
			final File root = fileChooser.getSelectedFile();

			ProgressDialog<MutableCatalog> progressDialog = new ProgressDialog<MutableCatalog>(
			        EditorFrame.this, Resources.get().getString(
			                "editorFrame.importing"));
			progressDialog.setVisible(true);

			OPSProImporter worker = new OPSProImporter(root, JOptionPane.showInputDialog( this, "Password for OPSPro database" ));
			progressDialog.setWorker(worker);
			worker.addWorkerListener(new WorkerListener<MutableCatalog>()
			{
				public void stateChanged(WorkerEvent<MutableCatalog> e)
				{
					switch (e.getType())
					{
					case FINISHED:
						MutableCatalog catalog = e.getSource().get();
						if (catalog != null)
						{
							importCatalog(catalog);
						}
					}
				}
			});

			progressDialog.setWorker(worker);
			worker.start();
		}
	}

	private void importPlainTextCatalog()
	{
		JFileChooser fileChooser = FileChoosers.createFileChooser();
		fileChooser.setAcceptAllFileFilterUsed(true);
		fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
		        Resources.get().getString("editorFrame.plainText"), "txt"));

		int option = fileChooser.showOpenDialog(this);
		if (option == JFileChooser.APPROVE_OPTION)
		{
			File file = fileChooser.getSelectedFile();
			String title = Resources.get().getString("editorFrame.importing");
			ProgressDialog<MutableCatalog> progressDialog = new ProgressDialog<MutableCatalog>(
			        EditorFrame.this, title);
			PlainTextCatalogParser parser = new PlainTextCatalogParser();
			boolean cancelled = PlainTextCatalogParserConfigDialog.showConfigurationDialog(
			        this, parser);

			if (!cancelled)
			{
				progressDialog.setVisible(true);
				PlainTextImporter importer = new PlainTextImporter(file, parser);
				progressDialog.setWorker(importer);
				importer.addWorkerListener(new PlainTextImporterListener());
				importer.start();
			}
		}
	}

	private void settings()
	{
		Dimension size = configEditor.getSize();
		Dimension parentSize = configEditor.getParent().getSize();
		configEditor.setLocation((parentSize.width - size.width) / 2,
		        (parentSize.height - size.height) / 2);
		setCursor(Cursor.getDefaultCursor());
		configEditor.setConfig(ConfigManager.getConfig());
		configEditor.setVisible(true);
	}

	public void showExceptionDialog(String message, Exception e)
	{
		showExceptionDialog(this, message, e);
	}

	private void showExceptionDialog(Component parent, String message,
	        Exception e)
	{
		if (e == null)
		{
			JOptionPane.showMessageDialog(parent, message, getTitle(),
			        JOptionPane.WARNING_MESSAGE);
		}
		else
		{
			StringBuilder messageAndTrace = new StringBuilder();
			messageAndTrace.append(message);
			messageAndTrace.append("\n\n");

			try
			{
				appendStackTrace(messageAndTrace, e, true);
			}
			catch (IOException ex)
			{
				// not applicable for StringBuilder
			}

			JOptionPane.showMessageDialog(parent, messageAndTrace.toString(),
			        e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
		}
	}

	/**
	 * Appends a stack trace for the given throwable and its causes, if any, to
	 * the given appendable character sequence.
	 *
	 * @param result the character sequence to append the stack trace to
	 * @param throwable the throwable to append the stack trace of
	 * @param excludeJavaAPI whether stack trace elements referring to standard
	 *            Java API classes should be excluded from the stack trace
	 */
	private void appendStackTrace(Appendable result, Throwable throwable,
	        boolean excludeJavaAPI) throws IOException
	{
		Throwable cause = throwable;
		List<StackTraceElement> previousTrace = Collections.emptyList();
		do
		{
			boolean afterApplicationClasses = false;
			boolean skippingJava = false;
			result.append(cause.toString());
			result.append("\n");

			StackTraceElement[] stackTrace = cause.getStackTrace();
			for (StackTraceElement element : stackTrace)
			{
				if (previousTrace.contains(element))
				{
					break;
				}

				String className = element.getClassName();
				boolean isJavaAPI = excludeJavaAPI
				        && (className.startsWith("java.") || className.startsWith("javax."));

				if (isJavaAPI && afterApplicationClasses)
				{
					if (!skippingJava)
					{
						skippingJava = true;
						result.append("    ...\n");
					}
				}
				else
				{
					result.append("    at ");
					result.append(element.toString());
					result.append("\n");
					skippingJava = false;
					afterApplicationClasses = true;
				}
			}

			cause = cause.getCause();
			if (cause != null)
			{
				result.append("Caused by: ");
				previousTrace = Arrays.asList(stackTrace);
			}
		}
		while (cause != null);
	}

	/**
	 * Adds controls for the specified lyric view model to the editor.
	 *
	 * @param model the model to be controlled
	 */
	public void openControls(ViewModel model)
	{
		assert !isControlsOpen() : "expected controls to be closed";

		// controls panel (select, search, playlist)
		controlsPanel = new ControlsPanel(model);
		getContentPane().add(controlsPanel, BorderLayout.SOUTH);

		// small scrollable preview
		view = new GLView(model, true);
		// final LyricViewGL lyricView2 = new LyricViewGL(model, false);

		JPanel lyricViewPanel = new JPanel();
		lyricViewPanel.setLayout(new BorderLayout());
		lyricViewPanel.add(new JToggleButton(new AbstractAction(
		        "Separated scrolling")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				JToggleButton source = (JToggleButton) e.getSource();
				view.setSeparatedScrolling(source.isSelected());
			}
		}), BorderLayout.NORTH);
		lyricViewPanel.add(view);

		scrollControlsPanel = new JPanel();
		scrollControlsPanel.setLayout(new BorderLayout());
		scrollControlsPanel.add(createRecorderControls(), BorderLayout.SOUTH);
		scrollControlsPanel.add(lyricViewPanel);
		// scrollControlsPanel.add(lyricView2, BorderLayout.EAST);

		// Dimension preferredSize = scrollControlsPanel.getPreferredSize();
		// preferredSize.width = 300;
		// scrollControlsPanel.setPreferredSize(preferredSize);

		// split main area between preview and some extra controls
		final JSplitPane extraControlsPanel = new JSplitPane(HORIZONTAL_SPLIT);
		extraControlsPanel.setLeftComponent(new ExtraControlsPanel(model));
		extraControlsPanel.setRightComponent(scrollControlsPanel);
		extraControlsPanel.setResizeWeight(1.0);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				extraControlsPanel.setDividerLocation(extraControlsPanel.getWidth()
				        - scrollControlsPanel.getPreferredSize().width);
			}
		});

		// add tabbed pane to switch between desktop and plain background
		desktopSwitcher = createDesktopSwitcher();
		desktopSwitcher.setSelectedIndex(1);
		desktopSwitcher.setComponentAt(TAB_DESKTOP, desktopScroller);
		desktopSwitcher.setComponentAt(TAB_CONTROLS, extraControlsPanel);

		getContentPane().remove(desktopScroller);
		getContentPane().add(desktopSwitcher, BorderLayout.CENTER);

		// update layout
		getContentPane().validate();

		setControlsOpen(true);
	}

	public GLView getView()
	{
		return view;
	}

	private Component createRecorderControls()
	{
		Config config = ConfigManager.getConfig();
		final Recorder recorder = new Recorder(config);

		final JVolumeBar volumeBar = new JVolumeBar();
		volumeBar.setOrientation(JVolumeBar.Orientation.HORIZONTAL);
		volumeBar.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));

		final JLabel timeLabel = new JLabel("-:--'--\"");
		timeLabel.setBorder(BorderFactory.createCompoundBorder(
		        BorderFactory.createBevelBorder(BevelBorder.LOWERED),
		        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
		timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, 14));

		class Listener implements AmplitudeListener, GainListener
		{
			private DecimalFormat twoDigitFormat = new DecimalFormat("00");

			private long startTimeMillis;

			@Override
			public void amplitudeChanged(int channel, double amplitude)
			{
				if (recorder.isStarted())
				{
					volumeBar.setAmplitude(channel, amplitude);

					int seconds = (int) ((System.currentTimeMillis() - startTimeMillis) / 1000);
					int minutes = seconds / 60;
					int hours = minutes / 60;
					minutes %= 60;
					seconds %= 60;
					timeLabel.setText(hours + ":"
					        + twoDigitFormat.format(minutes) + "'"
					        + twoDigitFormat.format(seconds) + '"');
				}
			}

			@Override
			public void gainChanged(int channel, double gain)
			{
				volumeBar.setGain(channel, gain);
			}
		}

		final Listener listener = new Listener();
		recorder.addAmplitudeListener(listener);
		recorder.addGainListener(listener);

		final Action[] recordStopActions = new Action[2];

		Action recordAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setEnabled(false);
				recorder.setConfig(ConfigManager.getConfig());
				try
				{
					recorder.start();

					recordStopActions[1].setEnabled(true);
					listener.startTimeMillis = System.currentTimeMillis();

				}
				catch (Exception ex)
				{
					showExceptionDialog(Resources.get().getString(
					        "recorder.exception.record"), ex);
					setEnabled(true);
				}
			}
		};
		recordAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("custom/Record24.gif"));
		recordStopActions[0] = recordAction;

		Action stopAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				setEnabled(false);
				try
				{
					recorder.stop();

					volumeBar.clear();
					timeLabel.setText("-:--'--\"");
					recordStopActions[0].setEnabled(true);

				}
				catch (Exception ex)
				{
					showExceptionDialog(Resources.get().getString(
					        "recorder.exception.stop"), ex);
					setEnabled(true);
				}
			}
		};
		stopAction.putValue(Action.LARGE_ICON_KEY, getIcon("media/Stop24.gif"));
		stopAction.setEnabled(false);
		recordStopActions[1] = stopAction;

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.add(timeLabel);
		toolBar.addSeparator();
		toolBar.add(volumeBar);
		toolBar.addSeparator();
		toolBar.add(recordAction);
		toolBar.add(stopAction);
		toolBar.setPreferredSize(new Dimension(300,
		        toolBar.getPreferredSize().height));
		toolBar.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		// JPanel panel = new JPanel();
		// panel.setPreferredSize(new Dimension(300,50));
		// panel.add();
		return toolBar;
	}

	/**
	 * Removes components for controlling a lyric view model from the editor.
	 */
	public void closeControls()
	{
		assert isControlsOpen() : "expected controls to be open";

		// controls panel (select, search, playlist)
		getContentPane().remove(controlsPanel);

		// small scrollable preview
		view.dispose();

		// replace desktop switcher with desktop
		if (desktopSwitcher != null)
		{
			getContentPane().remove(desktopSwitcher);
		}
		getContentPane().add(desktopScroller, BorderLayout.CENTER);
		desktopSwitcher = null;

		// update layout
		getContentPane().validate();

		setControlsOpen(false);
	}

	private void setControlsOpen(boolean controlsOpen)
	{
		this.controlsOpen = controlsOpen;
	}

	private boolean isControlsOpen()
	{
		return controlsOpen;
	}

	public void showDesktop()
	{
		if (isControlsOpen())
		{
			desktopSwitcher.setSelectedIndex(TAB_DESKTOP);
		}
		else
		{
			// desktop is inherently visible
		}
	}

	public void showControls()
	{
		if (isControlsOpen())
		{
			desktopSwitcher.setSelectedIndex(TAB_CONTROLS);
		}
		else
		{
			// controls can't be shown
		}
	}

	private JTabbedPane createDesktopSwitcher()
	{
		JPanel panel = new JPanel();
		panel.setBackground(Color.GRAY);

		ResourceUtilities res = Resources.get();

		final JTabbedPane switcher = new JTabbedPane();
		switcher.addTab(res.getString("editor.mode.editing"), null);
		switcher.addTab(res.getString("editor.mode.presentation"), panel);
		switcher.setTabPlacement(SwingConstants.LEFT);

		JLabel desktopHeader = new JLabel(res.getString("editor.mode.editing"),
		        getImageIcon("/images/editor-editing.png"),
		        SwingConstants.CENTER);
		desktopHeader.setToolTipText(res.getString("editor.mode.editing.tip"));
		desktopHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		desktopHeader.setHorizontalTextPosition(SwingConstants.CENTER);
		desktopHeader.setVerticalTextPosition(SwingConstants.BOTTOM);
		desktopHeader.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				switcher.setSelectedIndex(TAB_DESKTOP);
			}
		});
		switcher.setTabComponentAt(TAB_DESKTOP, desktopHeader);

		JLabel controlsHeader = new JLabel(
		        res.getString("editor.mode.presentation"),
		        getImageIcon("/images/editor-presentation.png"),
		        SwingConstants.CENTER);
		controlsHeader.setToolTipText(res.getString("editor.mode.presentation.tip"));
		controlsHeader.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		controlsHeader.setHorizontalTextPosition(SwingConstants.CENTER);
		controlsHeader.setVerticalTextPosition(SwingConstants.BOTTOM);
		controlsHeader.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				switcher.setSelectedIndex(TAB_CONTROLS);
			}
		});
		switcher.setTabComponentAt(TAB_CONTROLS, controlsHeader);

		return switcher;
	}

	/**
	 * Adds the given frame to the editor's desktop pane.
	 *
	 * @param frame the frame to be added
	 */
	public void addToDesktop(final JInternalFrameEx frame)
	{
		// add listener for confirmation of closing the frame
		if (frame instanceof Savable)
		{
			frame.addVetoableChangeListener(this);
		}

		// notify internal frames of deactivation of the EditorFrame
		if (frame instanceof WindowFocusListener)
		{
			addWindowFocusListener((WindowFocusListener) frame);
			frame.addInternalFrameListener(new InternalFrameAdapter()
			{
				@Override
				public void internalFrameClosed(InternalFrameEvent e)
				{
					removeWindowFocusListener((WindowFocusListener) frame);
				}
			});
		}

		if (frame instanceof Editor)
		{
			Editor editor = (Editor) frame;
			editor.setEditorContext(this);
		}

		showDesktop();
		desktop.add(frame, JDesktopPaneEx.DEFAULT);
		frame.getFrameGroup().addInternalFrameListener(new FrameGroupListener());

		// select the frame afterwards
		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				selectInternalFrame(frame);
			}
		});
	}

	/**
	 * Selects the given frame. If for some reason the frame cannot be selected,
	 * this method fails quietly.
	 */
	private void selectInternalFrame(JInternalFrame frame)
	{
		try
		{
			frame.setSelected(true);
		}
		catch (PropertyVetoException e)
		{
			// accept veto
		}
	}

	private boolean confirmClosing()
	{
		try
		{
			JInternalFrame[] frames = desktop.getAllFrames();
			for (int i = 0; i < frames.length; i++)
			{
				frames[i].setClosed(true);
			}

			if (FullScreenDisplay.isStarted())
			{
				int selection = JOptionPane.showConfirmDialog(
				        this,
				        Resources.get().getString("editorFrame.closeFullScreen"),
				        Resources.get().getString("editorFrame.exit"),
				        JOptionPane.YES_NO_CANCEL_OPTION,
				        JOptionPane.QUESTION_MESSAGE);

				if (selection == JOptionPane.YES_OPTION)
				{
					FullScreenDisplay.getInstance().dispose();
				}
				else if (selection == JOptionPane.CANCEL_OPTION)
				{
					// don't exit
					return false;
				}
				else
				{
					// TODO: usability
					/* Ditch the editor, but don't exit don't exit. */
					// isn't that a bit complex for the user?
					return false;
				}
			}

			// close
			return true;
		}
		catch (PropertyVetoException ex)
		{
			// accept veto: don't close
			return false;
		}
	}

	/**
	 * Creates actions used by numerous GUI elements and assigns them to the
	 * appropriate instance variables.
	 */
	private void createActions()
	{
		final ResourceUtilities res = Resources.get();

		newAction = new AbstractAction(res.getString("new"))
		{
			public void actionPerformed(ActionEvent e)
			{
				newFile();
			}
		};
		newAction.putValue(MNEMONIC_KEY, res.getMnemonic("new"));
		newAction.putValue(SMALL_ICON, getIcon("general/New16.gif"));
		newAction.putValue(LARGE_ICON_KEY, getIcon("general/New24.gif"));
		newAction.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_N, InputEvent.CTRL_MASK));
		newAction.putValue(SHORT_DESCRIPTION, res.getString("new.tooltip"));

		openAction = new AbstractAction(res.getString("open"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				openFile();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		openAction.putValue(MNEMONIC_KEY, res.getMnemonic("open"));
		openAction.putValue(SMALL_ICON, getIcon("general/Open16.gif"));
		openAction.putValue(LARGE_ICON_KEY, getIcon("general/Open24.gif"));
		openAction.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_O, InputEvent.CTRL_MASK));
		openAction.putValue(SHORT_DESCRIPTION, res.getString("open.tooltip"));

		saveAction = new AbstractAction(res.getString("save"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				save();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		saveAction.putValue(MNEMONIC_KEY, res.getMnemonic("save"));
		saveAction.putValue(SMALL_ICON, getIcon("general/Save16.gif"));
		saveAction.putValue(LARGE_ICON_KEY, getIcon("general/Save24.gif"));
		saveAction.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_S, InputEvent.CTRL_MASK));
		saveAction.putValue(SHORT_DESCRIPTION, res.getString("save.tooltip"));
		saveAction.setEnabled(false);

		saveAsAction = new AbstractAction(res.getString("saveAs"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				saveAs();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		saveAsAction.putValue(MNEMONIC_KEY, res.getMnemonic("saveAs"));
		saveAsAction.putValue(SMALL_ICON, getIcon("general/SaveAs16.gif"));
		saveAsAction.putValue(LARGE_ICON_KEY, getIcon("general/SaveAs24.gif"));
		saveAsAction.putValue(SHORT_DESCRIPTION,
		        res.getString("saveAs.tooltip"));
		saveAsAction.setEnabled(false);

		importOPSProAction = new AbstractAction(res.getString("opsProFormat"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				importOPSPro();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		importOPSProAction.putValue(MNEMONIC_KEY,
		        res.getMnemonic("opsProFormat"));
		importOPSProAction.setEnabled(true);

		importClassicFormatAction = new AbstractAction(
		        res.getString("classicFormat"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				importClassicFormat();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		importClassicFormatAction.putValue(MNEMONIC_KEY,
		        res.getMnemonic("classicFormat"));
		importClassicFormatAction.setEnabled(true);

		importPlainTextCatalogAction = new AbstractAction(
		        res.getString("plainText"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				importPlainTextCatalog();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		importPlainTextCatalogAction.putValue(MNEMONIC_KEY,
		        res.getMnemonic("plainText"));
		importPlainTextCatalogAction.setEnabled(true);

		exportAction = new AbstractAction(res.getString("export"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				export();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		exportAction.putValue(MNEMONIC_KEY, res.getMnemonic("export"));
		exportAction.putValue(SMALL_ICON, getIcon("general/Export16.gif"));
		exportAction.putValue(LARGE_ICON_KEY, getIcon("general/Export24.gif"));
		exportAction.setEnabled(false);

		preferencesAction = new AbstractAction(res.getString("settings"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				settings();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		preferencesAction.putValue(MNEMONIC_KEY, res.getMnemonic("settings"));
		preferencesAction.putValue(SMALL_ICON,
		        getIcon("general/Preferences16.gif"));
		preferencesAction.putValue(LARGE_ICON_KEY,
		        getIcon("general/Preferences24.gif"));
		preferencesAction.putValue(SHORT_DESCRIPTION,
		        res.getString("settings.tooltip"));

		exitAction = new AbstractAction(res.getString("exit"))
		{
			public void actionPerformed(ActionEvent e)
			{
				if (confirmClosing())
				{
					dispose();
				}
			}
		};
		exitAction.putValue(MNEMONIC_KEY, res.getMnemonic("exit"));

		scrollViewAction = new AbstractAction(res.getString("scrollView"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				FullScreenDisplay.start();
				setCursor(Cursor.getDefaultCursor());
			}
		};
		scrollViewAction.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(
		        KeyEvent.VK_F5, 0));
		scrollViewAction.putValue(MNEMONIC_KEY, res.getMnemonic("scrollView"));
		scrollViewAction.putValue(SMALL_ICON, getIcon("media/Play16.gif"));
		scrollViewAction.putValue(LARGE_ICON_KEY, getIcon("media/Play24.gif"));
		scrollViewAction.putValue(SHORT_DESCRIPTION,
		        res.getString("scrollView.tooltip"));

		findAction = new AbstractAction(res.getString("playlistEditor.find"))
		{
			public void actionPerformed(ActionEvent e)
			{
				JInternalFrame selected = desktop.getSelectedFrame();
				if (selected instanceof PlaylistEditor)
				{
					((PlaylistEditor) selected).find();
				}
				else
				{
					FindLyricDialog dialog = new FindLyricDialog(null, null);
					addToDesktop(dialog);
				}
			}
		};
		findAction.putValue(MNEMONIC_KEY,
		        res.getMnemonic("playlistEditor.find"));
		findAction.putValue(SMALL_ICON, getIcon("general/Find16.gif"));
		findAction.putValue(LARGE_ICON_KEY, getIcon("general/Find24.gif"));
		findAction.putValue(SHORT_DESCRIPTION,
		        res.getString("playlistEditor.find.tip"));
		findAction.setEnabled(true);

		aboutAction = new AbstractAction(res.getString("about"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				AboutDialog dialog = new AboutDialog(EditorFrame.this);
				dialog.setVisible(true);
				setCursor(Cursor.getDefaultCursor());
			}
		};
		aboutAction.putValue(SMALL_ICON, getIcon("general/About16.gif"));
		aboutAction.putValue(LARGE_ICON_KEY, getIcon("general/About24.gif"));
		aboutAction.putValue(MNEMONIC_KEY, res.getMnemonic("about"));
	}

	public ImageIcon getIcon(String name)
	{
		return new ImageIcon(getClass().getResource(
		        "/toolbarButtonGraphics/" + name));
	}

	public ImageIcon getImageIcon(String name)
	{
		return new ImageIcon(getClass().getResource(name));
	}

	private JMenuBar createMenuBar()
	{
		JAlignedMenuBar menuBar = new JAlignedMenuBar();
		JMenu menu, subMenu, subSubMenu;

		menu = new JMenu(Resources.get().getString("file"));
		menu.setMnemonic(Resources.get().getMnemonic("file"));
		menuBar.add(menu);
		menu.add(newAction);
		menu.add(openAction);
		menu.add(saveAction);
		menu.add(saveAsAction);
		menu.addSeparator();
		subMenu = new JMenu(Resources.get().getString("import"));
		subMenu.setMnemonic(Resources.get().getMnemonic("import"));
		subSubMenu = new JMenu(Resources.get().getString("catalog"));
		subSubMenu.setMnemonic(Resources.get().getMnemonic("catalog"));
		subSubMenu.add(importOPSProAction);
		subSubMenu.add(importClassicFormatAction);
		subSubMenu.add(importPlainTextCatalogAction);
		subMenu.add(subSubMenu);
		subMenu.setIcon(new ImageIcon(getClass().getResource(
		        "/toolbarButtonGraphics/general/Import16.gif")));
		menu.add(subMenu);
		menu.add(exportAction);
		menu.addSeparator();
		menu.add(preferencesAction);
		menu.addSeparator();
		for (int i = 0; i < Config.RECENT_FILE_COUNT; i++)
		{
			JMenuItem item = new JMenuItem();
			item.setAction(new RecentFileAction(this, i, item));
			menu.add(item);
		}
		menu.addSeparator();
		menu.add(exitAction);

		menu = new JMenu(Resources.get().getString("view"));
		menu.setMnemonic(Resources.get().getMnemonic("view"));
		menu.add(scrollViewAction);
		menuBar.add(menu);

		menu = new JMenu(Resources.get().getString("help"));
		menu.setMnemonic(Resources.get().getMnemonic("help"));
		menu.add(aboutAction);
		menuBar.add(menu, Integer.MAX_VALUE);

		Config config = ConfigManager.getConfig();
		if (config.isEnabled(Config.DEBUG_MENU))
		{
			menu = new JMenu("Debug Options");
			menu.setMnemonic('D');
			menu.add(new AbstractAction("Force seperate controls")
			{
				public void actionPerformed(ActionEvent e)
				{
					Config config = ConfigManager.getConfig();
					config.setEnabled(Config.FORCE_SEPERATE_CONTROLS, true);
				}
			});
			menu.add(new AbstractAction("Show InternalFrameDisplay")
			{
				public void actionPerformed(ActionEvent e)
				{
					ViewModel model = new ViewModel();

					Object[] bundles = CatalogManager.getCatalog().getBundles().toArray();
					Lyric lyric = ((Bundle) bundles[0]).getLyric(1);
					model.setContent(lyric);

					InternalFrameDisplay smallView = new InternalFrameDisplay();
					smallView.setLyricView(new LyricView(model));

					InternalFrameDisplay smallView2 = new InternalFrameDisplay();
					smallView2.setLyricView(new LyricView(model));

					addToDesktop(smallView);
					addToDesktop(smallView2);
				}
			});
			menu.addSeparator();
			menu.add(new AbstractAction("Open Scroll Controls")
			{
				public void actionPerformed(ActionEvent e)
				{
					final ViewModel model = new ViewModel();
					model.register(null);
					openControls(model);
					addWindowListener(new WindowAdapter()
					{
						@Override
						public void windowClosed(WindowEvent e)
						{
							model.unregister(null);
						}
					});
				}
			});
			menu.add(new AbstractAction("Close Scroll Controls")
			{
				public void actionPerformed(ActionEvent e)
				{
					closeControls();
				}
			});
			menu.addSeparator();
			menu.add(new AbstractAction("Resize to 1024x768")
			{
				public void actionPerformed(ActionEvent e)
				{
					setExtendedState(NORMAL);
					setSize(new Dimension(1024, 768));
				}
			});
			menu.add(new AbstractAction("Resize to 800x600")
			{
				public void actionPerformed(ActionEvent e)
				{
					setExtendedState(NORMAL);
					setSize(new Dimension(800, 600));
				}
			});
			menu.add(new AbstractAction("Resize to 640x480")
			{
				public void actionPerformed(ActionEvent e)
				{
					setExtendedState(NORMAL);
					setSize(new Dimension(640, 480));
				}
			});
			menuBar.add(menu);
		}

		return menuBar;
	}

	/**
	 * Creates the content pane of the editor. Sets desktop and desktopScroller
	 * to the appropriate values.
	 */
	private JPanel createContentPane()
	{
		desktop = new JDesktopPaneEx();
		desktopScroller = new JScrollPane(desktop);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(createToolBar(), BorderLayout.NORTH);
		panel.add(desktopScroller, BorderLayout.CENTER);
		return panel;
	}

	private JToolBar createToolBar()
	{
		JToolBar toolBar = new JToolBar(SwingConstants.HORIZONTAL);
		toolBar.setFloatable(false);
		toolBar.add(newAction);
		toolBar.addSeparator();
		toolBar.add(openAction);
		toolBar.add(saveAction);
		toolBar.add(saveAsAction);
		toolBar.addSeparator();
		toolBar.add(scrollViewAction);
		toolBar.add(findAction);
		toolBar.addSeparator();
		toolBar.add(preferencesAction);
		return toolBar;
	}

	public void vetoableChange(PropertyChangeEvent e)
	        throws PropertyVetoException
	{
		String propertyName = e.getPropertyName();

		// confirm closing of modified Savables
		if (propertyName == JInternalFrame.IS_CLOSED_PROPERTY
		        && Boolean.TRUE.equals(e.getNewValue()))
		{
			if (e.getSource() instanceof Savable)
			{
				Savable savable = (Savable) e.getSource();
				if (savable.isModified())
				{
					String filename = savable.getFile() == null ? Resources.get().getString(
					        "editorFrame.untitled")
					        : savable.getFile().toString();

					String fileInfo;
					if (savable instanceof CatalogEditor)
					{
						fileInfo = Resources.get().getString(
						        "editorFrame.catalogDescription", filename);
					}
					else if (savable instanceof PlaylistEditor)
					{
						fileInfo = Resources.get().getString(
						        "editorFrame.playlistDescription", filename);
					}
					else
					{
						fileInfo = Resources.get().getString(
						        "editorFrame.fileDescription", filename);
					}

					int selection = JOptionPane.showConfirmDialog(this,
					        Resources.get().getString(
					                "editorFrame.saveChanges", fileInfo),
					        getTitle(), JOptionPane.YES_NO_CANCEL_OPTION,
					        JOptionPane.WARNING_MESSAGE);

					if (selection == JOptionPane.YES_OPTION)
					{
						boolean saved = save();
						if (!saved)
						{
							// don't close if save fails
							throw new PropertyVetoException(
							        JInternalFrame.IS_CLOSED_PROPERTY, e);
						}
					}
					else if (selection != JOptionPane.NO_OPTION)
					{
						throw new PropertyVetoException(
						        JInternalFrame.IS_CLOSED_PROPERTY, e);
					}
				}
			}
		}
	}

	private class FrameGroupListener extends InternalFrameAdapter
	{
		/**
		 * Maintains a set of menus added to the main menu bar on behalf of the
		 * currently focused dialog.
		 */
		private JMenu[] frameMenus = null;

		@Override
		public void internalFrameActivated(InternalFrameEvent e)
		{
			// get top-level parent of selected frame
			JInternalFrameEx frame = (JInternalFrameEx) e.getInternalFrame();
			JInternalFrameEx parent = frame;
			for (InternalFrameGroup parentGroup = frame.getParentFrameGroup(); parentGroup != null;)
			{
				parent = parentGroup.getParent();
				parentGroup = parent.getParentFrameGroup();
			}
			activeFrame = parent;

			// add extra menu's
			assert frameMenus == null;
			if (parent instanceof MenuSource)
			{
				MenuSource menuSource = (MenuSource) parent;
				frameMenus = menuSource.getMenus();
			}
			if (frameMenus != null)
			{
				for (int i = 0; i < frameMenus.length; i++)
				{
					getJMenuBar().add(frameMenus[i]);
				}
				setJMenuBar(getJMenuBar());
			}

			// enable file menu items
			if (parent instanceof Savable)
			{
				saveAction.setEnabled(true);
				saveAsAction.setEnabled(true);
			}
			if (parent instanceof Exportable)
			{
				exportAction.setEnabled(true);
			}
		}

		@Override
		public void internalFrameDeactivated(InternalFrameEvent e)
		{
			/*
			 * // get top-level parent of selected frame JInternalFrameEx parent
			 * = frame; for (InternalFrameGroup parentGroup =
			 * frame.getParentFrameGroup(); parentGroup != null;) { parent =
			 * parentGroup.getParent(); parentGroup =
			 * parent.getParentFrameGroup(); }
			 */

			// remove extra menu's
			if (frameMenus != null)
			{
				for (int i = 0; i < frameMenus.length; i++)
				{
					getJMenuBar().remove(frameMenus[i]);
				}
				setJMenuBar(getJMenuBar());
				frameMenus = null;
			}

			// disable file menu items
			// if (parent instanceof Savable) {
			saveAction.setEnabled(false);
			saveAsAction.setEnabled(false);
			// }
			// if (parent instanceof Exportable) {
			exportAction.setEnabled(false);
			// }

			activeFrame = null;
		}
	}

	private class PlainTextImporterListener implements
	        WorkerListener<MutableCatalog>
	{
		public void stateChanged(WorkerEvent<MutableCatalog> e)
		{
			if (e.getType() != WorkerEvent.EventType.FINISHED)
			{
				return;
			}

			MutableCatalog catalog = e.getSource().get();
			if (catalog == null)
			{
				return;
			}

			boolean merge = false;
			if (activeFrame != null && activeFrame instanceof CatalogEditor)
			{
				File catalogFile = ((CatalogEditor) activeFrame).getFile();
				String catalogName = catalogFile == null ? Resources.get().getString(
				        "editorFrame.untitled")
				        : catalogFile.toString();
				int choice = JOptionPane.showConfirmDialog(EditorFrame.this,
				        Resources.get().getString("editorFrame.mergeImported",
				                catalogName), Resources.get().getString(
				                "editorFrame.import"),
				        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				merge = choice == JOptionPane.YES_OPTION;
			}

			if (merge)
			{
				assert activeFrame instanceof CatalogEditor;
				CatalogEditor frame = ((CatalogEditor) activeFrame);
				DefaultCatalog.merge(frame.getCatalog(), catalog);

			}
			else
			{
				addToDesktop(new CatalogEditor(catalog));
			}
		}
	}
}
