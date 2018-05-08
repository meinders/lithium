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
import java.awt.geom.*;
import java.nio.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;

import com.github.meinders.common.animation.*;
import com.jogamp.opengl.util.texture.*;
import lithium.display.*;

class TransitionModel
{
	private GLView view;

	private ContentModel first;

	ContentModel second;

	private ContentModel background;

	private GLTransitionRenderer transition;

	private Timer timer;

	private Variable1D progress;

	private Variable1D contentAlpha;

	private Variable1D backgroundAlpha;

	private Texture firstTexture;

	private Texture secondTexture;

	private Texture framebufferTexture;

	boolean transitionEffects = true; // XXX Disabled transitions

	public TransitionModel(GLView view)
	{
		this.view = view;
		this.contentAlpha = new Constant1D(1.0);
		this.backgroundAlpha = new Constant1D(1.0);

		// transition = new FadeTransition();
		// transition = new SpinningRectanglesTransition();

		timer = new Timer();
	}

	public void setBackgroundModel(ContentModel background)
	{
		this.background = background;
	}

	public void transition(ContentModel contentModel)
	{
		first = second;
		firstTexture = secondTexture;
		second = contentModel;
		secondTexture = null;

		double duration = 1.0;
		double time = timer.currentTime();
		progress = new Transition1D(new Linear1D(0.0, time, 1.0 / duration),
		        time, time + duration);
	}

	public void setContentVisible(boolean contentVisible)
	{
		double branchTime = timer.currentTime();
		double endTime = branchTime + 1.0;
		double endValue = contentVisible ? 1.0 : 0.0;
		Cubic1D cubic = Cubic1D.branch(contentAlpha, branchTime, endValue, 0.0,
		        endTime);
		contentAlpha = new MinMax1D(cubic, 0.0, 1.0);
	}

	public void setBackgroundVisible(boolean backgroundVisible)
	{
		double branchTime = timer.currentTime();
		double endTime = branchTime + 1.0;
		double endValue = backgroundVisible ? 1.0 : 0.0;
		Cubic1D cubic = Cubic1D.branch(backgroundAlpha, branchTime, endValue,
		        0.0, endTime);
		backgroundAlpha = new MinMax1D(cubic, 0.0, 1.0);
	}

	public void prepare(GL gl, Rectangle2D bounds, Point2D offset)
	{
		Rectangle pixelBounds = project(gl, bounds);

		if (transition == null)
		{
			if (framebufferTexture == null)
			{
				framebufferTexture = emptyTexture(pixelBounds.width,
				        pixelBounds.height);
			}
		}
		else
		{
			double currentTime = timer.currentTime();
			double contentAlpha = this.contentAlpha.get(currentTime);
			double backgroundAlpha = this.backgroundAlpha.get(currentTime);

			final GL2 gl2 = gl.getGL2();

			/*
			 * Render first content to texture.
			 */
			if ((firstTexture == null) && (first != null) && first.ready())
			{
				if (isRenderToTextureAvailable(gl))
				{
					firstTexture = emptyTexture(pixelBounds.width,
					        pixelBounds.height);

					int[] framebuffer = startRenderToTexture(gl, firstTexture,
					        pixelBounds);

					gl2.glTranslated( -bounds.getX(), -bounds.getY(), 0.0 );
					background.render(gl, bounds, offset, backgroundAlpha);
					first.render(gl, bounds, offset, contentAlpha);
					gl2.glTranslated( bounds.getX(), bounds.getY(), 0.0 );

					endRenderToTexture(gl, framebuffer);

				}
				else
				{
					background.render(gl, bounds, offset, backgroundAlpha);
					first.render(gl, bounds, offset, contentAlpha);
					firstTexture = pixelsToTexture(gl, pixelBounds);
				}
			}

			/*
			 * Render second content to texture.
			 */
			if ((secondTexture == null) && (second != null) && second.ready())
			{
				if (isRenderToTextureAvailable(gl))
				{
					secondTexture = emptyTexture(pixelBounds.width,
					        pixelBounds.height);
					int[] framebuffer = startRenderToTexture(gl, secondTexture,
					        pixelBounds);

					gl2.glTranslated(-bounds.getX(), -bounds.getY(), 0.0);
					background.render(gl, bounds, offset, backgroundAlpha);
					second.render(gl, bounds, offset, contentAlpha);
					gl2.glTranslated(bounds.getX(), bounds.getY(), 0.0);

					endRenderToTexture(gl, framebuffer);

				}
				else
				{
					background.render(gl, bounds, offset, backgroundAlpha);
					second.render(gl, bounds, offset, contentAlpha);
					secondTexture = pixelsToTexture(gl, pixelBounds);
				}
			}
		}
	}

	private boolean isRenderToTextureAvailable(GL gl)
	{
		return gl.isExtensionAvailable("GL_EXT_framebuffer_object");
	}

	private int[] startRenderToTexture(GL gl, Texture texture,
	        Rectangle pixelBounds)
	{
		int[] framebuffers = new int[1];
		gl.glGenFramebuffers( framebuffers.length, framebuffers, 0 );
		int framebuffer = framebuffers[0];

		gl.glBindFramebuffer( GL2.GL_FRAMEBUFFER, framebuffer );
		gl.glFramebufferTexture2D( GL.GL_FRAMEBUFFER,
		                           GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D,
		                           texture.getTextureObject( gl ), 0 );

		gl.glBindFramebuffer( GL.GL_FRAMEBUFFER, framebuffer );
		return framebuffers;
	}

