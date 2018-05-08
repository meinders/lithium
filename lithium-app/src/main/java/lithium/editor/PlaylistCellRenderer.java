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
import javax.swing.table.*;

import lithium.*;

/**
 * A list cell renderer for displaying LyricRef values.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class PlaylistCellRenderer implements ListCellRenderer,
        TableCellRenderer {
	/** Serial version UID */
	private static final long serialVersionUID = 1L;

	private static final DefaultListCellRenderer LIST_RENDERER = new DefaultListCellRenderer();

	private static final DefaultTableCellRenderer TABLE_RENDERER = new DefaultTableCellRenderer();

	public Component getListCellRendererComponent(JList list, Object value,
	        int index, boolean isSelected, boolean cellHasFocus) {
		return LIST_RENDERER.getListCellRendererComponent(list,
		        getDisplayText(value), index, isSelected, cellHasFocus);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
	        boolean isSelected, boolean hasFocus, int row, int column) {
		return TABLE_RENDERER.getTableCellRendererComponent(table,
		        getDisplayText(value), isSelected, hasFocus, row, column);
	}

	protected String getDisplayText(Object value) {
		final String result;

		if (value == null) {
			result = Resources.get().getLabel("playlist.nullValue");

		} else if (value instanceof PlaylistItem) {
			result = ((PlaylistItem) value).getTitle();

		} else {
			final String text = value.toString();
			final int endOfLine = text.indexOf('\n');
			result = endOfLine == -1 ? text : text.substring(0, endOfLine);
		}

		return result;
	}
}
