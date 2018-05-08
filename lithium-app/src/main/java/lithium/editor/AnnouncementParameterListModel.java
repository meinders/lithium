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

package lithium.editor;

import java.beans.*;
import java.util.*;
import javax.swing.*;

import lithium.*;
import lithium.Announcement.*;

import static lithium.Announcement.*;

/**
 * A list model containing the parameters of a single announcement preset.
 *
 * @since 0.9
 * @version 0.9 (2006.08.25)
 * @author Gerrit Meinders
 */
public class AnnouncementParameterListModel extends AbstractListModel implements
        PropertyChangeListener {
    /** The announcement preset on which the model is based. */
    private Announcement announcement;

    /** The announcement presets in the list. */
    private List<Parameter> parameters;

    /**
     * Constructs a new parameter list model for the given announcement preset.
     *
     * @param announcement the announcement preset
     */
    public AnnouncementParameterListModel(Announcement announcement) {
        parameters = new ArrayList<Parameter>();
        setAnnouncement(announcement);
    }

    /**
     * Sets the announcement to get parameters from.
     *
     * @param announcement the announcement
     */
    public void setAnnouncement(final Announcement announcement) {
        if (this.announcement != null) {
            this.announcement.removePropertyChangeListener(this);
        }
        if (parameters.size() > 0) {
            fireIntervalRemoved(this, 0, parameters.size() - 1);
        }
        this.announcement = announcement;
        parameters.clear();
        if (announcement != null) {
            parameters.addAll(announcement.getParameters());
            announcement.addPropertyChangeListener(this);
        }
        if (parameters.size() > 0) {
            fireIntervalAdded(this, 0, parameters.size() - 1);
        }
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the index
     * @return the value at the given index
     */
    public Parameter getElementAt(int index) {
        return parameters.get(index);
    }

    /**
     * Returns the length of the list.
     *
     * @return the length of the list
     */
    public int getSize() {
        return parameters.size();
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() == PARAMETERS_PROPERTY) {
            setAnnouncement(announcement);
        }
    }
}
