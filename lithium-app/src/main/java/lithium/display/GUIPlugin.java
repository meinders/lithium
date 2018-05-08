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
import javax.swing.*;

/**
 * An abstract base class for GUI plugins that adds start and dispose methods to
 * the JComponent class.
 *
 * @version 0.9x (2005.08.10)
 * @author Gerrit Meinders
 */
public abstract class GUIPlugin {
	protected GUIPlugin() {
		// No initialization needed.
	}

	public abstract SwingRenderer getSwingRenderer();

	public abstract GLRenderer getGLRenderer();

	/**
	 * The interface for GUI plugin renderers. The renderer provides a component
	 * to display whatever is appropriate for the plugin and is responsible for
	 * updating the component if and when necessary.
	 */
	public interface SwingRenderer {
		/**
		 * Returns the renderer component. Typically <code>this</code>.
		 *
		 * @return Renderer component.
		 */
		JComponent getComponent();
	}

	/**
	 * Interface for OpenGL-based GUI plugin renderers.
	 */
	public interface GLRenderer {
		void init(GL gl);

		void render(GL gl, Rectangle2D viewBounds);
	}
}
