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

import java.awt.*;
import javax.swing.*;

import lithium.*;

/**
 * A list cell renderer for displaying a list of announcement presets.
 *
 * @version 0.9 (2006.02.27)
 * @author Gerrit Meinders
 */
public class AnnouncementCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        DefaultListCellRenderer component = (DefaultListCellRenderer)
                super.getListCellRendererComponent(list, value, index,
                isSelected, cellHasFocus);

        if (value instanceof Announcement) {
            Announcement announcement = (Announcement) value;
            component.setText(announcement.getName());
        } else if (value == null) {
            component.setText(Resources.get().getString("selectAnnouncement"));
        }

        return component;
    }
}
