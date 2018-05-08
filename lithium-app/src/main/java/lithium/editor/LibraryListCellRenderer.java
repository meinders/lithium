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

import java.net.*;

import lithium.books.*;

/**
 * Renders cells containing a URL to an {@link ArchiveLibrary} using the
 * collection's title.
 *
 * @author Gerrit Meinders
 */
public class LibraryListCellRenderer extends URLCellRenderer {
    @Override
    protected Object getDisplayValue(Object value) {
        Object result;
        if (value instanceof URL) {
            URL url = (URL) value;
            try {
                final ArchiveLibrary collection = new ArchiveLibrary(url
                        .toURI());
                result = collection.getName();
            } catch (Exception e) {
                result = super.getDisplayValue(value);
            }
        } else {
            result = super.getDisplayValue(value);
        }
        return result;
    }
}
