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
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.tree.*;

import lithium.*;
import lithium.catalog.*;

/**
 * A table model showing lyrics from part of a catalog. By adding the model to
 * a tree of the bundles and groups in the catalog, the model can be
 * synchronized with the tree to display lyrics from the currently selected
 * bundle or group.
 *
 * @version 0.9 (2006.12.26)
 * @author Gerrit Meinders
 */
public class CatalogTableModel extends AbstractTableModel
        implements TreeSelectionListener, PropertyChangeListener {
    /** Serial version UID */
    private static final long serialVersionUID = 1L;

    private Catalog catalog;
    private Lyric[] lyrics = null;
    private Group group = null;

    private String[] columnNames;

    /**
     * Constructs a new table model based on the specified catalog.
     *
     * @param catalog catalog to base the model on
     */
    public CatalogTableModel(Catalog catalog) {
        super();
        this.catalog = catalog;
        columnNames = Resources.get().getStringArray(
                "catalogTableModel.columnNames");
    }

    public int getRowCount() {
        return lyrics == null ? 0 : lyrics.length;
    }

    public int getColumnCount() {
        /*
        if (group instanceof Bundle) {
            // bundles don't need the last ("bundle") column
            return columnNames.length - 1;
        } else if (group instanceof ReferenceGroup) {
        */
            return columnNames.length;
        /*
        } else {
            return 0;
        }
        */
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Class<?> getColumnClass(int column) {
        return getValueAt(0, column).getClass();
    }

    public Object getValueAt(int row, int column) {
        switch (column) {
        case 0:
            return lyrics[row].getNumber();
        case 1:
            return lyrics[row].getTitle();
        case 2:
	        Set<Group> groups = catalog.getGroups( lyrics[ row ] );
	        String result;
	        result = "";
	        boolean first = true;
	        for ( final Group group : groups )
	        {
		        if ( first )
		        {
			        first = false;
			        result = group.getName();
		        }
		        else
		        {
			        result += ", " + group.getName();
		        }
	        }
	        return result;
        default:
            throw new AssertionError("Invalid column: " + column);
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        Object[] pathNodes = e.getPath().getPath();
        TreeNode node = (TreeNode) pathNodes[pathNodes.length-1];

        // remove listeners from previous data source
        if (group != null) {
            group.removePropertyChangeListener(this);
        }

        boolean structureChange = false;

        Group oldSourceObject = group;
        group = CatalogTreeModel.getGroup(node);

        // update model content and add listeners to new data source
        if (group == null) {
            clear();

        } else {
            setLyrics(group.getLyrics());
            group.addPropertyChangeListener(this);

            structureChange = oldSourceObject == null ||
                    group.getClass() != oldSourceObject.getClass();
        }

        if (structureChange) {
            fireTableStructureChanged();
        } else {
            fireTableDataChanged();
        }
    }

    public void propertyChange(PropertyChangeEvent e) {
        String property = e.getPropertyName();
        if (e.getSource() instanceof Group) {
            Group group = (Group) e.getSource();
            if (property == Group.LYRICS_PROPERTY) {
                setLyrics(group.getLyrics());
            }
        }
    }

    private void clear() {
        lyrics = new Lyric[0];
        fireTableStructureChanged();
    }

    private void setLyrics(Collection<Lyric> lyrics) {
        Lyric[] lyricArray = lyrics.toArray(new Lyric[0]);
        Arrays.sort(lyricArray);
        this.lyrics = lyricArray;
        fireTableDataChanged();
    }

    /**
     * Returns the lyric for the specified table row.
     *
     * @param row table row index
     *
     * @return lyric for the given row
     */
    public Lyric getLyric(int row) {
        return lyrics[row];
    }
}
