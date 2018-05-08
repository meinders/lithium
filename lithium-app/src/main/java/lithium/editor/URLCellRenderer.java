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
import java.io.*;
import java.net.*;
import javax.swing.*;

/**
 * Renders cells containing URL values in decoded format. If a URL refers to a
 * file, the representation defined by {@link File#toString()} is used.
 *
 * @author Gerrit Meinders
 */
public class URLCellRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        return super.getListCellRendererComponent(list, getDisplayValue(value),
                index, isSelected, cellHasFocus);
    }

    /**
     * Creates a human-readable representation to be displayed for the given
     * value.
     *
     * @param value the value to be displayed
     * @return the display value
     */
    protected Object getDisplayValue(Object value) {
        Object result;
        try {
            final URI uri = ((URL) value).toURI();
            if ("file".equals(uri.getScheme())) {
                result = new File(uri.getPath());
            } else {
                result = uri;
            }
        } catch (URISyntaxException e) {
            result = value;
        }
        return result;
    }
}
