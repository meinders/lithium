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

package lithium.display.java2d;

import java.awt.event.*;
import javax.swing.plaf.basic.*;

import static lithium.display.MouseWheelHandler.*;

/**
 * A special ScrollPaneUI used by the LyricView to provide mouse wheel scrolling
 * even in the absence of a scroll bar.
 *
 * @since 0.9
 * @version 0.9 (2006.02.23)
 * @author Gerrit Meinders
 */
public class LyricViewScrollPaneUI extends BasicScrollPaneUI {
    /**
     * Creates an instance of MouseWheelListener, which is added to the
     * JScrollPane by installUI(). The returned MouseWheelListener is used to
     * handle mouse wheel-driven scrolling.
     *
     * @return MouseWheelListener which implements wheel-driven scrolling
     */
    protected MouseWheelListener createMouseWheelListener() {
        return new lithium.display.MouseWheelHandler(
                scrollpane, SCROLL_ALWAYS);
    }
}

