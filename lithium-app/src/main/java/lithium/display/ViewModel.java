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

import java.beans.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import javax.imageio.*;
import javax.media.opengl.*;
import javax.swing.*;

import com.github.meinders.common.*;
import com.github.meinders.common.animation.Timer;
import lithium.*;
import lithium.animation.*;
import lithium.animation.legacy.scrolling.*;
import lithium.catalog.*;
import lithium.display.opengl.*;
import lithium.editor.*;

/**
 * A model that keeps track of content and scrolling for one or more views.
 *
 * @author Gerrit Meinders
 */
public class ViewModel implements PropertyChangeListener
{
	/** Property constant: whether a {@link ControlsPanel} should be visible. */
	public static final String CONTROLS_VISIBLE_PROPERTY = "controlsVisible";

	/** Property constant: whether views should display the current content. */
	public static final String CONTENT_VISIBLE_PROPERTY = "contentVisible";

	public static final String CONTENT_LOCKED_PROPERTY = "contentLocked";

	/** Property constant: whether views should display the background. */
	public static final String BACKGROUND_VISIBLE_PROPERTY = "backgroundVisible";

	/** Property constant: the content item being viewed. */
	public static final String CONTENT_PROPERTY = "content";

	/** Property constant: whether automatic scrolling is active */
	public static final String AUTO_SCROLLING_ENABLED_PROPERTY = "autoScrollingEnabled";

	/** Property constant: the aspect ratio of the visible view area */
	public static final String ASPECT_RATIO_PROPERTY = "aspectRatio";

	/** Property constant: the playlist from which content items are shown. */
	public static final String PLAYLIST_PROPERTY = null;

	/** Property constant: the {@link Scroller} performing scrolling */
	public static final String SCROLLER_PROPERTY = "scroller";

	/** Provides support for bounds properties. */
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Provides asynchronous execution of long-running tasks to views.
	 */
	private final ExecutorService executor = Executors.newSingleThreadExecutor();

	private AutoScroller autoScroller;

	private Scroller scroller;

	private final SynchronizationModel synchronizationModel;

	private Playlist playlist = null;

	private Object content = null;

	private boolean controlsVisible = false;

	private boolean contentVisible = true;

	/**
	 * Indicates whether the content of the model is locked. When locked, the
	 * content can't be changed. Any attempted changes to the content are
	 * ignored. When the content is unlocked, it is immediately set to the
	 * currently selected playlist item, if any.
	 */
	private boolean contentLocked;

	private boolean backgroundVisible = true;

	private float aspectRatio = 4.0f / 3.0f;

	private int referenceCount = 0;

	/** @since 0.8, experimental 0.9x */
	private GUIPluginManager guiPluginManager;

	private boolean disposed = false;

	private final Timer transitionTimer = new Timer();

	private PlaylistItem playlistItem;

	private final Runnable nextItem = new Runnable()
	{
		@Override
		public void run()
		{
			getPlaylist().getSelectionModel().selectNext();
		}
	};

	/**
	 * Constructs a new view model.
	 */
	public ViewModel()
	{
		this(false);
	}

	/**
	 * Constructs a new view model.
	 *
	 * @param standalone if {@code true}, the model will ignore global playlist
	 *            or configuration changes.
	 */
	public ViewModel(boolean standalone)
	{
		autoScroller = new AutoScroller();

		if (standalone)
		{
			setPlaylist(new Playlist());
		}
		else
		{
			PlaylistManager.addPropertyChangeListener(this);
			setPlaylist(PlaylistManager.getPlaylist());
		}

		configure(ConfigManager.getConfig());
		if (!standalone)
		{
			ConfigManager configManager = ConfigManager.getInstance();
			configManager.addPropertyChangeListener(this);
		}

		guiPluginManager = new GUIPluginManager();

		synchronizationModel = new SynchronizationModel();
	}

	@Override
	public void finalize()
	{
		dispose();
	}

	public void register(Object user)
	{
		referenceCount++;
	}

	public void unregister(Object user)
	{
		referenceCount--;
		if (referenceCount == 0)
		{
			dispose();
		}
	}

	/**
	 * Configures the view model according to the given configuration settings.
	 *
	 * @param config the configuration settings
	 */
	private void configure(Config config)
	{
		if (config == null)
		{
			throw new NullPointerException("config");
		}
		// FIXME: Obey specs or change them!
		// Scroller scroller = getScroller();
		// switch (config.getScrollType()) {
		// case PLAIN:
		// if (!(scroller instanceof PlainScroller)) {
		// scroller = new PlainScroller();
		// }
		// break;
		// case SMOOTH:
		// default:
		// if (!(scroller instanceof SmoothScroller)) {
		// scroller = new SmoothScroller();
		// }
		// break;
		// }
		Scroller scroller = new NewScroller(0.0, 1.0);
		setScroller(scroller);
		autoScroller.setScrollRate(config.getAutoScrollSpeed());
	}

