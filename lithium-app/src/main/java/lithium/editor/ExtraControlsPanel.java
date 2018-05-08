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
import java.util.List;
import java.util.concurrent.*;
import javax.swing.*;
import javax.swing.filechooser.*;

import com.github.meinders.common.*;
import com.github.meinders.common.FileFilter;
import com.github.meinders.common.swing.*;
import lithium.*;
import lithium.Config.*;
import lithium.display.*;
import lithium.gui.*;
import lithium.powerpoint.*;

import static javax.swing.SwingConstants.*;

/**
 * This class provides additional controls to fill up some space while providing
 * useful functions to the user.
 *
 * @author Gerrit Meinders
 */
public class ExtraControlsPanel extends JPanel
{
	private ViewModel model;

	private JTextArea announcementText;

	private Action showAnnouncementAction;

	private Action clearAnnouncementAction;

	private Action openPowerPointAction;

	private AbstractAction playMediaFileAction;

	private AbstractAction playDVDAction;

	/**
	 * Constructs a new panel displaying controls for the specified model.
	 *
	 * @param model the lyric view model to be controlled
	 */
	public ExtraControlsPanel(ViewModel model)
	{
		super();
		this.model = model;
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		setLayout(new BorderLayout());
		createActions();
		add(createComponents(), BorderLayout.CENTER);
	}

