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
import javax.swing.table.*;

import lithium.*;
import lithium.Announcement.*;

import static lithium.Announcement.*;

/**
 * A table model containing the parameters of a single announcement preset.
 *
 * @since 0.9
 * @version 0.9 (2006.08.25)
 * @author Gerrit Meinders
 */
public class AnnouncementParameterTableModel extends AbstractTableModel
        implements TableModel, PropertyChangeListener {
    private static final Class<?>[] COLUMN_CLASSES = {
        String.class, Class.class, Parameter.class
    };

    /** The announcement preset on which the model is based. */
    private Announcement announcement;

    /** The announcement presets in the list. */
    private List<Parameter> parameters;

    /**
     * Constructs a new parameter list model for the given announcement preset.
     *
     * @param announcement the announcement preset
     */
    public AnnouncementParameterTableModel(Announcement announcement) {
        parameters = new ArrayList<Parameter>();
        setAnnouncement(announcement);
    }

    /**
     * Returns the parameter displayed by the specified row.
     *
     * @param rowIndex the index of the row
     * @return the parameter for the specified row
     */
    public Parameter getParameter(int rowIndex) {
        return parameters.get(rowIndex);
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
        this.announcement = announcement;
        parameters.clear();
        if (announcement != null) {
            parameters.addAll(announcement.getParameters());
            announcement.addPropertyChangeListener(this);
        }
        fireTableDataChanged();
    }

    public int getRowCount() {
        return parameters.size();
    }

    public int getColumnCount() {
        return COLUMN_CLASSES.length;
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return COLUMN_CLASSES[column];
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Parameter parameter = parameters.get(rowIndex);
        switch (columnIndex) {
        case 0:
            return parameter.getTag();
        case 1:
            return parameter.getClass();
        case 2:
            return parameter;
        default:
            throw new IllegalArgumentException("columnIndex");
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName() == PARAMETERS_PROPERTY) {
            setAnnouncement(announcement);
        }
    }
}