	private void endRenderToTexture(GL gl, int[] framebuffers)
	{
		gl.glBindFramebuffer( GL.GL_FRAMEBUFFER, 0 );
		gl.glDeleteFramebuffers( framebuffers.length, framebuffers, 0 );
	}

	public void render(GL gl, Rectangle2D bounds, Point2D offset)
	{
		double currentTime = timer.currentTime();
		double progress = this.progress.get(currentTime);
		double contentAlpha = Math.max(this.contentAlpha.get(currentTime),
		        view.isPreview() ? 0.25 : 0.0);
		double backgroundAlpha = this.backgroundAlpha.get(currentTime);

		background.render(gl, bounds, offset, backgroundAlpha);

		if (view.isPreview() || contentAlpha > 0.0)
		{
			if (transition == null)
			{
				second.render(gl, bounds, offset, contentAlpha);

				if (false)
				{
					Rectangle pixelBounds = project(gl, bounds);
					int[] framebuffers = startRenderToTexture(gl,
					        framebufferTexture, pixelBounds);
					gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
					gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
					second.render(gl, bounds, offset, contentAlpha);
					endRenderToTexture(gl, framebuffers);

					// shaderProgram.enable();
					framebufferTexture.enable(gl);
					framebufferTexture.bind(gl);
					gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
					gl.glEnable(GL.GL_BLEND);
					TextureCoords coords = framebufferTexture.getImageTexCoords();
					final GL2 gl2 = gl.getGL2();
					gl2.glBegin( GL2.GL_QUADS );
					gl2.glTexCoord2f( coords.left(), coords.top() );
					gl2.glVertex2d( bounds.getMinX(), bounds.getMaxY() );
					gl2.glTexCoord2f( coords.right(), coords.top() );
					gl2.glVertex2d( bounds.getMaxX(), bounds.getMaxY() );
					gl2.glTexCoord2f( coords.right(), coords.bottom() );
					gl2.glVertex2d( bounds.getMaxX(), bounds.getMinY() );
					gl2.glTexCoord2f( coords.left(), coords.bottom() );
					gl2.glVertex2d( bounds.getMinX(), bounds.getMinY() );
					gl2.glEnd();
					framebufferTexture.disable(gl);
					gl2.glColor4f( 1.0f, 0.0f, 0.0f, 0.1f );
					gl2.glBegin( GL2.GL_QUADS );
					gl2.glVertex2d( bounds.getMinX(), bounds.getMaxY() );
					gl2.glVertex2d( bounds.getMaxX(), bounds.getMaxY() );
					gl2.glVertex2d( bounds.getMaxX(), bounds.getMinY() );
					gl2.glVertex2d( bounds.getMinX(), bounds.getMinY() );
					gl2.glEnd();
					framebufferTexture.disable(gl);
					// ShaderProgram.disable(gl);
				}
			}
			else
			{
				if (progress == 0.0)
				{
					if (first != null)
					{
						first.render(gl, bounds, offset, contentAlpha);
					}
				}
				else if (progress == 1.0)
				{
					if (second != null)
					{
						second.render(gl, bounds, offset, contentAlpha);
					}
				}
				else
				{
					transition.render(gl, bounds, firstTexture, secondTexture,
					        progress);
				}
			}
		}
	}

	private Texture pixelsToTexture(GL gl, Rectangle pixelBounds)
	{
		Texture texture = emptyTexture(pixelBounds.width, pixelBounds.height);
		pixelsToTexture(gl, pixelBounds, texture);
		return texture;
	}

	private void pixelsToTexture(GL gl, Rectangle pixelBounds, Texture texture)
	{
		texture.bind(gl);
		gl.glCopyTexImage2D(texture.getTarget(), 0, GL.GL_RGB, pixelBounds.x,
		        pixelBounds.y, pixelBounds.width, pixelBounds.height, 0);
	}

	private Rectangle project(GL gl, Rectangle2D bounds)
	{
		int[] viewport = new int[4];
		double[] modelViewMatrix = new double[16];
		double[] projectionMatrix = new double[16];

		gl.glGetIntegerv(GL.GL_VIEWPORT, viewport, 0);
		final GL2 gl2 = gl.getGL2();
		gl2.glGetDoublev(GL2.GL_MODELVIEW_MATRIX, modelViewMatrix, 0);
		gl2.glGetDoublev(GL2.GL_PROJECTION_MATRIX, projectionMatrix, 0);

		GLU glu = new GLU();
		double[] coordinates = new double[3];

		glu.gluProject(bounds.getMinX(), bounds.getMinY(), 0.0,
		        modelViewMatrix, 0, projectionMatrix, 0, viewport, 0,
		        coordinates, 0);

		int minX = (int) Math.round(coordinates[0]);
		int minY = (int) Math.round(coordinates[1]);

		glu.gluProject(bounds.getMaxX(), bounds.getMaxY(), 0.0,
		        modelViewMatrix, 0, projectionMatrix, 0, viewport, 0,
		        coordinates, 0);

		int maxX = (int) Math.round(coordinates[0]);
		int maxY = (int) Math.round(coordinates[1]);

		return new Rectangle(minX, minY, maxX - minX, maxY - minY);
	}

	private Texture emptyTexture(int width, int height)
	{
		ByteBuffer data = ByteBuffer.allocateDirect( width * height * 4 );
		data.limit(data.capacity());

		TextureData textureData = new TextureData(GLProfile.getGL2GL3(), GL.GL_RGBA, width, height, 0, GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, false, false, false, data, null);
		return TextureIO.newTexture(textureData);
	}
}