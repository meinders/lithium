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
 * An extension of the {@link FutureTask} that defines additional feedback
 * mechanisms
 *
 * @version 0.9 (2006.03.11)
 * @author Gerrit Meinders
 */
public class MonitoredTask<T> extends FutureTask<T> implements Task<T> {
    /** Provides support for bounds properties. */
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** The current progress of the task, in the range from 0 to 100. */
    private int progress = 0;

    /** Indicates whether the progress of the task is indeterminate. */
    private boolean indeterminate = true;

    /**
     * Constructs a new task from the given callable.
     *
     * @param callable the callable
     */
    protected MonitoredTask(Callable<T> callable) {
        super(callable);
    }

    /**
     * Fires a property change of the "done" property when the task completes.
     */
    protected void done() {
        pcs.firePropertyChange("done", false, true);
    }

    /**
     * Add a PropertyChangeListener to the listener list.
     *
     * @param listener the PropertyChangeListener to be added
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    /**
     * Removes a PropertyChangeListener from the listener list.
     *
     * @param listener the PropertyChangeListener to be removed
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    /**
     * Returns the current progress of the task.
     *
     * @return the progress, as an integer in the range from 0 to 100
     */
    public int getProgress() {
        return progress;
    }

    /**
     * Sets the progress of the task and sets the task to not be indeterminate.
     *
     * @param progress the progress to be set, in the range from 0 to 100
     */
    public void setProgress(int progress) {
        if (this.progress != progress) {
            int oldValue = this.progress;
            this.progress = progress;
            if (isIndeterminate()) {
                setIndeterminate(false);
            }
            pcs.firePropertyChange("progress", oldValue, progress);
        }
    }

    /**
     * Returns whether the progress of the task is indeterminate.
     *
     * @return {@code true} if the progress of the task is indeterminate;
     *         {@code false} otherwise
     */
    public boolean isIndeterminate() {
        return indeterminate;
    }

    /**
     * Sets whether the progress of the task is indeterminate.
     *
     * @param indeterminate the new value of the indeterminate property
     */
    public void setIndeterminate(boolean indeterminate) {
        if (this.indeterminate != indeterminate) {
            this.indeterminate = indeterminate;
            pcs.firePropertyChange("indeterminate", !indeterminate,
                    indeterminate);
        }
    }
}