	private synchronized void dispose()
	{
		if (disposed)
		{
			return;
		}
		disposed = true;

		ExecutorService executorService = getExecutorService();
		executorService.shutdownNow();

		if (scroller != null)
		{
			scroller.dispose();
			scroller = null;
		}

		if (autoScroller != null)
		{
			autoScroller.dispose();
			autoScroller = null;
		}

		ConfigManager configManager = ConfigManager.getInstance();
		configManager.removePropertyChangeListener(this);
		PlaylistManager.removePropertyChangeListener(this);

		setPlaylist(null);
	}

	synchronized boolean isDisposed()
	{
		return disposed;
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		if (e.getPropertyName() == PlaylistSelectionModel.SELECTED_ITEM_PROPERTY)
		{
			if (!isContentLocked())
			{
				setContentFromPlaylist();
			}
		}
		else if (e.getPropertyName() == PlaylistManager.PLAYLIST_PROPERTY)
		{
			setPlaylist(PlaylistManager.getPlaylist());

		}
		else if (e.getPropertyName() == ConfigManager.CONFIG_PROPERTY)
		{
			configure(ConfigManager.getConfig());
		}
	}

	private void setContentFromPlaylist()
	{
		PlaylistItem selectedItem = playlist.getSelectionModel().getSelectedItem();
		if (selectedItem != null)
		{
			Object newContent = selectedItem.getValue();
			if (content != newContent)
			{
				setContentVisible(true);
				setContent(selectedItem);
			}
		}
	}

	public void setScroller(Scroller scroller)
	{
		if (this.scroller != scroller)
		{
			if (this.scroller != null)
			{
				this.scroller.dispose();
			}
			Scroller oldValue = this.scroller;
			this.scroller = scroller;
			autoScroller.setScroller(scroller);
			pcs.firePropertyChange(SCROLLER_PROPERTY, oldValue, scroller);
		}
	}

	public void setPlaylist(Playlist playlist)
	{
		Playlist oldValue = this.playlist;
		if (oldValue != playlist)
		{
			if (oldValue != null)
			{
				oldValue.getSelectionModel().removePropertyChangeListener(this);
			}

			this.playlist = playlist;
			if (!isContentLocked() && playlist != null)
			{
				addDebugContent(playlist);

				PlaylistItem selected = playlist.getSelectionModel().getSelectedItem();
				if (selected != null)
				{
					setContent(selected);
				}
				playlist.getSelectionModel().addPropertyChangeListener(this);
			}

			pcs.firePropertyChange(PLAYLIST_PROPERTY, oldValue, playlist);
		}
	}

	private void addDebugContent(Playlist playlist)
	{
		Config config = ConfigManager.getConfig();
		if (config.isEnabled(Config.DEBUG_PLAYLIST_CONTENT))
		{
			playlist.add(new PlaylistItem("Plain text\nAll your base\n"
			        + "are belong to us!\nMove zig\nfor great justice!"));
			playlist.add(new PlaylistItem(new LyricRef("Opwekking", 123)));

			ExtensionFileFilter imageFilter = new ExtensionFileFilter(null,
			        ImageIO.getReaderFileSuffixes(), false);
			File[] images = ConfigManager.getSettingsFolder().listFiles(
			        imageFilter);
			if (images != null)
			{
				for (File file : images)
				{
					PlaylistItem item = new PlaylistItem(new ImageRef(file));
					item.setTransitionDelay(2000);
					playlist.add(item);
				}
			}
		}
	}

	public Playlist getPlaylist()
	{
		return playlist;
	}

	public void setControlsVisible(boolean controlsVisible)
	{
		boolean oldValue = this.controlsVisible;
		this.controlsVisible = controlsVisible;
		pcs.firePropertyChange(CONTROLS_VISIBLE_PROPERTY, oldValue,
		        controlsVisible);
	}

	public boolean isControlsVisible()
	{
		return controlsVisible;
	}

	public void setContentVisible(boolean contentVisible)
	{
		if (this.contentVisible != contentVisible)
		{
			this.contentVisible = contentVisible;

			// make sure content is never visible without background
			if (contentVisible && !isBackgroundVisible())
			{
				setBackgroundVisible(true);
			}

			pcs.firePropertyChange(CONTENT_VISIBLE_PROPERTY, !contentVisible,
			        contentVisible);
		}
	}

	public boolean isContentVisible()
	{
		return contentVisible;
	}

