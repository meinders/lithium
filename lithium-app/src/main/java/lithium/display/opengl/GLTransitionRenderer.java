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

import com.jogamp.opengl.util.texture.*;

/**
 * Provides an interface for OpenGL-based transition renderers, using the JOGL
 * API.
 *
 * @author Gerrit Meinders
 */
public interface GLTransitionRenderer {
	/**
	 * Renders a transition between the given textures.
	 *
	 * @param gl the interface to OpenGL
	 * @param bounds the bounds of the visible area
	 * @param start the texture for the start of the transition
	 * @param end the texture for the end of the transition
	 * @param progress the progress of the transition, ranging from
	 *            <code>0.0</code> to <code>1.0</code>
	 */
	void render(GL gl, Rectangle2D bounds, Texture start, Texture end,
	        double progress);
}
