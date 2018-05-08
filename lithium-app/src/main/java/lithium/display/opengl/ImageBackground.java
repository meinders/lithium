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
import java.net.*;
import java.util.concurrent.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.texture.*;
import lithium.*;
import lithium.display.*;

public class ImageBackground extends GLImageRenderer implements Background
{
	private GLView view;

	private boolean visible;

	private float top;

	private float bottom;

	public ImageBackground(GLView view, ExecutorService executor)
	{
		super(executor);
		this.view = view;
	}

	@Override
	public boolean isViewBackgroundVisible()
	{
		return false;
	}

	@Override
	public boolean accept(Object content)
	{
		return content == this;
	}

	@Override
	public PreparedContent prepare(Object content)
	{
		URL location = view.getConfig().getBackgroundImage();

		if (visible
		        && (location != null)
		        && (!view.isPreview() || view.getConfig().isBackgroundVisibleInPreview()))
		{
			return super.prepare(new ImageRef(location));
		}

		return new PreparedContent(null, null);
	}

	@Override
	public void render(GL gl, Rectangle2D bounds, Point2D offset, double alpha,
	        Object prepared)
	{
		FutureTexture future = (FutureTexture) prepared;
		Texture texture = (future == null) ? null : future.getTexture();

		Color backgroundColor;
		Color invisibleColor;

		backgroundColor = view.getConfig().getBackgroundColor();
		invisibleColor = backgroundColor.darker();

		backgroundColor = GLView.blend(view.isPreview() ? GLView.grayscale(
		        backgroundColor, 0.5f) : Color.BLACK, backgroundColor, alpha);
		invisibleColor = GLView.blend(view.isPreview() ? GLView.grayscale(
		        invisibleColor, 0.5f) : Color.BLACK, invisibleColor, alpha);

		GLView.setClearColor(gl, invisibleColor);
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);

		final GL2 gl2 = gl.getGL2();

		boolean textureVisible = visible && (texture != null);

		final int width = 1024;

		double top = this.top;
		double bottom = this.bottom;
		if (bounds.getX() == 0.0 && bounds.getY() == 0.0)
		{
			// FIXME properly support render to texture?
			top = this.top - this.bottom;
			bottom = 0.0;
		}

		{
			GLView.setColor(gl, backgroundColor);

			gl2.glBegin( GL2.GL_QUADS );
			gl2.glVertex2d( 0.0, bottom );
			gl2.glVertex2d( width, bottom );
			gl2.glVertex2d( width, top );
			gl2.glVertex2d( 0.0, top );
			gl2.glEnd();
		}

		if (textureVisible)
		{
			gl2.glColor4d(1.0, 1.0, 1.0, alpha);
			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

			texture.enable(gl);
			texture.bind(gl);

			TextureCoords textureCoords = texture.getImageTexCoords();

			gl2.glBegin(GL2.GL_QUADS);
			gl2.glTexCoord2f(textureCoords.left(), textureCoords.bottom());
			gl2.glVertex2d(0.0, bottom);
			gl2.glTexCoord2f(textureCoords.right(), textureCoords.bottom());
			gl2.glVertex2d(width, bottom);
			gl2.glTexCoord2f(textureCoords.right(), textureCoords.top());
			gl2.glVertex2d(width, top);
			gl2.glTexCoord2f(textureCoords.left(), textureCoords.top());
			gl2.glVertex2d(0.0, top);
			gl2.glEnd();

			texture.disable(gl);

			gl.glDisable(GL.GL_BLEND);
		}

		if (view.isPreview())
		{
			/*
			 * Visible area indicators at top and bottom.
			 */
			Color indicatorColor;
			if (visible)
			{
				indicatorColor = backgroundColor.brighter();
			}
			else
			{
				indicatorColor = GLView.grayscale(backgroundColor.brighter(),
				        0.5f);
			}

			double height = top - bottom;

			Rectangle2D margins = view.getConfig().getFullScreenMargins();
			double marginTop = height * (1.0 - margins.getMaxY());
			double marginBottom = height * margins.getMinY();
			double marginLeft = width * margins.getMinX();
			double marginRight = width * margins.getMaxX();

			GLView.setColor(gl, indicatorColor);

			// vertical screen bounds indicators
			gl2.glBegin(GL.GL_LINES);
			gl2.glVertex2d(0.0, top);
			gl2.glVertex2d(width, top);
			gl2.glVertex2d(0.0, bottom);
			gl2.glVertex2d(width, bottom);

			if (view.getConfig().isEnabled(Config.CONTENT_BOUNDS_INDICATOR))
			{
				// vertical content bounds indicators
				GLView.setColor(gl, Color.WHITE);
				gl2.glVertex2d(marginLeft / 2.0, top - marginTop);
				gl2.glVertex2d(marginLeft / 2.0, bottom + marginBottom);
				gl2.glVertex2d((marginRight + width) / 2.0, top - marginTop);
				gl2.glVertex2d((marginRight + width) / 2.0, bottom + marginBottom);
			}

			gl2.glEnd();
		}
	}

	public void setVisible(boolean visible)
	{
		this.visible = visible;
	}

	public void setTop(float top)
	{
		this.top = top;
	}

	public void setBottom(float bottom)
	{
		this.bottom = bottom;
	}
}