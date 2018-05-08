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

import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 * A plain scroller implementation that keeps target and value equal at all
 * times, resulting in instant scrolling.
 *
 * @version 0.9x (2005.10.15)
 * @author Gerrit Meinders
 */
public class PlainScroller implements Scroller {
    private float value = 0f;

    private Set<ChangeListener> changeListeners = new HashSet<ChangeListener>();

    /**
     * Constructs a new plain scroller.
     */
    public PlainScroller() {
    }

    /** @see lithium.animation.legacy.Startable#start() */
    public void start() {
    }

    /** @see lithium.animation.legacy.Disposable#dispose() */
    public void dispose() {
    }

    public void addChangeListener(ChangeListener l) {
        changeListeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        changeListeners.remove(l);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        float oldValue = this.value;
        this.value = value;
        if (value != oldValue) {
            fireChangeEvent();
        }
    }

    public float getTarget() {
        return getValue();
    }

    public void setTarget(float target) {
        setTarget(target, false);
    }

    public void setTarget(float target, boolean addTargetDistance) {
        setValue(target);
    }

    private void fireChangeEvent() {
        assert SwingUtilities.isEventDispatchThread() :
                "Must be called from event dispatch thread";
        for (ChangeListener l : changeListeners) {
            l.stateChanged(new ChangeEvent(this));
        }
    }
}

