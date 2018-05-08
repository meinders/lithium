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

import javax.swing.event.*;

import lithium.animation.legacy.*;


/**
 * The scroller interface defines an interface for scroll models. Model
 * implementations can vary the way in which the value shifts towards the
 * target. This is accomplished by using both a value and a target property,
 * in stead of just a value.
 * <p>
 * An implementation of this interface could, for example, change the value
 * smoothly to the target value in a few seconds.
 *
 * @version 0.9x (2005.08.03)
 * @author Gerrit Meinders
 */
public interface Scroller extends Startable, Disposable {
    /**
     * Adds the given listener to the scroller.
     *
     * @param l the listener
     */
    public void addChangeListener(ChangeListener l);

    /**
     * Removes the given listener from the scroller.
     *
     * @param l the listener
     */
    public void removeChangeListener(ChangeListener l);

    /**
     * Returns the current value of the scroller.
     *
     * @return the current value
     */
    public float getValue();

    /**
     * Sets the value of the scroller. Calling this method circumvents the
     * customized scrolling method of the scroller and sets the scrollers value
     * immediately. To use the scroller's custom method, use
     * {@link #setTarget(float)}.
     *
     * @param value the value
     */
    public void setValue(float value);

    /**
     * Returns the target value of the scroller.
     *
     * @return the target value
     */
    public float getTarget();

    /**
     * Sets the target value of the scroller to the given value.
     *
     * @param target the target value
     */
    public void setTarget(float target);

    /**
     * Sets the target value of the scroller to the given value. If
     * addTargetDistance is set to <code>true</code>, the current target
     * distance will be added to the given target value. The target distance is
     * defined as the difference between the target value and the current value.
     *
     * @param target the target value
     * @param addTargetDistance if <code>true</code>, the current target distance is added to the given target value
     */
    public void setTarget(float target, boolean addTargetDistance);
}

