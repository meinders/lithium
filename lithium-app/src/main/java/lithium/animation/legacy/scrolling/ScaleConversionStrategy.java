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

import java.awt.*;

/**
 * <p>
 * This class provides a conversion strategy that converts view coordinates
 * relative to the views width. This assumes that all views are exactly
 * proportional to eachother. Otherwise, the content in different views will be
 * misaligned.
 *
 * @see AdvancedTextRangeModel
 *
 * @version 0.9x (2005.08.05)
 * @author Gerrit Meinders
 */
public class ScaleConversionStrategy implements ConversionStrategy {
    /** The width of a component with a scale 1.0f. */
    private static final float REFERENCE_WIDTH = 20f;

    /** The component being used as a view. */
    private Component view = null;

    /** The current scale of the view. */
    private float scale;

    /** Whether the top margin of the view should be fixed. */
    private boolean topMarginFixed = true;

    /**
     * Constructs a new conversion strategy without an associated view. A view
     * has to be set on the strategy before it is used.
     */
    public ScaleConversionStrategy() {
    }

    /**
     * Constructs a new conversion strategy for the given view.
     *
     * @param view the view
     */
    public ScaleConversionStrategy(Component view) {
        setView(view);
    }

    /**
     * Sets the view used to take measurements for unit conversion.
     *
     * @param view the view
     */
    private void setView(Component view) {
        this.view = view;
    }

    /**
     * Converts the given logical value to the view's units.
     *
     * @param value scroll value in logical units
     */
    public int modelToView(float value) {
        updateScale();
        return (int) (value * scale);
    }

    /**
     * Converts the given value in the view's units to logical units.
     *
     * @param value scroll value in view's units
     */
    public float viewToModel(int value) {
        updateScale();
        return value / scale;
    }

    private void updateScale() {
        System.out.println(scale);
        scale = view.getWidth() / REFERENCE_WIDTH;
    }

    public void dispose() {
        view = null;
    }

    public void setTopMarginFixed(boolean topMarginFixed) {
        this.topMarginFixed = topMarginFixed;
    }
}
