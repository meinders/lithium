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

import javax.swing.*;

import lithium.*;

/**
 * A combo box model containing all currently available announcement presets.
 *
 * @since 0.9
 * @version 0.9 (2006.03.14)
 * @author Gerrit Meinders
 */
public class AnnouncementComboBoxModel extends DefaultComboBoxModel {
    /** Constructs a new announcement combo box model. */
    public AnnouncementComboBoxModel() {
        Config config = ConfigManager.getConfig();
        addElement(null);
        for (Announcement announcement : config.getAnnouncementPresets()) {
            addElement(announcement);
        }
        // TODO: register listener for when config changes
    }
}

