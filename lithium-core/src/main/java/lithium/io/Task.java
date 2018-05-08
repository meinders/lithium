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

package lithium.io;

import java.beans.*;
import java.util.concurrent.*;

/**
 * An interface for tasks that may be run asynchronously.
 *
 * @version 0.9 (2006.02.21)
 * @author Gerrit Meinders
 */
public interface Task<T> extends Runnable, Future<T> {
    /**
     * Returns the current progress of the task.
     *
     * @return the progress, as an integer in the range from 0 to 100
     */
    public int getProgress();

    /**
     * Sets the progress of the task.
     *
     * @param progress the progress to be set, in the range from 0 to 100
     */
    public void setProgress(int progress);

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener);
}

