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

package lithium.display;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

import com.github.meinders.common.*;
import lithium.*;
import lithium.display.opengl.*;
import lithium.editor.*;

/**
 * The top-level container for the LyricView used in full-screen mode, this
 * class ensures the view is shown correctly and initiates the display of
 * seperate controls.
 *
 * @since 0.1
 * @version 0.9 (2006.04.14)
 * @author Gerrit Meinders
 */
public class FullScreenDisplay extends JFrame implements PropertyChangeListener
{
	private static FullScreenDisplay instance = null;

	/** Starts the full-screen display. */
	public static void start()
	{
		assert SwingUtilities.isEventDispatchThread();

		final Splash splash = Splash.getInstance();

		Config config = ConfigManager.getConfig();

		if (instance == null)
		{
			splash.setStatus(Resources.get().getString(
			        "scrollView.loadingPlaylist"));

			// load playlist
			Playlist playlist = PlaylistManager.getPlaylist();
			if (playlist == null)
			{
				playlist = new Playlist();
			}

			PlaylistManager.setPlaylist(playlist);

			splash.setStatus(Resources.get().getString(
			        "scrollView.initializing"));

			// create lyric view model
			ViewModel model = new ViewModel();
			model.setPlaylist(PlaylistManager.getPlaylist());

			instance = newInstance(config, model);

			// open seperate control
			if (config.isSeperateControlsEnabled() && EditorFrame.isStarted())
			{
				EditorFrame.getInstance().openControls(model);
			}

			instance.requestFocus();
			instance.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		}
		else
		{
			// instance.setVisible(true);
			instance.setExtendedState(MAXIMIZED_BOTH);
			instance.requestFocus();

			// Bug-fix: prevent NPE when the model's scroller has been disposed
			if (instance.lyricView.getModel().isDisposed())
			{
				instance.lyricView.setModel(new ViewModel());
			}
		}

		instance.setVisible(true);
		// instance.invalidate();
		// instance.validate();

		ViewModel model = instance.lyricView.getModel();
		if (config.isSeperateControlsEnabled())
		{
			// NB: model.controlsVisible is already true by default
			// instance.setControlsVisible(false);
		}
		else
		{
			model.setControlsVisible(true);
		}

		instance.lyricView.setCursorVisible(false);
		instance.lyricView.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				if (e.getModifiersEx() == 0)
				{
					focusOnPreview();
				}
			}

			@Override
			public void mouseMoved(MouseEvent e)
			{
				if (e.getModifiersEx() == 0)
				{
					focusOnPreview();
				}
			}