	public void setContentLocked(boolean contentLocked)
	{
		if (this.contentLocked != contentLocked)
		{
			this.contentLocked = contentLocked;
			if (!contentLocked)
			{
				setContentFromPlaylist();
			}
			pcs.firePropertyChange(CONTENT_LOCKED_PROPERTY, !contentLocked,
			        contentLocked);
		}
	}

	public boolean isContentLocked()
	{
		return contentLocked;
	}

	public void setBackgroundVisible(boolean backgroundVisible)
	{
		if (this.backgroundVisible != backgroundVisible)
		{
			boolean oldValue = this.backgroundVisible;
			this.backgroundVisible = backgroundVisible;

			// make sure content is never visible without background
			if (!backgroundVisible && isContentVisible())
			{
				setContentVisible(false);
			}

			pcs.firePropertyChange(BACKGROUND_VISIBLE_PROPERTY, oldValue,
			        backgroundVisible);
		}
	}

	/**
	 * Returns whether view should show a background.
	 *
	 * @return whether backgrounds should be shown
	 */
	public boolean isBackgroundVisible()
	{
		return backgroundVisible;
	}

	/**
	 * Returns the model's scroller.
	 *
	 * @return the scroller
	 */
	public Scroller getScroller()
	{
		return scroller;
	}

	public AutoScroller getAutoScroller()
	{
		return autoScroller;
	}

	/**
	 * Sets the content to be viewed.
	 *
	 * @param content the content to be viewed.
	 */
	public void setContent(Object content)
	{
		if (content instanceof PlaylistItem)
		{
			PlaylistItem playlistItem = (PlaylistItem) content;
			setContent(playlistItem.getValue());
			this.playlistItem = playlistItem;

		}
		else
		{
			Object oldValue = this.content;
			this.content = content;
			playlistItem = null;

			setAutoScrollingEnabled(false);

			if (scroller != null)
			{
				scroller.setTarget(0.0f);
				scroller.setValue(0.0f);
			}

			pcs.firePropertyChange(CONTENT_PROPERTY, oldValue, content);
		}

		transitionTimer.restart();
	}

	/**
	 * Returns the content that is currently viewed.
	 *
	 * @return the content that is currently viewed
	 */
	public Object getContent()
	{
		if (playlistItem != null)
		{
			/*
			 * Perform an automatic transition if the set delay is passed.
			 */
			int transitionDelay = playlistItem.getTransitionDelay();
			if ((transitionDelay > 0)
			        && (transitionDelay < transitionTimer.currentTime() * 1000.0))
			{
				transitionTimer.restart();
				invokeOnEDT(nextItem);
			}
		}

		return content;
	}

	public Object getNextContent()
	{
		PlaylistItem playlistItem = getNextItem();
		return playlistItem == null ? null : playlistItem.getValue();
	}

	/**
	 * Invokes the given runnable on the AWT event dispatching thread (EDT). The
	 * runnable may be invoked right away, if called from the EDT, or at some
	 * later time, if called from another thread.
	 *
	 * @param runnable the runnable to be invoked on the EDT
	 */
	private void invokeOnEDT(Runnable runnable)
	{
		if (SwingUtilities.isEventDispatchThread())
		{
			runnable.run();
		}
		else
		{
			SwingUtilities.invokeLater(runnable);
		}
	}

	public void setAutoScrollingEnabled(boolean autoScrollingEnabled)
	{
		boolean oldValue = autoScroller.isEnabled();
		autoScroller.setEnabled(autoScrollingEnabled);
		pcs.firePropertyChange(AUTO_SCROLLING_ENABLED_PROPERTY, oldValue,
		        autoScrollingEnabled);
	}

	public boolean isAutoScrollingEnabled()
	{
		return autoScroller.isEnabled();
	}

	public void setAspectRatio(float aspectRatio)
	{
		assert aspectRatio != 0 : "aspectRatio != 0";
		float oldValue = this.aspectRatio;
		this.aspectRatio = aspectRatio;
		pcs.firePropertyChange(ASPECT_RATIO_PROPERTY, oldValue, aspectRatio);
	}

	public float getAspectRatio()
	{
		return aspectRatio;
	}

	/**
	 * Returns the GUI plugin manager.
	 *
	 * @return the GUI plugin manager
	 */
	public GUIPluginManager getGUIPluginManager()
	{
		return guiPluginManager;
	}

	public SynchronizationModel getSynchronizationModel()
	{
		return synchronizationModel;
	}

