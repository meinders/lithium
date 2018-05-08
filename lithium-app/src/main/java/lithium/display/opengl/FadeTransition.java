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
 * A transition where the start frame fades to fully transparent, revealing the
 * end frame.
 *
 * @author Gerrit Meinders
 */
public class FadeTransition implements GLTransitionRenderer {
	public void render(GL gl, Rectangle2D bounds, Texture first,
	                   Texture second, double progress) {

		final GL2 gl2 = gl.getGL2();
		gl2.glColor3d(1.0, 1.0, 1.0);

		if (first != null) {
			first.enable(gl);
			first.bind(gl);
			TextureCoords coords = first.getImageTexCoords();
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glTexCoord2f(coords.left(), coords.top());
			gl2.glVertex2d(bounds.getMinX(), bounds.getMaxY());
			gl2.glTexCoord2f(coords.right(), coords.top());
			gl2.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
			gl2.glTexCoord2f(coords.right(), coords.bottom());
			gl2.glVertex2d(bounds.getMaxX(), bounds.getMinY());
			gl2.glTexCoord2f(coords.left(), coords.bottom());
			gl2.glVertex2d(bounds.getMinX(), bounds.getMinY());
			gl2.glEnd();
			first.disable(gl);
		}

		if (second != null) {
			// TODO: check extension: GL_ARB_imaging

			gl.glEnable(GL.GL_BLEND);
			gl.glBlendFunc(GL2ES2.GL_CONSTANT_ALPHA, GL2ES2.GL_ONE_MINUS_CONSTANT_ALPHA);
			gl2.glBlendColor(0.0f, 0.0f, 0.0f, (float) progress);

			second.enable(gl);
			second.bind(gl);
			TextureCoords coords = second.getImageTexCoords();
			gl2.glBegin(GL2.GL_QUADS);
			gl2.glTexCoord2f(coords.left(), coords.top());
			gl2.glVertex2d(bounds.getMinX(), bounds.getMaxY());
			gl2.glTexCoord2f(coords.right(), coords.top());
			gl2.glVertex2d(bounds.getMaxX(), bounds.getMaxY());
			gl2.glTexCoord2f(coords.right(), coords.bottom());
			gl2.glVertex2d(bounds.getMaxX(), bounds.getMinY());
			gl2.glTexCoord2f(coords.left(), coords.bottom());
			gl2.glVertex2d(bounds.getMinX(), bounds.getMinY());
			gl2.glEnd();
			second.disable(gl);

			gl.glDisable(GL.GL_BLEND);
		}
	}
}