	private void openPowerPoint()
	{
		Config config = ConfigManager.getConfig();
		File pptViewer = config.getPPTViewer();
		File odpViewer = config.getUtility(Config.UTILITY_OPEN_OFFICE);

		List<FileFilter> availableFilters = new ArrayList<FileFilter>();

		if ((pptViewer != null) && pptViewer.exists())
		{
			availableFilters.addAll(Arrays.asList(FilterManager.getFilters( FilterType.PPT)));
		}

		if ((pptViewer != null) && pptViewer.exists())
		{
			availableFilters.addAll(Arrays.asList(FilterManager.getFilters( FilterType.ODP)));
		}

		if (availableFilters.isEmpty())
		{
			ApplicationDescriptor application = Application.getInstance().getDescriptor();
			JOptionPane.showMessageDialog(this, Resources.get().getString(
			        "missingPPTViewer"), application.getTitle(),
			        JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		CombinedFileFilter presentationFilter = new CombinedFileFilter(
		        Resources.get().getString("presentation.any"),
		        CombinedFileFilter.Method.UNION, availableFilters);
		availableFilters.add(0, presentationFilter);

		// select presentation
		File presentation;
		JFileChooser chooser = FileChoosers.createFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		for (FileFilter filter : availableFilters)
		{
			chooser.addChoosableFileFilter(filter);
		}
		chooser.setFileFilter(presentationFilter);

		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			presentation = chooser.getSelectedFile();
		}
		else
		{
			return;
		}

		GraphicsDevice device = config.getDisplayConfig(
		        DisplayConfig.PRESENTATION_MODE).getDevice();

		File executable = pptViewer;
		if (executable == null
		        || presentation.getName().toLowerCase().endsWith(".odp"))
		{
			executable = odpViewer;
		}

		final PPTViewer viewer;
		final PPTViewerType type = config.getPPTViewerType();
		switch (type)
		{
		case POWERPOINT:
			viewer = new PowerPoint(executable);
			break;
		case POWERPOINT_VIEWER:
			viewer = new PowerPointViewer(executable, device);
			break;
		case OPEN_OFFICE_2:
			viewer = new OpenOfficeImpress(executable);
			break;
		default:
			throw new AssertionError("unexpected PPTViewerType: " + type);
		}

		// disable autoscrolling and plugins
		model.setAutoScrollingEnabled(false);
		clearAnnouncement();

		// create any viewer frames without use of L&F decorations
		boolean decorated = JFrame.isDefaultLookAndFeelDecorated();
		JFrame.setDefaultLookAndFeelDecorated(false);
		try
		{
			viewer.startViewer(presentation);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		JFrame.setDefaultLookAndFeelDecorated(decorated);

		disableEditorWhile(new Callable<Boolean>()
		{
			public Boolean call() throws Exception
			{
				return viewer.isActive();
			}
		}, new Runnable()
		{
			public void run()
			{
				viewer.requestFocus();
			}
		}, new Runnable()
		{
			public void run()
			{
				viewer.stop();
			}
		}, "PowerPoint presentatie");
	}

	/**
	 * Disables the editor while the given condition holds, showing the given
	 * message. Afterwards, the editor is re-enabled. This method returns (more
	 * or less) immediately.
	 * <p>
	 * A monitor thread is run to re-evaluate the condition periodically. While
	 * waiting, the given wait action is run after checking the condition.
	 */
	private void disableEditorWhile(final Callable<Boolean> condition,
	        final Runnable waitAction, final Runnable cancelAction,
	        final String message)
	{
		Container topLevelAncestor = getTopLevelAncestor();
		if (topLevelAncestor instanceof EditorFrame)
		{
			final EditorFrame editor = (EditorFrame) topLevelAncestor;
			Thread monitor = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						for (int i = 0; !condition.call() && i < 20; i++)
						{
							try
							{
								Thread.sleep(500L);
							}
							catch (InterruptedException e)
							{
								// ignore
							}
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}

					try
					{
						if (condition.call())
						{
							LayoutManager layout = getLayout();
							setLayout(null);

							JLabel notice = new JLabel(
							        "<html><h1>"
							                + message
							                + "</h1><p>(Lithium is tijdelijk uitgeschakeld)</p></html>");
							notice.setOpaque(true);
							notice.setHorizontalAlignment(SwingConstants.CENTER);

							JButton button = new JButton(new AbstractAction(
							        "Presentatie stoppen")
							{
								@Override
								public void actionPerformed(ActionEvent e)
								{
									cancelAction.run();
								}
							});

							JPanel panel = new JPanel();
							panel.setLayout(new BorderLayout());
							panel.add(notice, BorderLayout.CENTER);
							panel.setBounds(0, 0, getWidth(), getHeight());

							add(panel);
							setComponentZOrder(panel, 0);

							editor.setEnabled(false);
							System.out.println(" - Editor disabled.");
							System.out.println(" - Monitoring condition...");
							while (condition.call())
							{
								waitAction.run();
								try
								{
									Thread.sleep(500L);
								}
								catch (InterruptedException e)
								{
									// ignore
								}
							}

							remove(panel);
							setLayout(layout);
							revalidate();
							repaint();

							System.out.println(" - Editor re-enabled.");
							editor.setEnabled(true);
						}
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			});
			monitor.start();
		}
	}

	private void playMediaFile()
	{
		Config config = ConfigManager.getConfig();
		File mediaPlayer;

		mediaPlayer = config.getUtility(Config.UTILITY_MEDIA_PLAYER_CLASSIC);
		if ((mediaPlayer != null) && mediaPlayer.exists())
		{
			File mediaFile = chooseMediaFile();
			if (mediaFile != null)
			{
				playMediaFileMPC(config, mediaPlayer, mediaFile);
			}
			return;
		}

		mediaPlayer = config.getUtility(Config.UTILITY_VLC);
		if ((mediaPlayer != null) && mediaPlayer.exists())
		{
			File mediaFile = chooseMediaFile();
			if (mediaFile != null)
			{
				playMediaFileVLC(config, mediaPlayer, mediaFile);
			}
			return;
		}

		ApplicationDescriptor application = Application.getInstance().getDescriptor();
		JOptionPane.showMessageDialog(this, Resources.get().getString(
		        "media.noPlayer"), application.getTitle(),
		        JOptionPane.INFORMATION_MESSAGE);
	}

	private void playMediaFileMPC(Config config, File mediaPlayer,
	        File mediaFile)
	{
		GraphicsDevice device = config.getDisplayConfig(
		        DisplayConfig.PRESENTATION_MODE).getDevice();
		final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final List<GraphicsDevice> devices = Arrays.asList(environment.getScreenDevices());
		final int deviceIndex = devices.indexOf(device);
		String display = String.valueOf(deviceIndex + 1);

		String[] command = { mediaPlayer.toString(), mediaFile.toString(),
		        "/play", "/close", "/fullscreen", "/monitor", display };
		try
		{
			Runtime.getRuntime().exec(command);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void playMediaFileVLC(Config config, File mediaPlayer,
	        File mediaFile)
	{
		GraphicsDevice device = config.getDisplayConfig(
		        DisplayConfig.PRESENTATION_MODE).getDevice();
		GraphicsConfiguration defaultConfiguration = device.getDefaultConfiguration();
		Rectangle bounds = defaultConfiguration.getBounds();

		String[] command = { mediaPlayer.toString(), //
		        "--width", String.valueOf(bounds.width), //
		        "--height", String.valueOf(bounds.height), //
		        "--video-x", String.valueOf(bounds.x),//
		        "--video-y", String.valueOf(bounds.y), //
		        "--fullscreen", mediaFile.toString() };

		try
		{
			Runtime.getRuntime().exec(command);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private File chooseMediaFile()
	{
		File mediaFile = null;
		JFileChooser chooser = FileChoosers.createFileChooser();
		chooser.setAcceptAllFileFilterUsed(true);
		for (FileFilter filter : FilterManager.getFilters( FilterType.MEDIA_FILES))
		{
			chooser.addChoosableFileFilter(filter);
		}
		int returnVal = chooser.showOpenDialog(this);
		if (returnVal == JFileChooser.APPROVE_OPTION)
		{
			mediaFile = chooser.getSelectedFile();
		}
		return mediaFile;
	}

	private void playMediaFileVLC(Config config, File mediaPlayer)
	{
		File mediaFile = chooseMediaFile();

		GraphicsDevice device = config.getDisplayConfig(
		        DisplayConfig.PRESENTATION_MODE).getDevice();
		final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
		final List<GraphicsDevice> devices = Arrays.asList(environment.getScreenDevices());
		final int deviceIndex = devices.indexOf(device);
		String display = String.valueOf(deviceIndex + 1);

		String[] command = { mediaPlayer.toString(), mediaFile.toString(),
		        "/play", "/close", "/fullscreen", "/monitor", display };
		try
		{
			Runtime.getRuntime().exec(command);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void playDVD()
	{
		Config config = ConfigManager.getConfig();
		final File mediaPlayer = config.getUtility("mediaPlayerClassic");

		if (mediaPlayer == null || !mediaPlayer.exists())
		{
			ApplicationDescriptor application = Application.getInstance().getDescriptor();
			JOptionPane.showMessageDialog(this, Resources.get().getString(
			        "media.noPlayer"), application.getTitle(),
			        JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		final IconListChooser<File> dvdChooser = new IconListChooser<File>(
		        Resources.get().getString("media.playDVD"));
		final FileSystemView fileSystemView = FileSystemView.getFileSystemView();
		for (File root : File.listRoots())
		{
			dvdChooser.add(root, fileSystemView.getSystemIcon(root),
			        fileSystemView.getSystemDisplayName(root));
		}

		final File selection = dvdChooser.showSelectionDialog(getParentFrame());
		if (selection != null)
		{
			System.out.println(selection);

			GraphicsDevice device = config.getDisplayConfig(
			        DisplayConfig.PRESENTATION_MODE).getDevice();
			final GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
			final List<GraphicsDevice> devices = Arrays.asList(environment.getScreenDevices());
			final int deviceIndex = devices.indexOf(device);
			String display = String.valueOf(deviceIndex + 1);

			String[] command = { mediaPlayer.toString(), "/dvd",
			        selection.toString(), "/play", "/close", "/fullscreen",
			        "/monitor", display };
			try
			{
				Runtime.getRuntime().exec(command);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	private void showAnnouncement(String text)
	{
		clearAnnouncement();
		GUIPluginManager pluginManager = model.getGUIPluginManager();
		Marquee marquee = new Marquee(text);
		marquee.setRate(0.1f);
		marquee.setForeground(new Color(0xffff80));
		marquee.setShadow(Color.BLACK);
		pluginManager.addPlugin(marquee);
	}

	private void clearAnnouncement()
	{
		GUIPluginManager pluginManager = model.getGUIPluginManager();
		Set<GUIPlugin> plugins = new LinkedHashSet<GUIPlugin>(
		        pluginManager.getPlugins());
		for (GUIPlugin plugin : plugins)
		{
			if (plugin instanceof Marquee)
			{
				pluginManager.removePlugin(plugin);
			}
		}
	}

	private void createActions()
	{
		showAnnouncementAction = new AbstractAction(Resources.get().getString(
		        "showAnnouncement"))
		{
			public void actionPerformed(ActionEvent e)
			{
				showAnnouncement(announcementText.getText());
			}
		};

		clearAnnouncementAction = new AbstractAction(Resources.get().getString(
		        "clearAnnouncement"))
		{
			public void actionPerformed(ActionEvent e)
			{
				clearAnnouncement();
			}
		};

		openPowerPointAction = new AbstractAction(Resources.get().getString(
		        "openPPT"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				openPowerPoint();
				setCursor(Cursor.getDefaultCursor());
			}
		};

		playMediaFileAction = new AbstractAction(Resources.get().getString(
		        "media.playMediaFile"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				playMediaFile();
				setCursor(Cursor.getDefaultCursor());
			}
		};

		playDVDAction = new AbstractAction(Resources.get().getString(
		        "media.playDVD"))
		{
			public void actionPerformed(ActionEvent e)
			{
				setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
				playDVD();
				setCursor(Cursor.getDefaultCursor());
			}
		};
	}

	private JComponent createComponents()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(createAnnouncementPresetList());
		panel.add(Box.createRigidArea(new Dimension(5, 5)));
		panel.add(createAnnouncementComponents());
		panel.add(createStrongGlue());
		panel.add(createMiscellaneousButtons());
		return panel;
	}

	private Component createMiscellaneousButtons()
	{
		JPanel panel = new JPanel();
		panel.setLayout(new ProperFlowLayout(FlowLayout.CENTER, 5, 5));
		panel.add(new JButton(openPowerPointAction));
		panel.add(new JButton(playMediaFileAction));
		panel.add(new JButton(playDVDAction));
		return panel;
	}

	private Component createStrongGlue()
	{
		Component strongGlue = Box.createGlue();
		strongGlue.setMaximumSize(new Dimension(Integer.MAX_VALUE,
		        Integer.MAX_VALUE));
		return strongGlue;
	}

	private JComponent createAnnouncementComponents()
	{
		String labelText = Resources.get().getString("label",
		        Resources.get().getString("announcement"));

		JLabel label = new JLabel(labelText, LEFT);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(createAnnouncementArea(), BorderLayout.CENTER);
		panel.add(createAnnouncementButtons(), BorderLayout.EAST);
		panel.setMaximumSize(new Dimension(32767, 32767));
		return panel;
	}

	private JComponent createAnnouncementArea()
	{
		JTextArea textArea = new JTextArea(Resources.get().getString(
		        "announcementPlaceholder"));
		textArea.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		announcementText = textArea;

		JScrollPane announcementScroller = new JScrollPane(textArea,
		        JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
		        JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		announcementScroller.setPreferredSize(announcementScroller.getMinimumSize());
		announcementScroller.setAlignmentX(LEFT_ALIGNMENT);

		return announcementScroller;
	}

	private JComponent createAnnouncementButtons()
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new GridLayout(2, 1, 5, 5));
		panel.add(new JButton(showAnnouncementAction));
		panel.add(new JButton(clearAnnouncementAction));
		return panel;
	}

	private JComponent createAnnouncementPresetList()
	{
		String labelText = Resources.get().getString("label",
		        Resources.get().getString("announcementPresets"));

		JLabel label = new JLabel(labelText);
		label.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));

		final AnnouncementComboBoxModel model = new AnnouncementComboBoxModel();
		final JComboBox combo = new JComboBox(model);
		combo.setRenderer(new AnnouncementCellRenderer());
		combo.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == ItemEvent.SELECTED)
				{
					Announcement announcement = (Announcement) e.getItem();
					if (announcement == null)
					{
						return;
					}
					JFrame parent = getParentFrame();
					try
					{
						String text = announcement.generateText(parent);
						announcementText.setText(text);
					}
					catch (NullPointerException ex)
					{
						// an input dialog shown by the parameter was cancelled
						// XXX: maybe this would be nicer with custom exception
					}
					model.setSelectedItem(null);
				}
			}
		});

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(label, BorderLayout.NORTH);
		panel.add(combo, BorderLayout.CENTER);
		panel.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
		return panel;
	}

	private JFrame getParentFrame()
	{
		return (JFrame) getTopLevelAncestor();
	}
}
