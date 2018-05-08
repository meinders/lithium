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

import java.awt.event.*;
import javax.swing.*;

/**
 * <p>A mouse wheel handler that provides for mouse wheel scrolling even when
 * the scroll bar used is invisible.
 *
 * <p>Note: the implementation of this class borrows some code from the
 * BasicScrollPaneUI version 1.4 or 1.5, which wasn't exactly designed for
 * extension.
 *
 * @version 0.9 (2006.02.23)
 * @author Gerrit Meinders
 */
public class MouseWheelHandler implements MouseWheelListener {
    /** Scroll when the scroll bar is visible only. */
    public static int SCROLL_WHEN_VISIBLE = 1;

    /** Scroll when the scroll bar is invisible only. */
    public static int SCROLL_WHEN_INVISIBLE = 2;

    /** Scroll regardless of scroll bar visibility. */
    public static int SCROLL_ALWAYS = SCROLL_WHEN_VISIBLE |
            SCROLL_WHEN_INVISIBLE;

    private JScrollPane scrollPane;
    private int behavior;

    public MouseWheelHandler(JScrollPane scrollPane, int behavior) {
        this.scrollPane = scrollPane;
        this.behavior = behavior;
    }

    public void mouseWheelMoved(MouseWheelEvent e) {
        if (!e.getComponent().isEnabled()) {
            return;
        }
        if (scrollPane.isWheelScrollingEnabled() &&
                e.getScrollAmount() != 0) {
            JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
            if (scrollBar == null || !scrollBar.isVisible()) {
                if ((behavior & SCROLL_WHEN_INVISIBLE) !=
                        SCROLL_WHEN_INVISIBLE) {
                    return;
                }
            }

            int direction = 0;
            direction = e.getWheelRotation() < 0 ? -1 : 1;

            if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                scrollByUnits(scrollBar, direction, e.getScrollAmount());
            } else if (e.getScrollType() ==
                MouseWheelEvent.WHEEL_BLOCK_SCROLL) {
                scrollByBlock(scrollBar, direction);
            }
        }
    }

    private static void scrollByBlock(JScrollBar scrollBar, int direction) {
        int oldValue = scrollBar.getValue();
        int blockIncrement = scrollBar.getBlockIncrement(direction);
        int delta = blockIncrement * ((direction > 0) ? 1 : -1);

        scrollBar.setValue(oldValue + delta);
    }

    private static void scrollByUnits(JScrollBar scrollBar, int direction,
            int units) {
        int delta = units;

        if (direction > 0) {
            delta *= scrollBar.getUnitIncrement(direction);
        }
        else {
            delta *= -scrollBar.getUnitIncrement(direction);
        }

        int oldValue = scrollBar.getValue();
        int newValue = oldValue + delta;

        if (delta > 0 && newValue < oldValue) {
            newValue = scrollBar.getMaximum();
        } else if (delta < 0 && newValue > oldValue) {
            newValue = scrollBar.getMinimum();
        }
        scrollBar.setValue(newValue);
    }
}

