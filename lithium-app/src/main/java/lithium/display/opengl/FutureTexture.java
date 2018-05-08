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

import java.io.*;
import java.net.*;
import java.util.concurrent.*;
import javax.media.opengl.*;

import com.jogamp.opengl.util.texture.*;

/**
 * Provides access to an OpenGL {@link Texture} from texture data that is loaded
 * asynchronously. This allows for smooth animation to continue while the
 * texture is being loaded.
 *
 * <p>
 * For the texture data to be loaded, the task must be submitted to an
 * {@link ExecutorService} or executed by some other means. As soon as the
 * texture data is loaded, calls to {@link #getTexture()} will return a texture
 * object.
 *
 * @author Gerrit Meinders
 */
public class FutureTexture extends FutureTask<TextureData>
{
	/**
	 * OpenGL texture, or <code>null</code> if not available yet.
	 */
	private Texture texture;

	/**
	 * Object from which texture data is loaded. (Kept for debugging purposes.)
	 */
	private Object source;

	/**
	 * Constructs a new task for loading a texture from the given file.
	 *
	 * @param file the file to read texture data from
	 * @param mipmap whether mipmaps should be produced for this texture
	 */
	public FutureTexture(final File file, final boolean mipmap)
	{
		super(new Callable<TextureData>()
		{
			@Override
			public TextureData call() throws Exception
			{
				return TextureIO.newTextureData( GLProfile.getGL2GL3(), file, mipmap, null );
			}
		});
		source = file;
	}

	/**
	 * Constructs a new task for loading a texture from the given URL.
	 *
	 * @param url the URL to read texture data from
	 * @param mipmap whether mipmaps should be produced for this texture
	 */
	public FutureTexture(final URL url, final boolean mipmap)
	{
		super(new Callable<TextureData>()
		{
			@Override
			public TextureData call() throws Exception
			{
				return TextureIO.newTextureData( GLProfile.getGL2GL3(), url, mipmap, null );
			}
		});
		source = url;
	}

	/**
	 * Returns the texture, if it's been loaded. This method must be called from
	 * the OpenGL thread, to allow for the creation of a {@link Texture} object.
	 *
	 * @return the texture, or <code>null</code> if not yet available
	 *
	 * @throws GLException if no OpenGL context is current or if an OpenGL error
	 *             occurred while creating a {@link Texture} object
	 */
	public Texture getTexture() throws GLException
	{
		if (!Threading.isOpenGLThread())
		{
			throw new IllegalStateException("must be called from OpenGL thread");
		}

		Texture texture = this.texture;
		if (texture == null)
		{
			if (isDone())
			{
				try
				{
					TextureData textureData = get();
					texture = TextureIO.newTexture(textureData);
					textureData.flush();
				}
				catch (InterruptedException e)
				{
					// Task is already done.
					throw new AssertionError(e);
				}
				catch (ExecutionException e)
				{
					e.printStackTrace();
				}
				this.texture = texture;
			}
		}
		return texture;
	}

	/**
	 * Clears the resources used by the texture.
	 */
	public void flush()
	{
		if (isDone())
		{
			try
			{
				final TextureData textureData = get();
				if (textureData != null)
				{
					textureData.flush();
				}
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			catch (ExecutionException e)
			{
				e.printStackTrace();
			}
		}

		if (texture != null)
		{
			texture.destroy( GLContext.getCurrentGL() );
			texture = null;
		}
	}

	@Override
	public String toString()
	{
		return super.toString() + "[" + source + "]";
	}
}
