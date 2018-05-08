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

import javax.swing.*;
import javax.swing.event.*;

import lithium.animation.legacy.*;


/**
 * <p>
 * This class implements a bounded range model, typically used by scroll bars,
 * that transparently converts the actual units used by a text component to
 * logical units, used by Scrollers, and back.
 *
 * <p>
 * The conversion is based on the text content of the component and maps units
 * in such a way, that a logical unit is exactly one line of text in the
 * component's model (as opposed to the component's view, which may introduce
 * additional line breaks).
 *
 * @see Scroller
 *
 * @version 0.9x (2005.08.04)
 * @author Gerrit Meinders
 */
public class AdvancedTextRangeModel extends DefaultBoundedRangeModel implements
        Disposable {
    private Scroller scroller;

    private ConversionStrategy strategy;

    /**
     * Constructs a new model that ties into the given scroller and performs
     * coordinate conversions using the given strategy.
     *
     * @param scroller the scroller
     * @param strategy the conversion strategy
     */
    public AdvancedTextRangeModel(Scroller scroller, ConversionStrategy strategy) {
        super();

        this.scroller = scroller;
        this.strategy = strategy;

        scroller.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                // keep the model synchronized to the scroller
                updateValue();
            }
        });
    }

    /**
     * Sets the conversion strategy to the given strategy.
     *
     * @param strategy the conversion strategy
     */
    public void setStrategy(ConversionStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Updates the model's actual value (returned by getValue) to match the
     * scroller's current value.
     */
    private void updateValue() {
        super.setValue(strategy.modelToView(scroller.getValue()));
    }

    /**
     * Sets the target of the model's associated scroller to the given value
     * converted into logical coordinates.
     *
     * @param value the value to set, in the view's units
     */
    @Override
    public void setValue(int value) {
        scroller.setTarget(strategy.viewToModel(value), true);
    }

    public void dispose() {
        strategy.dispose();
    }
}
