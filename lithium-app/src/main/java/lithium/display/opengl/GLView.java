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

package lithium.display.opengl;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.beans.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.awt.*;
import javax.media.opengl.fixedfunc.*;
import javax.swing.*;

import com.github.meinders.common.animation.*;
import com.github.meinders.common.opengl.*;
import com.jogamp.opengl.util.gl2.*;
import lithium.*;
import lithium.animation.*;
import lithium.animation.legacy.scrolling.*;
import lithium.catalog.*;
import lithium.display.*;

/**
 * <p>
 * This component is a view for {@link Lyric} objects, and at the same time a
 * controller for the underlying {@link ViewModel}. The view automatically loads
 * relevant configuration settings directly from the {@link ConfigManager} and
 * {@link Config} classes.
 *
 * <p>
 * The view is controlled through its model, which may be shared amongst
 * multiple instances. A shared model results in synchronized content,
 * visibility and scrolling for all views sharing the same model.
 *
 * @author Gerrit Meinders
 */
public class GLView extends GLCanvas
implements PropertyChangeListener
{
	public static final String SEPARATED_SCROLLING_PROPERTY = "seperatedScrolling";

	private static final long serialVersionUID = 1L;

	private static final GLCapabilities capabilities = new GLCapabilities( GLProfile.getGL2GL3() );
	static
	{
		// At the moment, there are no additional capabilities needed.
	}

	private ViewModel model;

	private GUIPluginManager pluginManager;

	private boolean preview;

	private Config config;

	private AdaptiveFPSAnimator animator;

	private volatile Background background;

	/**
	 * Scroll in preview only until the left mouse button is clicked. Then
	 * automatically determine the optimal transition and perform it.
	 */
	private boolean separatedScrolling = false;

	/**
	 * Offset between the preview and the model.
	 */
	private float separatedScrollingOffset = 0.0f;

	private TransitionModel transitionModel = new TransitionModel(this);

	private ShaderProgram shaderProgram = null;

	protected final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/**
	 * Constructs a new lyric view using the given model. The constructed view
	 * will not be a preview.
	 *
	 * @param model the model
	 */
	public GLView(ViewModel model)
	{
		this(model, false);
	}

	/**
	 * Constructs a new lyric view using the given model either as a preview or
	 * regular view.
	 *
	 * @param model the model
	 * @param preview whether the view should be a preview
	 */
	public GLView(ViewModel model, boolean preview)
	{
		super(capabilities, null, model.getSharedGLContext(), null);
		model.setSharedGLContext(getContext());

		reloadBackground();

		setModel(model);
		this.preview = preview;
		setConfig(ConfigManager.getConfig());

		ConfigManager configManager = ConfigManager.getInstance();
		configManager.addPropertyChangeListener(this);

		// set default background
		setBackground(config.getBackgroundColor());

		ScrollerMouseListener mouseListener = new ScrollerMouseListener();

		addGLEventListener(new GLEventListenerImpl());
		addMouseListener(mouseListener);
		addMouseMotionListener(mouseListener);
		addMouseWheelListener(mouseListener);

		final AdaptiveFPSAnimator animator = new AdaptiveFPSAnimator(this, 75);
		SwingUtilities.invokeLater(new Runnable()
		{
			@Override
			public void run()
			{
				animator.start();
			}
		});
		this.animator = animator;
		reloadBackground();

		setPreferredSize(new Dimension(300, 400));

		// listen to config changes
		ConfigManager.getConfig().addPropertyChangeListener(this);
	}

	/** Releases the resources used by the view, if any. */
	public void dispose()
	{
		if (animator.isAnimating())
		{
			animator.stop();
		}

		setModel(null);
		setPluginManager(null);
		setConfig(null);

		ConfigManager configManager = ConfigManager.getInstance();
		configManager.removePropertyChangeListener( this );
	}

	/**
	 * Sets the model to be used by the view.
	 *
	 * @param model the model
	 */
	public void setModel(ViewModel model)
	{
		if (this.model != null)
		{
			this.model.removePropertyChangeListener(this);
			this.model.unregister(this);
		}
		this.model = model;
		if (model != null)
		{
			setPluginManager(model.getGUIPluginManager());

			model.addPropertyChangeListener(this);
			model.register(this);
		}
	}

	/**
	 * Returns the view's model. Lyric view models may be shared by multiple
	 * views.
	 *
	 * @return the model
	 */
	public ViewModel getModel()
	{
		return model;
	}

	/**
	 * Returns whether the view is a preview.
	 *
	 * @return {@code true} if the view is a preview; {@code false} otherwise
	 */
	public boolean isPreview()
	{
		return preview;
	}

	/**
	 * Sets the plugin manager of the view.
	 *
	 * @param pluginManager the plugin manager
	 */
	private void setPluginManager(GUIPluginManager pluginManager)
	{
		if (this.pluginManager != null)
		{
			this.pluginManager.removePropertyChangeListener(this);
		}
		this.pluginManager = pluginManager;
		if (pluginManager != null)
		{
			pluginManager.addPropertyChangeListener(this);
		}
	}

	/**
	 * Sets the configuration settings used by the view.
	 *
	 * @param config the configuration settings
	 */
	public void setConfig(Config config)
	{
		if (this.config != null)
		{
			this.config.removePropertyChangeListener(this);
		}
		this.config = config;
		if (config != null)
		{
			config.addPropertyChangeListener(this);
		}
	}

	public void setSeparatedScrolling(boolean separatedScrolling)
	{
		if (this.separatedScrolling != separatedScrolling)
		{
			Scroller scroller = model.getScroller();
			if (scroller instanceof NewScroller)
			{
				NewScroller newScroller = (NewScroller) scroller;
				newScroller.setFadingEnabled(separatedScrolling);
			}

			separatedScrollingOffset = 0.0f;
			this.separatedScrolling = separatedScrolling;
			pcs.firePropertyChange(SEPARATED_SCROLLING_PROPERTY,
			        !separatedScrolling, separatedScrolling);
		}
	}

	public boolean isSeparatedScrolling()
	{
		return separatedScrolling;
	}

	public void propertyChange(PropertyChangeEvent e)
	{
		String property = e.getPropertyName();

		if (property == Config.BACKGROUND_IMAGE_PROPERTY)
		{
			reloadBackground();

		}
		else if (property == ConfigManager.CONFIG_PROPERTY)
		{
			setConfig(ConfigManager.getConfig());
			reloadBackground();
		}
	}

	/**
	 * Sets the visibility of the cursor.
	 *
	 * @param cursorVisible whether the cursor should be visible
	 */
	public void setCursorVisible(boolean cursorVisible)
	{
		if (cursorVisible)
		{
			setCursor(Cursor.getDefaultCursor());
		}
		else
		{
			Cursor transparent = Toolkit.getDefaultToolkit().createCustomCursor(
			        new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB),
			        new Point(0, 0), "transparant");
			setCursor(transparent);
		}
	}

	private void reloadBackground()
	{
		if (model != null)
		{
			background = new ImageBackground(this, model.getExecutorService());
		}
//		background = new MediaBackground(this);
	}

	private final class ScrollerMouseListener extends MouseAdapter
	{
		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
			if ( e.getComponent().isEnabled() && ( e.getScrollAmount() != 0 ) )
			{

				float offset = 0;
				offset = e.getWheelRotation() < 0 ? -1 : 1;

				if ( isPreview() && separatedScrolling )
				{
					separatedScrollingOffset += offset;
					// TODO: constrain scrolling offset
				}
				else
				{
					Scroller scroller = model.getScroller();
					scroller.setTarget( scroller.getValue() + offset, true );
				}
			}
		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			if (isEnabled())
			{
				if (SwingUtilities.isLeftMouseButton(e))
				{
					if (separatedScrolling)
					{
						Scroller scroller = model.getScroller();
						scroller.setTarget(scroller.getValue()
						        + separatedScrollingOffset, false);
						separatedScrollingOffset = 0.0f;
					}

				}
				else if (SwingUtilities.isRightMouseButton(e))
				{
					model.setAutoScrollingEnabled(!model.isAutoScrollingEnabled());
				}
			}
		}
	}

	private class GLEventListenerImpl implements GLEventListener
	{
		private FrameCounter frameCounter = new FrameCounter();

		private int width = 0;

		private int height = 0;

		private final Map<GUIPlugin, GUIPlugin.GLRenderer> pluginRenderers;

		private ContentModel currentContentModel = null;

		public GLEventListenerImpl()
		{
			pluginRenderers = new LinkedHashMap<GUIPlugin, GUIPlugin.GLRenderer>();
			for (GUIPlugin plugin : pluginManager.getPlugins())
			{
				pluginRenderers.put(plugin, plugin.getGLRenderer());
			}

			pluginManager.addPropertyChangeListener(new PropertyChangeListener()
			{
				@Override
				public void propertyChange(PropertyChangeEvent e)
				{
					String name = e.getPropertyName();
					if (GUIPluginManager.PLUGINS_PROPERTY.equals(name))
					{
						/*
						 * Remove renderers for removed plugins.
						 */
						Set<GUIPlugin> plugins = pluginManager.getPlugins();
						pluginRenderers.keySet().retainAll(plugins);

						/*
						 * Add renderers for added plugins.
						 */
						for (GUIPlugin plugin : plugins)
						{
							if (!pluginRenderers.containsKey(plugin))
							{
								pluginRenderers.put(plugin,
								        plugin.getGLRenderer());
							}
						}
					}
				}
			});
		}

		@Override
		public void display(GLAutoDrawable drawable)
		{
			if (drawable.getWidth() <= 0 || drawable.getHeight() <= 0)
			{
				return;
			}

			ViewModel model = getModel();
			PlaylistItem currentItem = model.getCurrentItem();
			PlaylistItem nextItem = model.getNextItem();

			int visibleHeight = (int) (width / model.getAspectRatio());
			float visibleBottom = 0.5f * (height - visibleHeight);
			float visibleTop = visibleBottom + visibleHeight;

			ContentModel currentContentModel = model.getContentModel(
			        model.getContent(), currentItem);

			// pre-load the next content
			model.getContentModel(model.getNextContent(), nextItem);

			background.setVisible(true);
			background.setTop(visibleTop);
			background.setBottom(visibleBottom);
			ContentModel backgroundModel = model.getContentModel(background);
			transitionModel.setBackgroundModel(backgroundModel);

			if (this.currentContentModel != currentContentModel)
			{
				this.currentContentModel = currentContentModel;

				transitionModel.transition(currentContentModel);
				// transitionModel.setContentVisible(model.isContentVisible());
				// transitionModel.setBackgroundVisible(model.isBackgroundVisible());
			}
			transitionModel.setContentVisible(model.isContentVisible());
			transitionModel.setBackgroundVisible(model.isBackgroundVisible()
			        && transitionModel.second.isViewBackgroundVisible());

			/*
			 * Current content position inside view.
			 */
			NewScroller scroller = (NewScroller) model.getScroller();
			float scrollValue;
			if (isPreview() && separatedScrolling)
			{
				scrollValue = scroller.getTarget() + separatedScrollingOffset;
			}
			else
			{
				scrollValue = scroller.getValue();
			}
			float scrollTarget = scroller.getTarget();

			final GL gl = drawable.getGL();
			final GL2 gl2 = new DebugGL2( gl.getGL2() );
			gl2.glMatrixMode( GLMatrixFunc.GL_MODELVIEW );
			gl2.glLoadIdentity();
			gl2.glDisable(GL.GL_DEPTH_TEST);

			gl.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);

			/*
			 * Prepare transition between content and render it.
			 */
			Rectangle2D contentBounds = getVisibleBounds();
			Point2D.Float contentOffset = new Point2D.Float(0.0f, scrollValue);

			transitionModel.prepare(gl, contentBounds, contentOffset);
			transitionModel.render(gl, contentBounds, contentOffset);

			/*
			 * Scrolling target indicators.
			 */
			GLContentRenderer contentRenderer = currentContentModel.getRenderer();
			if (isPreview() && (contentRenderer instanceof GLTextRenderer))
			{
				GLTextRenderer textRenderer = (GLTextRenderer) contentRenderer;
				float normalLineHeight = textRenderer.getNormalLineHeight();
				float scrollTargetY = (float) contentBounds.getMaxY()
				        + scrollModelToView(scrollValue - scrollTarget,
				                normalLineHeight);

				gl2.glBegin(GL.GL_LINES);
				gl2.glColor3f(1.0f, 1.0f, 1.0f);
				gl2.glVertex2d(0.01 * width, scrollTargetY);
				gl2.glVertex2d(0.05 * width, scrollTargetY);
				gl2.glVertex2d(0.95 * width, scrollTargetY);
				gl2.glVertex2d(0.99 * width, scrollTargetY);
				gl2.glEnd();
			}

			/*
			 * Render plugins.
			 */
			Rectangle2D viewBounds = new Rectangle2D.Double(0.0, visibleBottom,
			        width, visibleHeight);
			for (GUIPlugin.GLRenderer pluginRenderer : pluginRenderers.values())
			{
				pluginRenderer.render(gl, viewBounds);
			}

			if (isPreview() || config.isEnabled(Config.SHOW_FRAMES_PER_SECOND))
			{
				String text = frameCounter.getFramesPerSecond() + " fps";
				int font = GLUT.BITMAP_HELVETICA_10;

				GLUT glut = new GLUT();
				int x = 5;
				int y = 5;

				gl2.glColor3f(1.0f, 1.0f, 1.0f);
				gl2.glRasterPos2i(x, y);
				glut.glutBitmapString(font, text);

				frameCounter.countFrame();
			}
		}

		private Rectangle2D getVisibleBounds()
		{
			int visibleHeight = (int) (width / model.getAspectRatio());
			float visibleBottom = 0.5f * (height - visibleHeight);

			Rectangle2D margins = config.getFullScreenMargins();

			double left = margins.getMinX() * width;
			double right = margins.getMaxX() * width;
			double top = visibleBottom + margins.getMaxY() * visibleHeight;
			double bottom = visibleBottom + margins.getMinY() * visibleHeight;

			return new Rectangle2D.Double(left, bottom, right - left, top
			        - bottom);
		}

		@Override
		public void init(GLAutoDrawable drawable)
		{
			// Thread.currentThread().setPriority(Thread.NORM_PRIORITY + 2);

			GL gl = drawable.getGL();

			// shaderProgram = new ShaderProgram(gl);
			// shaderProgram.attach(GL.GL_VERTEX_SHADER, "vertex.glsl",
			// getClass().getResourceAsStream("vertex.glsl"));
			// shaderProgram.attach(GL.GL_FRAGMENT_SHADER, "fragment.glsl",
			// getClass().getResourceAsStream("fragment.glsl"));

			for (GUIPlugin.GLRenderer renderer : pluginRenderers.values())
			{
				renderer.init(gl);
			}
		}

		@Override
		public void dispose( GLAutoDrawable drawable )
		{
		}

		@Override
		public void reshape(GLAutoDrawable drawable, int x, int y, int width,
		        int height)
		{
			GL gl = drawable.getGL();
			final GL2 gl2 = gl.getGL2();
			gl2.glMatrixMode( GLMatrixFunc.GL_PROJECTION );
			gl2.glLoadIdentity();

			if (width > 0)
			{
				this.width = 1024;
				this.height = 1024 * height / width;
				int depth = Math.max(this.width, this.height);
				gl2.glOrtho(0, this.width, 0, this.height, -depth, depth);
			}
		}
	}

	public float scrollModelToView(float scrollValue, float lineHeight)
	{
		return scrollValue * lineHeight - 1; // XXX what's the -1 for?
	}

	protected Config getConfig()
	{
		return config;
	}

	static Color grayscale(Color color, float scale)
	{
		float[] components = color.getRGBComponents(new float[4]);
		float value = components[0] * 0.3f * scale + components[1] * 0.59f
		        * scale + components[2] * 0.11f * scale;
		return new Color(value, value, value, components[3]);
	}

	static void setClearColor(GL gl, Color color)
	{
		float[] components = color.getRGBComponents(new float[4]);
		gl.glClearColor(components[0], components[1], components[2],
		        components[3]);
	}

	static void setColor(GL gl, Color color)
	{
		float[] components = color.getRGBComponents(new float[4]);
		final GL2 gl2 = gl.getGL2();
		gl2.glColor4f( components[ 0 ], components[ 1 ], components[ 2 ], components[ 3 ] );
	}

	static Color blend(Color first, Color second, double alpha)
	{
		return blend(first, second, (float) alpha);
	}

	private static Color blend(Color first, Color second, float alpha)
	{
		float[] firstComponents = first.getRGBColorComponents(new float[3]);
		float[] secondComponents = second.getRGBColorComponents(new float[3]);
		float inverse = 1.0f - alpha;
		return new Color(firstComponents[0] * inverse + secondComponents[0]
		        * alpha, firstComponents[1] * inverse + secondComponents[1]
		        * alpha, firstComponents[2] * inverse + secondComponents[2]
		        * alpha, 1.0f);
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
	@Override
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
	@Override
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
	@Override
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
	@Override
	public void removePropertyChangeListener(String propertyName,
	        PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(propertyName, listener);
	}
}
