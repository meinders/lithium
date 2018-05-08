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

import java.awt.geom.*;
import java.awt.image.*;
import java.net.*;
import java.util.concurrent.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.texture.*;
import com.jogamp.opengl.util.texture.awt.*;
import lithium.*;
import lithium.display.*;

public class GLImageRenderer implements GLContentRenderer
{
	private final ExecutorService executor;

	public GLImageRenderer(ExecutorService executor)
	{
		this.executor = executor;
	}

	@Override
	public boolean isViewBackgroundVisible()
	{
		return false;
	}

	@Override
	public boolean accept(Object content)
	{
		return content instanceof ImageRef || content instanceof BufferedImage;
	}

	@Override
	public PreparedContent prepare(Object content)
	{
		PreparedContent result = null;

		if (content instanceof ImageRef)
		{
			ImageRef imageContent = (ImageRef) content;
			URL source = imageContent.getSource();
			try
			{
				FutureTexture futureTexture = new FutureTexture(source, false);
				executor.submit(futureTexture);
				result = new PreparedContent(futureTexture, new Flusher()
				{
					@Override
					public void flush( Object object )
					{
						((FutureTexture)object).flush();
					}
				} );
			}
			catch (GLException e)
			{
				e.printStackTrace();
			}

		}
		else if (content instanceof BufferedImage)
		{
			Texture texture = AWTTextureIO.newTexture( GLProfile.getGL2GL3(), (BufferedImage)content, false );
			result = new PreparedContent(texture, new Flusher()
			{
				@Override
				public void flush( Object object )
				{
					((Texture)object).destroy( GLContext.getCurrentGL() );
				}
			} );
		}
		else
		{
			throw new IllegalArgumentException("content");
		}

		return result;
	}

	@Override
	public boolean ready(Object prepared)
	{
		return (prepared instanceof Texture)
		        || ((FutureTexture) prepared).isDone();
	}

	@Override
	public void render(GL gl, Rectangle2D bounds, Point2D offset, double alpha,
	        Object prepared)
	{

		Texture texture = null;

		if (prepared instanceof Texture)
		{
			texture = (Texture) prepared;
		}
		else if (prepared instanceof FutureTexture)
		{
			texture = ((FutureTexture) prepared).getTexture();
		}

		if (texture != null)
		{
			Rectangle2D imageBounds = Shapes.innerRectangle(bounds,
			        texture.getAspectRatio());

			final GL2 gl2 = gl.getGL2();
			gl2.glColor4d(1.0, 1.0, 1.0, alpha);
			gl.glEnable(GL2.GL_BLEND);
			gl.glBlendFunc(GL2ES2.GL_SRC_ALPHA, GL2ES2.GL_ONE_MINUS_SRC_ALPHA);

			texture.enable(gl);
			texture.bind(gl);

			TextureCoords coords = texture.getImageTexCoords();
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glTexCoord2f(coords.left(), coords.top());
			gl2.glVertex2d(imageBounds.getMinX(), imageBounds.getMaxY());
			gl2.glTexCoord2f(coords.right(), coords.top());
			gl2.glVertex2d(imageBounds.getMaxX(), imageBounds.getMaxY());
			gl2.glTexCoord2f(coords.right(), coords.bottom());
			gl2.glVertex2d(imageBounds.getMaxX(), imageBounds.getMinY());
			gl2.glTexCoord2f(coords.left(), coords.bottom());
			gl2.glVertex2d(imageBounds.getMinX(), imageBounds.getMinY());
			gl2.glEnd();

			texture.disable(gl);

			gl.glDisable(GL.GL_BLEND);
		}
	}
}