			private void focusOnPreview()
			{
				EditorFrame editor = EditorFrame.getInstance();
				if (editor != null)
				{
					GLView view = editor.getView();
					if (view != null)
					{
						view.requestFocusInWindow();
						try
						{
							Point center = new Point(view.getWidth() / 2,
							        view.getHeight() / 2);
							SwingUtilities.convertPointToScreen(center, view);
							Robot robot = new Robot();
							robot.mouseMove(center.x, center.y);
						}
						catch (AWTException ex)
						{
							ex.printStackTrace();
						}
					}
				}
			}
		});
	}

	public static FullScreenDisplay newInstance(Config config,
	        final ViewModel model)
	{
		/*
		 * Get graphics configuration.
		 */
		DisplayConfig displayConfig = config.getDisplayConfig(DisplayConfig.PRESENTATION_MODE);
		GraphicsConfiguration graphicsConfiguration = displayConfig.getGraphicsConfiguration();
		GraphicsEnvironment graphicsEnvironment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		List<GraphicsDevice> screenDevices = Arrays.asList(graphicsEnvironment.getScreenDevices());
		int screenIndex = screenDevices.indexOf(graphicsConfiguration.getDevice());
		System.out.println("Full screen display on screen " + (screenIndex + 1));

		/*
		 * Create instance on the selected screen. Disable L&F decorations.
		 */
		boolean decorated = JFrame.isDefaultLookAndFeelDecorated();
		JFrame.setDefaultLookAndFeelDecorated(false);
		final FullScreenDisplay result = new FullScreenDisplay(
		        graphicsConfiguration, model);
		result.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		JFrame.setDefaultLookAndFeelDecorated(decorated);

		/*
		 * Register with the model and unregister on closing.
		 */
		model.register(result);
		result.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				model.unregister(result);
			}
		});

		return result;
	}

	/**
	 * Returns the full-screen display instance, or {@code null} if the
	 * full-screen display hasn't been started yet.
	 *
	 * @return the full-screen display, or {@code null}
	 */
	public static FullScreenDisplay getInstance()
	{
		return instance;
	}

	/**
	 * Returns whether the full-screen display has been started.
	 *
	 * @return {@code true} if the full-screen display has been started; {@code
	 *         false} otherwise
	 */
	public static boolean isStarted()
	{
		return instance != null;
	}

	private DisplayMode oldDisplayMode = null;

	private boolean fullScreen = true;

	private Config config;

	private GLView lyricView;

	private ControlsPanel controlsPanel;

	// private JSplitPane controlsSplitPane;

	private static String getWindowTitle(boolean includeVersion)
	{
		ApplicationDescriptor application = Application.getInstance().getDescriptor();
		if (includeVersion)
		{
			return application.getTitle() + " " + application.getVersion();
		}
		else
		{
			return application.getTitle();
		}
	}

	/**
	 * TODO: full-screen exclusive mode Full-screen exclusive mode should be
	 * fixed at some point. Currently it tends to crash.
	 */
	@Deprecated
	private FullScreenDisplay()
	{
		super(getWindowTitle(true));
		if (true)
		{
			throw new AssertionError("not implemented");
		}

		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		// initComponents();
		initListeners();
		initFullScreenExclusive();
		setCursor(Cursor.getDefaultCursor());
	}

	/**
	 * Constructs a new full-screen display using the given graphics
	 * configuration and lyric view model.
	 *
	 * @param gc the graphics configuration
	 * @param model the lyric view model
	 */
	protected FullScreenDisplay(final GraphicsConfiguration gc, ViewModel model)
	{
		super(Resources.get().getString("scrollView.title",
		        getWindowTitle(false)), gc);

		config = ConfigManager.getConfig();
		config.addPropertyChangeListener(this);
		ConfigManager configManager = ConfigManager.getInstance();
		configManager.addPropertyChangeListener(this);

		// setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		setContentPane(createContentPane(model));
		initListeners();

		setUndecorated(true);
		setResizable(false);

		pack(); // 'realize' the frame before maximizing
		setBounds(gc.getBounds());
		setExtendedState(MAXIMIZED_BOTH);

		setIconImage(new ImageIcon(getClass().getResource(
		        "/images/fullScreen32.gif")).getImage());
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getPropertyName() == ViewModel.CONTROLS_VISIBLE_PROPERTY)
		{
			// setControlsVisible(lyricView.getModel().isControlsVisible());

		}
		else if (e.getPropertyName() == Config.DIVIDER_VISIBLE_PROPERTY)
		{
			// setDividerVisible(config.isDividerVisible());

		}
		else if (e.getPropertyName() == ConfigManager.CONFIG_PROPERTY)
		{
			config.removePropertyChangeListener(this);
			config = ConfigManager.getConfig();
			config.addPropertyChangeListener(this);

			// setDividerVisible(config.isDividerVisible());
		}
	}

	public void dispose()
	{
		try
		{
			if (fullScreen)
			{
				// System.out.println("Exiting fullscreen mode.");
				GraphicsDevice display = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
				if (oldDisplayMode != null)
				{
					display.setDisplayMode(oldDisplayMode);
				}
				display.setFullScreenWindow(null);
			}

		}
		catch (Exception e)
		{
			e.printStackTrace();

		}
		finally
		{
			super.dispose();
			ConfigManager configManager = ConfigManager.getInstance();
			configManager.removePropertyChangeListener(this);
			lyricView.dispose();
			instance = null;
		}
	}

	private void initListeners()
	{
		// exit fullscreen mode when window closes
		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				dispose();
			}

			public void windowClosed(WindowEvent e)
			{
				if (config.isSeperateControlsEnabled()
				        && EditorFrame.isStarted())
				{
					EditorFrame.getInstance().closeControls();
				}
				// System.exit(0);
			}
		});
	}

	private JPanel createContentPane(ViewModel model)
	{
		model.addPropertyChangeListener(this);

		lyricView = new GLView(model);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				lyricView.dispose();
			}
		});

		// controlsPanel = new ControlsPanel(model);
		// controlsSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		// controlsSplitPane.setBorder(BorderFactory.createEmptyBorder());
		// controlsSplitPane.setOpaque(false);
		// controlsSplitPane.setOneTouchExpandable(true);
		// controlsSplitPane.setResizeWeight(1.0);
		// controlsSplitPane.setTopComponent(lyricView);
		// controlsSplitPane.setBottomComponent(controlsPanel);
		// controlsSplitPane.setAutoscrolls(false);
		// setDividerVisible(config.isDividerVisible());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		// panel.add(controlsSplitPane, BorderLayout.CENTER);
		panel.add(lyricView);

		if (!EditorFrame.isStarted())
		{
			Action showControlsAction = new AbstractAction("showControls")
			{
				public void actionPerformed(ActionEvent e)
				{
					ViewModel model = lyricView.getModel();
					model.setControlsVisible(!model.isControlsVisible());
				}
			};
			panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
			        KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			        showControlsAction.getValue(AbstractAction.NAME));
			panel.getActionMap().put(
			        showControlsAction.getValue(AbstractAction.NAME),
			        showControlsAction);
		}

		return panel;
	}

	private void initFullScreenExclusive()
	{
		// get display device
		DisplayConfig displayConfig = config.getDisplayConfig("presentation");
		GraphicsDevice display;
		if (displayConfig.isDeviceSet())
		{
			display = displayConfig.getDevice();

		}
		else
		{
			GraphicsEnvironment graphEnv = GraphicsEnvironment.getLocalGraphicsEnvironment();
			display = graphEnv.getDefaultScreenDevice();
		}
		System.out.println("Display device: " + display.toString());

		// check fullscreen support
		boolean fullScreenSupported = display.isFullScreenSupported();
		fullScreen &= fullScreenSupported;
		if (fullScreen)
		{
			if (fullScreenSupported)
			{
				System.out.println("Fullscreen is supported.");
			}
			else
			{
				System.out.println("Fullscreen is not supported.");
			}
		}

		// enter full screen mode
		if (fullScreen && fullScreenSupported)
		{
			// enter full-screen mode
			setUndecorated(true);
			display.setFullScreenWindow(this);

			if (display.isDisplayChangeSupported())
			{
				System.out.println("Display mode change is supported.");

				if (displayConfig.isDisplayModeSet())
				{
					// get display mode
					DisplayMode newDisplayMode = displayConfig.getDisplayMode();
					System.out.println("Using display mode: "
					        + newDisplayMode.getWidth() + "x"
					        + newDisplayMode.getHeight() + "x"
					        + newDisplayMode.getBitDepth() + " ("
					        + newDisplayMode.getRefreshRate() + " Hz)");

					// change display mode
					oldDisplayMode = display.getDisplayMode();
					display.setDisplayMode(newDisplayMode);

				}
				else
				{
					System.out.println("Using current display mode.");
				}

			}
			else
			{
				System.out.println("Display mode change is not supported.");
			}

		}
		else
		{
			System.out.println("Running windowed.");

			// configure window
			setExtendedState(MAXIMIZED_BOTH);
		}
	}

	public GLView getView()
	{
		return lyricView;
	}
}
