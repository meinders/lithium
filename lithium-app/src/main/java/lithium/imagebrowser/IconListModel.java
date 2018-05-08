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

package lithium.imagebrowser;

import java.util.*;
import javax.swing.*;

/**
 * A list model containing an icon for each value. Using this implementation
 * assigning different icons to values that are equal is impossible.
 *
 * @author Gerrit Meinders
 * @param <V> value type of the model
 */
public class IconListModel<V> extends AbstractListModel {
    private ArrayList<V> valueList;

    private HashMap<V, Icon> iconMap;

    private HashMap<V, String> descriptionMap;

    public IconListModel() {
        super();
        valueList = new ArrayList<V>();
        iconMap = new HashMap<V, Icon>();
        descriptionMap = new HashMap<V, String>();
    }

    public void add(V value, ImageIcon icon) {
        add(value, icon, icon.getDescription());
    }

    public void add(V value, Icon icon, String description) {
        valueList.add(value);
        iconMap.put(value, icon);
        descriptionMap.put(value, description);
        int addedIndex = valueList.indexOf(value);
        fireIntervalAdded(this, addedIndex, addedIndex);
    }

    public void set(V value, ImageIcon icon) {
        set(value, icon, icon.getDescription());
    }

    public void set(V value, Icon icon, String description) {
        iconMap.put(value, icon);
        descriptionMap.put(value, description);
        int changedIndex = valueList.indexOf(value);
        fireContentsChanged(this, changedIndex, changedIndex);
    }

    public void remove(V value) {
        int removedIndex = valueList.indexOf(value);
        valueList.remove(value);
        iconMap.remove(value);
        descriptionMap.remove(value);
        fireIntervalRemoved(this, removedIndex, removedIndex);
    }

    public void clear() {
        if (valueList.size() > 0) {
            int lastIndex = valueList.size() - 1;
            valueList.clear();
            iconMap.clear();
            descriptionMap.clear();
            fireIntervalRemoved(this, 0, lastIndex);
        }
    }

    public V getElementAt(int index) {
        return valueList.get(index);
    }

    public Icon getIcon(V value) {
        return iconMap.get(value);
    }

    public String getDescription(V value) {
        return descriptionMap.get(value);
    }

    public int getSize() {
        return valueList.size();
    }
}
