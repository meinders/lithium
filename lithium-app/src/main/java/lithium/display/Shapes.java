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

public class Shapes {
	/**
	 * Scales a rectangle to fit exactly inside another, preserving the
	 * rectangle's aspect ratio. The resulting rectangle is centered on the
	 * target rectangle.
	 *
	 * @param target the target rectangle that the result must fit
	 * @param sourceAspect the aspect ratio of the rectangle to be scaled
	 *
	 * @return A rectangle with the given aspect ratio that fits exactly inside
	 *         the given target rectangle.
	 */
	public static Rectangle2D innerRectangle(Rectangle2D target,
	        double sourceAspect) {
		double targetAspect = target.getWidth() / target.getHeight();
		if (sourceAspect == targetAspect) {
			return target;

		} else if (sourceAspect < targetAspect) {
			double width = target.getHeight() * sourceAspect;
			double x = target.getX() + 0.5 * (target.getWidth() - width);
			return new Rectangle2D.Double(x, target.getY(), width,
			        target.getHeight());

		} else {
			double height = target.getWidth() / sourceAspect;
			double y = target.getY() + 0.5 * (target.getHeight() - height);
			return new Rectangle2D.Double(target.getX(), y, target.getWidth(),
			        height);
		}
	}

	private Shapes() {
	}
}
