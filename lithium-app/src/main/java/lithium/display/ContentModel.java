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

import java.awt.geom.*;
import javax.media.opengl.*;

import lithium.display.opengl.*;

/**
 * Keeps track of content along with an optimized version prepared for a
 * specific content renderer.
 *
 * @author Gerrit Meinders
 */
public class ContentModel
{
	private Object content;

	private PreparedContent preparedContent;

	private GLContentRenderer renderer;

	public ContentModel(Object content, PreparedContent preparedContent,
	        GLContentRenderer renderer)
	{
		super();

		if (content == null)
		{
			if (renderer != null)
			{
				throw new IllegalArgumentException("renderer without content");
			}
			else if (preparedContent != null)
			{
				throw new IllegalArgumentException(
				        "preparedContent without content");
			}
		}
		else if (renderer == null)
		{
			throw new NullPointerException("renderer");
		}
		else if (preparedContent == null)
		{
			throw new NullPointerException("preparedContent");
		}

		this.content = content;
		this.preparedContent = preparedContent;
		this.renderer = renderer;
	}

	public Object getContent()
	{
		return content;
	}

	public PreparedContent getPreparedContent()
	{
		if ((renderer == null) || (preparedContent == null)
		        || preparedContent.isValid())
		{
			return preparedContent;
		}
		else
		{
			flush();
			preparedContent = renderer.prepare(content);
			return preparedContent;
		}
	}

	public GLContentRenderer getRenderer()
	{
		return renderer;
	}

	public boolean ready()
	{
		return renderer == null ? false : renderer.ready(preparedContent);
	}

	public void render(GL gl, Rectangle2D bounds, Point2D offset, double alpha)
	{
		if (renderer != null)
		{
			Object preparedContent = getPreparedContent().getValue();
			renderer.render(gl, bounds, offset, alpha, preparedContent);
		}
	}

	public void flush()
	{
		PreparedContent preparedContentWrapper = this.preparedContent;
		if (preparedContentWrapper != null)
		{
			preparedContentWrapper.flush();
		}
	}

	public boolean isViewBackgroundVisible()
	{
		return (renderer == null) || renderer.isViewBackgroundVisible();
	}
}