	/**
	 * Add a PropertyChangeListener to the listener list. The listener is
	 * registered for all properties. The same listener object may be added more
	 * than once, and will be called as many times as it is added. If
	 * <code>listener</code> is null, no exception is thrown and no action is
	 * taken.
	 *
	 * @param listener The PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Add a PropertyChangeListener for a specific property. The listener will
	 * be invoked only when a call on firePropertyChange names that specific
	 * property. The same listener object may be added more than once. For each
	 * property, the listener will be invoked the number of times it was added
	 * for that property. If <code>propertyName</code> or <code>listener</code>
	 * is null, no exception is thrown and no action is taken.
	 *
	 * @param propertyName The name of the property to listen on.
	 * @param listener The PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(String propertyName,
	        PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(propertyName, listener);
	}

	/**
	 * Remove a PropertyChangeListener from the listener list. This removes a
	 * PropertyChangeListener that was registered for all properties. If
	 * <code>listener</code> was added more than once to the same event source,
	 * it will be notified one less time after being removed. If
	 * <code>listener</code> is null, or was never added, no exception is thrown
	 * and no action is taken.
	 *
	 * @param listener The PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	/**
	 * Remove a PropertyChangeListener for a specific property. If
	 * <code>listener</code> was added more than once to the same event source
	 * for the specified property, it will be notified one less time after being
	 * removed. If <code>propertyName</code> is null, no exception is thrown and
	 * no action is taken. If <code>listener</code> is null, or was never added
	 * for the specified property, no exception is thrown and no action is
	 * taken.
	 *
	 * @param propertyName The name of the property that was listened on.
	 * @param listener The PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(String propertyName,
	        PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(propertyName, listener);
	}

	public PlaylistItem getCurrentItem()
	{
		return playlistItem;
	}

	public PlaylistItem getNextItem()
	{
		PlaylistSelectionModel selectionModel = playlist.getSelectionModel();
		if (selectionModel.isLastItemSelected())
		{
			return null;
		}

		int nextIndex = selectionModel.getSelectedIndex() + 1;
		return playlist.getItem(nextIndex);
	}

	/*
	 * -----------------------------------------------------------------------
	 */

	private static final int MAXIMUM_CONTENT_MODELS = 5;

	private final Collection<ContentRenderer> contentRenderers = new ArrayList<ContentRenderer>();

	private final LinkedList<ContentModel> contentModels = new LinkedList<ContentModel>();

	{
//		contentRenderers.add(new GLTextRenderer2(this));
		contentRenderers.add(new GLTextRenderer(this));
		contentRenderers.add(new GLImageRenderer(executor));
	}

	public ContentModel getContentModel(Object content)
	{
		return getContentModel(content, null);
	}

	public synchronized ContentModel getContentModel(Object content,
	        PlaylistItem playlistItem)
	{
		ContentModel result = null;

		for (final Iterator<ContentModel> i = contentModels.iterator(); i.hasNext();)
		{
			ContentModel contentModel = i.next();
			if (content == contentModel.getContent())
			{
				result = contentModel;

				// Re-insert at the start of the list.
				i.remove();
				contentModels.addFirst(contentModel);
				break;
			}
		}

		if (result == null)
		{
			GLContentRenderer renderer = getContentRenderer(content);

			PreparedContent preparedContent = null;
			if (renderer == null)
			{
				if (content != null)
				{
					String message = "No OpenGL rendering support for content: "
					        + content.getClass();
					renderer = getContentRenderer(message);
					preparedContent = renderer.prepare(message);
				}
			}
			else
			{
				preparedContent = renderer.prepare(content);
			}

			result = new ContentModel(content, preparedContent, renderer);
			contentModels.addFirst(result);

			if (contentModels.size() > MAXIMUM_CONTENT_MODELS)
			{
				ContentModel removed = contentModels.removeLast();
				removed.flush();
			}
		}

		return result;
	}

	/**
	 * Returns a content renderer suitable for the given content. This method
	 * must support plain text ({@link String}) content.
	 *
	 * @param content the content to get a renderer for
	 * @return a renderer for the given content
	 */
	private GLContentRenderer getContentRenderer(Object content)
	{
		GLContentRenderer result = null;

		if (content instanceof GLContentRenderer)
		{
			result = (GLContentRenderer) content;
		}
		else
		{
			for (ContentRenderer contentRenderer : contentRenderers)
			{
				if ((contentRenderer instanceof GLContentRenderer)
				        && contentRenderer.accept(content))
				{
					result = (GLContentRenderer) contentRenderer;
					break;
				}
			}
		}

		return result;
	}

	private GLContext sharedGLContext;

	public GLContext getSharedGLContext()
	{
		return sharedGLContext;
	}

	public void setSharedGLContext(GLContext sharedGLContext)
	{
		this.sharedGLContext = sharedGLContext;
	}

	public ExecutorService getExecutorService()
	{
		return executor;
	}
}
