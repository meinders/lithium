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

package lithium;

import java.beans.*;
import java.util.*;

/**
 * A list of lyrics, of which one can be selected to be (dis)played.
 *
 * @author Gerrit Meinders
 */
public class Playlist implements Reorderable {
    /** Playlist items property */
    public static final String ITEMS_PROPERTY = "items";

    /** Modified property */
    public static final String MODIFIED_PROPERTY = "modified";

    /** Provides support for bounds properties. */
    protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /** The items on the playlist. */
    private ArrayList<PlaylistItem> items = new ArrayList<PlaylistItem>();

    /** Keeps track of the selected item. */
    private PlaylistSelectionModel selectionModel;

    /** Whether the playlist is modified. */
    private boolean modified;

    /**
     * Constructs a new empty playlist.
     */
    public Playlist() {
        setSelectionModel(new PlaylistSelectionModel());
    }

    /**
     * Sets whether the playlist is modified.
     *
     * @param modified {@code true} for a modified playlist; {@code false}
     *        otherwise
     */
    public void setModified(boolean modified) {
        boolean oldValue = this.modified;
        this.modified = modified;
        pcs.firePropertyChange(MODIFIED_PROPERTY, oldValue, modified);
    }

    public boolean isModified() {
        return modified;
    }

    public void setSelectionModel(PlaylistSelectionModel selectionModel) {
        assert selectionModel != null;
        this.selectionModel = selectionModel;
        selectionModel.setPlaylist(this);
    }

    public PlaylistSelectionModel getSelectionModel() {
        return selectionModel;
    }

    public void add(PlaylistItem item) {
        items.add(item);
        setModified(true);
        pcs.firePropertyChange(ITEMS_PROPERTY, null, null);
    }

    public void add(int position, PlaylistItem item) {
        items.add(position, item);
        setModified(true);
        pcs.firePropertyChange(ITEMS_PROPERTY, null, null);
    }

    public PlaylistItem remove(int index) {
        assert index >= 0 : index;
        assert index < items.size() : index;

        if (selectionModel.getSelectedIndex() >= index) {
            if (index > 0) {
                selectionModel.setSelectedIndex(selectionModel
                        .getSelectedIndex() - 1);
            } else {
                selectionModel.clearSelection();
            }
        }
        PlaylistItem removedItem = items.remove(index);
        if (removedItem != null) {
            setModified(true);
        }
        pcs.firePropertyChange(ITEMS_PROPERTY, null, null);
        return removedItem;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public int getLength() {
        return items.size();
    }

    public int getIndex(PlaylistItem item) {
        return items.indexOf(item);
    }

    /**
     * Returns the index of and occurance of the given item that is closest to
     * nearIndex.
     *
     * @param item the item to return the index of
     * @param nearIndex in the case of multiple occurances of the given item,
     *        the index closest to this index will be returned
     *
     * @return the found index, or -1 if item isn't on the playlist
     */
    public int getIndex(PlaylistItem item, int nearIndex) {
        assert nearIndex >= 0;
        assert nearIndex < items.size();

        for (int i = 0; i < items.size(); i++) {
            if (nearIndex + i < items.size()) {
                if (item.equals(items.get(nearIndex + i))) {
                    return nearIndex + i;
                }
            }
            if (nearIndex - i >= 0) {
                if (item.equals(items.get(nearIndex - i))) {
                    return nearIndex - i;
                }
            }
        }
        return -1;
    }

    public PlaylistItem getItem(int index) {
        assert index >= 0;
        assert index < items.size();
        return items.get(index);
    }

    public Collection<PlaylistItem> getItems() {
        return Collections.unmodifiableCollection(items);
    }

    public boolean contains(PlaylistItem item) {
        return items.contains(item);
    }

    public void swap(int index1, int index2) {
        assert index1 >= 0;
        assert index1 < items.size();
        assert index2 >= 0;
        assert index2 < items.size();
        PlaylistItem temp = items.get(index1);
        items.set(index1, items.get(index2));
        items.set(index2, temp);
        pcs.firePropertyChange(ITEMS_PROPERTY, null, null);
    }

    public void move(int from, int to) {
        assert from >= 0;
        assert from < items.size();
        assert to >= 0;
        assert to < items.size();

        if (from < to) {
            PlaylistItem temp = items.get(from);
            for (int i = from; i < to; i++) {
                items.set(i, items.get(i + 1));
            }
            items.set(to, temp);

        } else if (from > to) {
            PlaylistItem temp = items.get(from);
            for (int i = from; i > to; i--) {
                items.set(i, items.get(i - 1));
            }
            items.set(to, temp);

        } else {
            return;
        }

        pcs.firePropertyChange(ITEMS_PROPERTY, null, null);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(propertyName, listener);
    }

    public PropertyChangeListener[] getPropertyChangeListeners() {
        return pcs.getPropertyChangeListeners();
    }

    public PropertyChangeListener[] getPropertyChangeListeners(
            String propertyName) {
        return pcs.getPropertyChangeListeners(propertyName);
    }

    public boolean hasListeners(String propertyName) {
        return pcs.hasListeners(propertyName);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(propertyName, listener);
    }
}
