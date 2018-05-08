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

package lithium;

import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.*;
import java.text.*;
import java.util.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.event.*;

import com.github.meinders.common.*;
import com.github.meinders.common.swing.*;
import lithium.audio.*;
import lithium.catalog.*;
import lithium.display.*;
import lithium.display.opengl.*;
import lithium.editor.*;
import lithium.gui.*;
import lithium.io.*;
import lithium.remote.server.*;

import static javax.swing.Action.*;

/**
 * A user interface consisting of a single tab-less window, providing access to
 * all essential presentation-related features.
 *
 * @author Gerrit Meinders
 */
public class CompactPresentationUI extends JFrame
{
	public static void start(Config config, ViewModel model)
	{
		final FullScreenDisplay display = FullScreenDisplay.newInstance(config,
		        model);
		display.setVisible(true);

		final CompactPresentationUI compactEditor = new CompactPresentationUI(
		        config, model);
		compactEditor.setVisible(true);
		compactEditor.addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				display.dispose();
			}
		});

		/*
		 * Hide cursor over the presentation view.
		 */
		GLView view = display.getView();
		view.setCursorVisible(false);

		/*
		 * Prevent the mouse from entering the presentation view window (unless
		 * a modifier key is pressed.)
		 */
		CursorPositionListener cursorListener = new CursorPositionListener(
		        display, compactEditor);
		view.addMouseListener(cursorListener);
		view.addMouseMotionListener(cursorListener);

		if (config.isEnabled("debugRemoteControl"))
		{
			/*
			 * Allow incoming connections from remote control clients.
			 */
			final Server remoteServer = new Server(7171,
			        new RemoteConnectionHandlerFactory(model,
			                compactEditor.getRecorder()));
			final Thread remoteServerThread = new Thread(remoteServer);
			remoteServerThread.start();

			compactEditor.addWindowListener(new WindowAdapter()
			{
				@Override
				public void windowClosed(WindowEvent e)
				{
					remoteServer.close();
					remoteServerThread.interrupt();
				}
			});
		}
	}

	private GLView preview;

	private Recorder recorder;

	public CompactPresentationUI(final Config config, final ViewModel model)
	{
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		final Recorder recorder = new Recorder(config);
		this.recorder = recorder;

		/*
		 * Register with the model and unregister on closing.
		 */
		model.register(this);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				model.unregister(CompactPresentationUI.this);
			}
		});

		/*
		 * Set application title.
		 */
		Application application = Application.getInstance();
		ApplicationDescriptor descriptor = application.getDescriptor();
		setTitle(descriptor.getTitle());

		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		setLayout(new BorderLayout());
		add(createPreviewPanel(model), BorderLayout.EAST);
		add(createPlaylistPanel(model), BorderLayout.CENTER);

		/*
		 * Set size and center on screen.
		 */
		setMinimumSize(new Dimension(400, 400));
		setPreferredSize(new Dimension(1024, 768));
		pack();
		GraphicsConfiguration graphicsConfiguration = getGraphicsConfiguration();
		Rectangle bounds = graphicsConfiguration.getBounds();
		setLocation((int) bounds.getCenterX() - getWidth() / 2,
		        (int) bounds.getCenterY() - getHeight() / 2);
		setExtendedState(MAXIMIZED_BOTH);
	}

	public GLView getPreview()
	{
		return preview;
	}

	public Recorder getRecorder()
	{
		return recorder;
	}

	private Component createPreviewPanel(final ViewModel model)
	{
		/*
		 * Create preview component.
		 */
		final GLView preview = new GLView(model, true);
		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosed(WindowEvent e)
			{
				preview.dispose();
			}
		});
		this.preview = preview;

		/*
		 * Create buttons to toggle various view features.
		 */
		Collection<Action> toggleActions = new ArrayList<Action>();

		// Toggle content
		final Action toggleContentAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.setContentVisible((Boolean) getValue(Action.SELECTED_KEY));
			}
		};
		model.addPropertyChangeListener(ViewModel.CONTENT_VISIBLE_PROPERTY,
		        new SelectedStateUpdater(toggleContentAction));
		toggleContentAction.putValue(Action.NAME, "Content");
		toggleContentAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("images/toggle-content.png"));
		toggleContentAction.putValue(Action.SELECTED_KEY,
		        model.isContentVisible());
		toggleContentAction.putValue(Action.ACCELERATOR_KEY,
		        KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0));
		toggleActions.add(toggleContentAction);

		// Toggle background
		Action toggleBackgroundAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.setBackgroundVisible((Boolean) this.getValue(Action.SELECTED_KEY));
			}
		};
		model.addPropertyChangeListener(ViewModel.BACKGROUND_VISIBLE_PROPERTY,
		        new SelectedStateUpdater(toggleBackgroundAction));
		toggleBackgroundAction.putValue(Action.NAME, "Background");
		toggleBackgroundAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("images/toggle-background.png"));
		toggleBackgroundAction.putValue(Action.SELECTED_KEY,
		        model.isBackgroundVisible());
		toggleBackgroundAction.putValue(Action.ACCELERATOR_KEY,
		        KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0));
		toggleActions.add(toggleBackgroundAction);

		// Toggle smooth scrolling
		Action toggleAutoScrollingAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.setAutoScrollingEnabled((Boolean) this.getValue(Action.SELECTED_KEY));
			}
		};
		model.addPropertyChangeListener(
		        ViewModel.AUTO_SCROLLING_ENABLED_PROPERTY,
		        new SelectedStateUpdater(toggleAutoScrollingAction));
		toggleAutoScrollingAction.putValue(Action.NAME, "Auto-scrolling");
		toggleAutoScrollingAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("images/auto-scrolling.png"));
		toggleAutoScrollingAction.putValue(Action.SELECTED_KEY,
		        model.isAutoScrollingEnabled());
		toggleActions.add(toggleAutoScrollingAction);

		// Toggle separated scrolling
		Action toggleSeparatedScrollingAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				preview.setSeparatedScrolling((Boolean) this.getValue(Action.SELECTED_KEY));
			}
		};
		model.addPropertyChangeListener(GLView.SEPARATED_SCROLLING_PROPERTY,
		        new SelectedStateUpdater(toggleSeparatedScrollingAction));
		toggleSeparatedScrollingAction.putValue(Action.NAME,
		        "Separated scrolling");
		toggleSeparatedScrollingAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("images/separated-scrolling.png"));
		toggleSeparatedScrollingAction.putValue(Action.SELECTED_KEY,
		        preview.isSeparatedScrolling());
		toggleActions.add(toggleSeparatedScrollingAction);

		// Toggle lock content
		Action toggleContentLockedAction = new AbstractAction()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				model.setContentLocked((Boolean) this.getValue(Action.SELECTED_KEY));
			}
		};
		model.addPropertyChangeListener(ViewModel.CONTENT_LOCKED_PROPERTY,
		        new SelectedStateUpdater(toggleContentLockedAction));
		toggleContentLockedAction.putValue(Action.NAME, "Content locked");
		toggleContentLockedAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("images/content-locked.png"));
		toggleContentLockedAction.putValue(Action.SELECTED_KEY,
		        preview.isSeparatedScrolling());
		toggleActions.add(toggleContentLockedAction);

		/*
		 * Layout components.
		 */
		// Toggle buttons on toolbar.
		JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
		toolBar.setFloatable(false);
		for (Action action : toggleActions)
		{
			final JToggleButton button = new JToggleButton(action);
			button.setFocusPainted(false);
			button.setHideActionText(true);

			// Set tool tip text.
			String toolTip = (String) action.getValue(Action.SHORT_DESCRIPTION);
			if (toolTip == null)
			{
				toolTip = (String) action.getValue(Action.NAME);
			}
			button.setToolTipText(toolTip);
			toolBar.add(button);

			/*
			 * WORKAROUND: 'SELECTED_KEY' isn't updated if the button isn't
			 * clicked. Instead of calling the action directly, call the
			 * button's 'doClick' here to update the action as well.
			 */
			KeyStroke keyStroke = (KeyStroke) action.getValue(Action.ACCELERATOR_KEY);
			if (keyStroke != null)
			{
				InputMap inputMap = toolBar.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
				ActionMap actionMap = toolBar.getActionMap();

				inputMap.put(keyStroke, action.getValue(Action.NAME));
				actionMap.put(action.getValue(Action.NAME),
				        new AbstractAction()
				        {
					        @Override
					        public void actionPerformed(ActionEvent e)
					        {
						        button.doClick();
					        }
				        });
			}
		}

		// Volume-meter on toolbar.
		toolBar.add(Box.createVerticalGlue());
		toolBar.add(createRecorderControls());

		// Toolbar to the right of the preview.
		JPanel previewPanel = new JPanel();
		previewPanel.setLayout(new BorderLayout());
		previewPanel.add(toolBar, BorderLayout.EAST);
		previewPanel.add(preview, BorderLayout.WEST);
		return previewPanel;
	}

	private Component createRecorderControls()
	{
		final JVolumeBar volumeBar = new JVolumeBar();
		volumeBar.setAlignmentX(CENTER_ALIGNMENT);
		volumeBar.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));

		final JLabel timeLabel = new JLabel("-:--'--\"");
		timeLabel.setAlignmentX(CENTER_ALIGNMENT);
		timeLabel.setBorder(BorderFactory.createEmptyBorder());
		int fontSize = 8 * getToolkit().getScreenResolution() / 72;
		timeLabel.setFont(new Font(Font.MONOSPACED, Font.BOLD, fontSize));

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
					try
					{
						recorder.stop();
					}
					catch (IOException e1)
					{
						e1.printStackTrace();
					}
					showExceptionDialog(Resources.get().getString(
					        "recorder.exception.record"), ex);
					setEnabled(true);
				}
			}
		};
		recordAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("toolbarButtonGraphics/custom/Record24.gif"));
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
		stopAction.putValue(Action.LARGE_ICON_KEY,
		        getIcon("toolbarButtonGraphics/media/Stop24.gif"));
		stopAction.setEnabled(false);
		recordStopActions[1] = stopAction;

		JButton recordButton = new JButton(recordAction);
		JButton stopButton = new JButton(stopAction);
		recordButton.setAlignmentX(CENTER_ALIGNMENT);
		stopButton.setAlignmentX(CENTER_ALIGNMENT);

		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentX(LEFT_ALIGNMENT);
		toolBar.setFloatable(false);
		toolBar.setOrientation(SwingConstants.VERTICAL);
		toolBar.add(volumeBar);
		toolBar.add(timeLabel);
		toolBar.addSeparator();
		toolBar.add(recordButton);
		toolBar.add(stopButton);
		toolBar.addSeparator();
		toolBar.setPreferredSize(new Dimension(
		        toolBar.getPreferredSize().width, 300));
		toolBar.setBorder(BorderFactory.createEmptyBorder());

		return toolBar;
	}

	private Component createPlaylistPanel(final ViewModel model)
	{
		ResourceUtilities resources = Resources.get("playlist");

		Action addTextAction = new AbstractAction(
		        resources.getString("addText"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				ResourceUtilities resources = Resources.get();

				JFileChooser fileChooser = FileChoosers.createFileChooser();
				fileChooser.setSelectedFiles(new File[0]);
				fileChooser.setMultiSelectionEnabled(true);
				fileChooser.setAcceptAllFileFilterUsed(true);
				fileChooser.resetChoosableFileFilters();
				fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
				        resources.getString("media.plainText"), "txt"));
				fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
				        resources.getString("media.richText"), "rtf"));
				fileChooser.addChoosableFileFilter(new ExtensionFileFilter(
				        resources.getString("media.wordText"), "doc"));
				fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
				Dimension preferredSize = new Dimension(600, 500);
				fileChooser.setPreferredSize(preferredSize);

				Component parent = CompactPresentationUI.this;
				fileChooser.setLocation(parent.getX()
				        + (parent.getWidth() - preferredSize.width) / 2,
				        parent.getY()
				                + (parent.getHeight() - preferredSize.height)
				                / 2);

				int result = fileChooser.showOpenDialog(CompactPresentationUI.this);

				if (result == JFileChooser.APPROVE_OPTION)
				{
					Playlist playlist = model.getPlaylist();
					for (File file : fileChooser.getSelectedFiles())
					{
						try
						{
							Object content = TextInput.read(file);
							System.out.println(content + " <- content");
							playlist.add(new PlaylistItem(content));
						}
						catch (Exception ex)
						{
							String title = resources.getString("ioException.title");
							String message = resources.getString(
							        "ioException.message",
							        ex.getLocalizedMessage());
							JOptionPane.showMessageDialog(
							        CompactPresentationUI.this, message, title,
							        JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		};
		addTextAction.putValue(SHORT_DESCRIPTION,
		        resources.getString("addText.tip"));
		// addImagesAction.putValue(SMALL_ICON,
		// getIcon("toolbarButtonGraphics/custom/AddImages16.gif"));
		addTextAction.putValue(LARGE_ICON_KEY,
		        getIcon("toolbarButtonGraphics/general/Add24.gif"));

		Action addImagesAction = new AbstractAction(
		        resources.getString("addImages"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
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

				int result = fileChooser.showOpenDialog(CompactPresentationUI.this);

				if (result == JFileChooser.APPROVE_OPTION)
				{
					Playlist playlist = model.getPlaylist();
					for (File file : fileChooser.getSelectedFiles())
					{
						playlist.add(new PlaylistItem(new ImageRef(file)));
					}
				}
			}
		};
		addImagesAction.putValue(SHORT_DESCRIPTION,
		        resources.getString("addImages.tip"));
		// addImagesAction.putValue(SMALL_ICON,
		// getIcon("toolbarButtonGraphics/custom/AddImages16.gif"));
		addImagesAction.putValue(LARGE_ICON_KEY,
		        getIcon("toolbarButtonGraphics/custom/AddImages24.gif"));

		Action removeAction = new AbstractAction(resources.getString("remove"))
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				Playlist playlist = model.getPlaylist();
				int selectedIndex = playlist.getSelectionModel().getSelectedIndex();
				if (selectedIndex != -1)
				{
					playlist.remove(selectedIndex);
				}
			}
		};
		removeAction.putValue(SHORT_DESCRIPTION,
		        resources.getString("addImages.tip"));
		removeAction.putValue(SMALL_ICON,
		        getIcon("toolbarButtonGraphics/general/Delete16.gif"));
		removeAction.putValue(LARGE_ICON_KEY,
		        getIcon("toolbarButtonGraphics/general/Delete24.gif"));

		JButton addTextButton = new JButton(addTextAction);
		addTextButton.setHideActionText(true);
		addTextButton.setMargin(new Insets(2, 2, 2, 2));

		JButton addImagesButton = new JButton(addImagesAction);
		addImagesButton.setHideActionText(true);
		addImagesButton.setMargin(new Insets(2, 2, 2, 2));

		JButton removeButton = new JButton(removeAction);
		removeButton.setHideActionText(true);
		removeButton.setMargin(new Insets(2, 2, 2, 2));

		Component quickAdd = createQuickAddField(model);

		JPanel addContentPanel = new JPanel();
		addContentPanel.setLayout(new BoxLayout(addContentPanel,
		        BoxLayout.X_AXIS));
		addContentPanel.add(quickAdd);
		addContentPanel.add(Box.createHorizontalStrut(5));
		addContentPanel.add(addTextButton);
		addContentPanel.add(Box.createHorizontalStrut(5));
		addContentPanel.add(addImagesButton);
		addContentPanel.add(Box.createHorizontalStrut(5));
		addContentPanel.add(removeButton);

		JPanel playlistPanel = new JPanel();
		playlistPanel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
		playlistPanel.setLayout(new BorderLayout(5, 5));
		playlistPanel.add(addContentPanel, BorderLayout.NORTH);
		playlistPanel.add(new JScrollPane(createPlaylist(model),
		        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
		        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),
		        BorderLayout.CENTER);
		playlistPanel.add(new ExtraControlsPanel(model), BorderLayout.SOUTH);
		return playlistPanel;
	}

	private Component createPlaylist(final ViewModel model)
	{
		final JList list = new JList();
		list.setCellRenderer(new PlaylistCellRenderer());

		/*
		 * Fill the list with the current playlist's contents. Implemented as a
		 * runnable to allow for re-use below.
		 */
		final Runnable updatePlaylist = new Runnable()
		{
			@Override
			public void run()
			{
				DefaultListModel listModel = new DefaultListModel();
				Playlist playlist = model.getPlaylist();
				if (playlist != null)
				{
					for (PlaylistItem item : playlist.getItems())
					{
						listModel.addElement(item);
					}
				}
				list.setModel(listModel);
			}
		};
		updatePlaylist.run();

		/*
		 * Update whenever the playlist changes.
		 */
		final PropertyChangeListener playlistChangeListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				updatePlaylist.run();
			}
		};
		final PropertyChangeListener selectionListener = new PropertyChangeListener()
		{
			@Override
			public void propertyChange(PropertyChangeEvent evt)
			{
				Playlist playlist = model.getPlaylist();
				PlaylistSelectionModel selectionModel = playlist.getSelectionModel();
				list.setSelectedIndex(selectionModel.getSelectedIndex());
			}
		};

		Playlist playlist = model.getPlaylist();
		playlist.addPropertyChangeListener(Playlist.ITEMS_PROPERTY,
		        playlistChangeListener);
		PlaylistSelectionModel selectionModel = playlist.getSelectionModel();
		selectionModel.addPropertyChangeListener(
		        PlaylistSelectionModel.SELECTED_ITEM_PROPERTY,
		        selectionListener);

		/* The playlist may be replaced. If so, re-assign the listener. */
		model.addPropertyChangeListener(ViewModel.PLAYLIST_PROPERTY,
		        new PropertyChangeListener()
		        {
			        @Override
			        public void propertyChange(PropertyChangeEvent evt)
			        {
				        Playlist oldValue = (Playlist) evt.getOldValue();
				        if (oldValue != null)
				        {
					        oldValue.removePropertyChangeListener(
					                Playlist.ITEMS_PROPERTY,
					                playlistChangeListener);
					        PlaylistSelectionModel selectionModel = oldValue.getSelectionModel();
					        selectionModel.removePropertyChangeListener(
					                PlaylistSelectionModel.SELECTED_ITEM_PROPERTY,
					                selectionListener);
				        }

				        Playlist newValue = (Playlist) evt.getNewValue();
				        if (newValue != null)
				        {
					        newValue.addPropertyChangeListener(
					                Playlist.ITEMS_PROPERTY,
					                playlistChangeListener);
					        PlaylistSelectionModel selectionModel = newValue.getSelectionModel();
					        selectionModel.addPropertyChangeListener(
					                PlaylistSelectionModel.SELECTED_ITEM_PROPERTY,
					                selectionListener);
				        }

				        updatePlaylist.run();
			        }
		        });

		/*
		 * Update the playlist's selection model to match the JList.
		 */
		list.addListSelectionListener(new ListSelectionListener()
		{
			@Override
			public void valueChanged(ListSelectionEvent e)
			{
				Playlist playlist = model.getPlaylist();
				PlaylistSelectionModel selectionModel = playlist.getSelectionModel();
				Object selectedValue = list.getSelectedValue();
				if ((selectedValue == null)
				        || selectedValue instanceof PlaylistItem)
				{
					selectionModel.setSelectedItem((PlaylistItem) selectedValue);
				}
			}
		});

		/*
		 * Allow for re-ordering of items in the playlist.
		 */
		ReorderListener.addToList(list, new Reorderable()
		{
			@Override
			public void move(int from, int to)
			{
				model.getPlaylist().move(from, to);
			}

			@Override
			public void swap(int index1, int index2)
			{
				model.getPlaylist().swap(index1, index2);
			}
		});

		/*
		 * Allow for editing of items by double-clicking.
		 */
		list.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					if (!list.isSelectionEmpty())
					{
						PlaylistItem playlistItem = (PlaylistItem) list.getSelectedValue();
						Object value = playlistItem.getValue();

						if (value instanceof LyricRef)
						{
							value = CatalogManager.getCatalog().getLyric(
							        (LyricRef) value);
						}

						if (value instanceof Lyric)
						{
							Lyric selection = (Lyric) value;
							EditLyricDialog dialog = new EditLyricDialog(
							        CompactPresentationUI.this, selection);
							SwingUtilities2.centerOnParent(dialog);
							dialog.setVisible(true);

						}
						else if (value instanceof CharSequence)
						{
							EditTextDialog dialog = new EditTextDialog(
							        CompactPresentationUI.this, playlistItem);
							SwingUtilities2.centerOnParent(dialog);
							dialog.setVisible(true);
						}
					}
				}
			}
		});

		return list;
	}

	private Component createQuickAddField(final ViewModel model)
	{
		final JTextField result = new JTextField();
		result.setBorder(BorderFactory.createCompoundBorder(result.getBorder(),
		        BorderFactory.createEmptyBorder(5, 5, 5, 5)));

		result.addActionListener(new QuickAddAction(result)
		{
			@Override
			protected void playlistItemSelected(PlaylistItem item)
			{
				Playlist playlist = model.getPlaylist();
				if (playlist != null)
				{
					playlist.add(item);
				}
			}
		});

		return result;
	}

	private void showExceptionDialog(String message, Exception e)
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
			e.printStackTrace();
			JOptionPane.showMessageDialog(parent, Resources.get().getString(
			        "editorFrame.exception", message, e.getLocalizedMessage()),
			        e.getClass().getName(), JOptionPane.WARNING_MESSAGE);
		}
	}

	private ImageIcon getIcon(String name)
	{
		return new ImageIcon(getClass().getClassLoader().getResource(name));
	}

	private static final class CursorPositionListener extends MouseAdapter
	{
		private final FullScreenDisplay display;
		private final CompactPresentationUI compactEditor;

		private CursorPositionListener(FullScreenDisplay display,
		        CompactPresentationUI compactEditor)
		{
			this.display = display;
			this.compactEditor = compactEditor;
		}

		@Override
		public void mouseEntered(MouseEvent e)
		{
			if (e.getModifiersEx() == 0)
			{
				focusOnUI(e);
			}
		}

		@Override
		public void mouseMoved(MouseEvent e)
		{
			if (e.getModifiersEx() == 0)
			{
				focusOnUI(e);
			}
		}

		private void focusOnUI(MouseEvent e)
		{
			KeyboardFocusManager focusManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
			Window activeWindow = focusManager.getActiveWindow();

			if ((activeWindow == display) || (activeWindow == compactEditor))
			{
				GLView view = compactEditor.getPreview();
				view.requestFocusInWindow();

				Point target = e.getLocationOnScreen();
				Rectangle bounds = compactEditor.getBounds();
				boolean moveCursor = false;

				if (target.x > bounds.getMaxX() - 1)
				{
					target.x = (int) bounds.getMaxX() - 1;
					moveCursor = true;
				}
				else if (target.x < bounds.getMinX())
				{
					target.x = (int) bounds.getMinX();
					moveCursor = true;
				}

				if (target.y > bounds.getMaxY() - 1)
				{
					target.y = (int) bounds.getMaxY() - 1;
					moveCursor = true;
				}
				else if (target.y < bounds.getMinY())
				{
					target.y = (int) bounds.getMinY();
					moveCursor = true;
				}

				if (moveCursor)
				{
					try
					{
						Robot robot = new Robot();
						robot.mouseMove(target.x, target.y);
					}
					catch (AWTException ex)
					{
						ex.printStackTrace();
					}
				}
			}
		}
	}

	private class SelectedStateUpdater implements PropertyChangeListener
	{
		private Action action;

		public SelectedStateUpdater(Action action)
		{
			super();
			this.action = action;
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt)
		{
			action.putValue(Action.SELECTED_KEY, evt.getNewValue());
		}
	}
}
