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

import static lithium.Config.*;

/**
 * A list model containing all currently available announcement presets.
 *
 * @since 0.9
 * @version 0.9 (2006.02.27)
 * @author Gerrit Meinders
 */
public class AnnouncementListModel extends AbstractListModel implements
        PropertyChangeListener {
    /** The configuration settings on which the model is based. */
    private Config config;

    /** The announcement presets in the list. */
    private List<Announcement> announcements;

    /**
     * Constructs a new announcement list model based on the active
     * configuration.
     */
    public AnnouncementListModel() {
        this(ConfigManager.getConfig());
    }

    /**
     * Constructs a new announcement list model based on the given
     * configuration.
     *
     * @param config the configuration settings
     */
    public AnnouncementListModel(Config config) {
        announcements = new ArrayList<Announcement>();
        setConfig(config);
    }

    /**
     * Sets the configuration settings used by the model.
     *
     * @param config the configuration settings
     */
    public void setConfig(final Config config) {
        if (this.config != null) {
            this.config.removePropertyChangeListener(this);
        }
        this.config = config;
        if (!announcements.isEmpty()) {
            fireIntervalRemoved(this, 0, announcements.size() - 1);
        }
        announcements.clear();
        if (config != null) {
            announcements.addAll(config.getAnnouncementPresets());
            config.addPropertyChangeListener(this);
        }
        if (!announcements.isEmpty()) {
            fireIntervalAdded(this, 0, announcements.size() - 1);
        }
    }

    /**
     * Returns the value at the specified index.
     *
     * @param index the index
     * @return the value at the given index
     */
    public Announcement getElementAt(int index) {
        return announcements.get(index);
    }

    /**
     * Returns the length of the list.
     *
     * @return the length of the list
     */
    public int getSize() {
        return announcements.size();
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() == ANNOUNCEMENT_PRESETS_PROPERTY) {
            setConfig(config);
        }
    }
}
