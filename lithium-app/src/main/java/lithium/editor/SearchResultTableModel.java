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

import java.text.*;
import java.util.*;
import javax.swing.table.*;

import lithium.*;
import lithium.search.*;

/**
 * A table model containing a list of search results.
 *
 * @version 0.9 (2005.10.21)
 * @author Gerrit Meinders
 */
public class SearchResultTableModel extends AbstractTableModel {
    private List<SearchResult> searchResults;

    public SearchResultTableModel(List<SearchResult> searchResults) {
        this.searchResults = new ArrayList<SearchResult>(searchResults);
    }

    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
        return searchResults.size();
    }

    public String getColumnName(int column) {
        switch (column) {
        case 0: return Resources.get().getString("searchResultDialog.bundle");
        case 1: return Resources.get().getString("searchResultDialog.number");
        case 2: return Resources.get().getString("searchResultDialog.title");
        case 3: return Resources.get().getString("SearchResultTableModel.relevance");
        default: throw new IndexOutOfBoundsException("column: " + column);
        }
    }

    public Class<?> getColumnClass(int column) {
        switch (column) {
        case 0: return String.class;
        case 1: return String.class;
        case 2: return String.class;
        case 3: return Percentage.class;
        default: throw new IndexOutOfBoundsException("column: " + column);
        }
    }

    public Object getValueAt(int row, int column) {
        switch (column) {
        case 0: return searchResults.get(row).getLyricRef().getBundle();
        case 1: return searchResults.get(row).getLyricRef().getNumber();
        case 2: return CatalogManager.getCatalog().getLyric(
                searchResults.get(row).getLyricRef()).getTitle();
        case 3: return new Percentage(searchResults.get(row).getRelevance());
        default: throw new IndexOutOfBoundsException("column: " + column);
        }
    }

    private class Percentage implements Comparable<Object> {
        private double percentage;

        public Percentage(double percentage) {
            this.percentage = percentage;
        }

        public int compareTo(Object o) {
            if (o instanceof Percentage) {
                Percentage p = (Percentage) o;
                return Double.compare(percentage, p.percentage);
            } else {
                throw new ClassCastException();
            }
        }

        public String toString() {
            return NumberFormat.getPercentInstance().format(percentage);
        }
    }
}
