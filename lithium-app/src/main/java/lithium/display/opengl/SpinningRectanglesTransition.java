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
import java.util.*;
import javax.media.opengl.*;

import com.github.meinders.common.math.*;
import com.jogamp.opengl.util.texture.*;

/**
 * TODO: class javadoc
 *
 * @author Gerrit Meinders
 */
public class SpinningRectanglesTransition implements GLTransitionRenderer {
	private long seed;

	public SpinningRectanglesTransition() {
		Random random = new Random();
		seed = random.nextLong();
	}

	public void render(GL gl, Rectangle2D bounds, Texture first,
	        Texture second, double progress) {

		final GL2 gl2 = gl.getGL2();
		gl2.glColor3d( 1.0, 1.0, 1.0 );

		if (progress < 0.5) {
			if (first != null) {
				Random random = new Random(seed);
				first.enable(gl);
				first.bind(gl);
				renderCheckers(gl, bounds, random, 2.0 * progress);
			}

		} else {
			if (second != null) {
				Random random = new Random(seed + 1);
				second.enable(gl);
				second.bind(gl);
				renderCheckers(gl, bounds, random, 2.0 - 2.0 * progress);
			}
		}
	}

	private void renderPlain(GL gl, Rectangle2D bounds, TextureCoords coords) {
		final GL2 gl2 = gl.getGL2();
		gl2.glBegin( GL2.GL_QUADS );
		gl2.glTexCoord2f( coords.left(), coords.bottom() );
		gl2.glVertex2d( bounds.getMinX(), bounds.getMaxY() );
		gl2.glTexCoord2f( coords.right(), coords.bottom() );
		gl2.glVertex2d( bounds.getMaxX(), bounds.getMaxY() );
		gl2.glTexCoord2f( coords.right(), coords.top() );
		gl2.glVertex2d( bounds.getMaxX(), bounds.getMinY() );
		gl2.glTexCoord2f( coords.left(), coords.top() );
		gl2.glVertex2d( bounds.getMinX(), bounds.getMinY() );
		gl2.glEnd();
	}

	private void renderCheckers(GL gl, Rectangle2D bounds, Random random,
	        double progress) {
		int n = 8;
		int m = 6;

		double width = bounds.getWidth() / n;
		double height = bounds.getHeight() / m;

		Vector3d vertex1 = new Vector3d(-0.5 * width, -0.5 * height, 0.0);
		Vector3d vertex2 = new Vector3d(-0.5 * width, 0.5 * height, 0.0);
		Vector3d vertex3 = new Vector3d(0.5 * width, 0.5 * height, 0.0);
		Vector3d vertex4 = new Vector3d(0.5 * width, -0.5 * height, 0.0);

		final GL2 gl2 = gl.getGL2();
		gl2.glPushMatrix();
		gl2.glTranslated(bounds.getX() - 0.5 * width, bounds.getY() - 0.5
		        * height, 0.0);

		for (int i = 0; i < n; i++) {
			gl2.glTranslated(width, 0.0, 0.0);
			for (int j = 0; j < m; j++) {
				gl2.glTranslated(0.0, height, 0.0);

				Vector3d axis = new Vector3d(1.0, 0.0, 0.0);
				axis = axis.rotateZ(2.0 * Math.PI * random.nextDouble());

				Matrix3d rotation = Matrix3d.rotationMatrix(axis, progress
				        * 0.5 * Math.PI);

				Vector3d rotated1 = rotation.multiply(vertex1);
				Vector3d rotated2 = rotation.multiply(vertex2);
				Vector3d rotated3 = rotation.multiply(vertex3);
				Vector3d rotated4 = rotation.multiply(vertex4);

				double minS = (double) i / (double) n;
				double minT = (double) j / (double) m;
				double maxS = (double) (i + 1) / (double) n;
				double maxT = (double) (j + 1) / (double) m;

				gl2.glBegin(GL2.GL_QUADS);
				gl2.glTexCoord2d(minS, minT);
				gl2.glVertex3d(rotated1.x, rotated1.y, rotated1.z);
				gl2.glTexCoord2d(minS, maxT);
				gl2.glVertex3d(rotated2.x, rotated2.y, rotated2.z);
				gl2.glTexCoord2d(maxS, maxT);
				gl2.glVertex3d(rotated3.x, rotated3.y, rotated3.z);
				gl2.glTexCoord2d(maxS, minT);
				gl2.glVertex3d(rotated4.x, rotated4.y, rotated4.z);
				gl2.glEnd();
			}
			gl2.glTranslated(0.0, -m * height, 0.0);
		}
		gl2.glPopMatrix();
	}
}
