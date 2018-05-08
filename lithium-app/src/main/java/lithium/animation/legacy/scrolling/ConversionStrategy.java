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

package lithium.animation.legacy.scrolling;

import lithium.animation.legacy.*;

/**
 * A conversion strategy defines a mapping between view and (scroll) model
 * coordinates, such that smooth scrolling can be achieved at the appropriate
 * rate.
 *
 * @see AdvancedTextRangeModel
 *
 * @version 0.9x (2005.08.04)
 * @author Gerrit Meinders
 */
public interface ConversionStrategy extends Disposable {
	/**
	 * Converts the given logical value to the text component's units.
	 *
	 * @param value scroll value in logical units
	 */
	public int modelToView(float value);

	/**
	 * Converts the given value in the text component's units to logical units.
	 *
	 * @param value scroll value in view's units
	 */
	public float viewToModel(int value);

	/**
	 * Sets whether the top margin should be ignored.
	 */
	public void setTopMarginFixed(boolean topMarginFixed);
}
