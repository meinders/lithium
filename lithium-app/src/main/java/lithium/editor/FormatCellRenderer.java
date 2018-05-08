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
import java.text.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 * Cell renderer that formats each value using the specified {@link Format}.
 *
 * @author Gerrit Meinders
 */
public class FormatCellRenderer extends DefaultTableCellRenderer {
    private final Format format;

    /**
     * Constructs a new cell renderer using the specified format.
     *
     * @param format
     */
    public FormatCellRenderer(Format format) {
        this.format = format;
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
            boolean isSelected, boolean hasFocus, int row, int column) {
        return super.getTableCellRendererComponent(table,
                value instanceof String ? value : format.format(value),
                isSelected, hasFocus, row, column);
    }
}
