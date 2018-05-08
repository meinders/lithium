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
import javax.media.opengl.*;

import lithium.display.*;

/**
 * Provides an interface for OpenGL-based content renderers, using the JOGL API.
 *
 * @author Gerrit Meinders
 */
public interface GLContentRenderer extends ContentRenderer {
	/**
	 * Renders the content to the given OpenGL interface.
	 *
	 * @param gl the interface to OpenGL
	 * @param bounds the rectangle that the content should be rendered inside,
	 *            in view space
	 * @param offset the offset applied to the content before rendering it; e.g.
	 *            the y-coordinate may indicate the amount that the content is
	 *            scrolled vertically
	 * @param prepared the result of the {@link #prepare} method for the content
	 *            to be rendered
	 */
	void render(GL gl, Rectangle2D bounds, Point2D offset, double alpha,
	        Object prepared);
}
