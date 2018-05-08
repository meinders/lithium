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

/**
 * A model that maintains the selected item of a playlist.
 *
 * @version 0.9 (2005.10.23)
 * @author Gerrit Meinders
 */
public class PlaylistSelectionModel {
	public static final String SELECTED_ITEM_PROPERTY = "selectedItem";

	/** Provides support for bounds properties. */
	private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	private Playlist playlist;

	private int selectedIndex = -1;

	private PlaylistItem selectedItem = null;

	public PlaylistSelectionModel() {
		selectedIndex = -1;
	}

	public void setPlaylist(Playlist playlist) {
		assert playlist != null;
		this.playlist = playlist;
	}

	public int getSelectedIndex() {
		return selectedItem == null ? selectedIndex : -1;
	}

	public PlaylistItem getSelectedItem() {
		if (selectedItem == null) {
			if (selectedIndex == -1) {
				return null;
			} else {
				return playlist.getItem(selectedIndex);
			}
		} else {
			return selectedItem;
		}
	}

	public void clearSelection() {
		PlaylistItem oldValue = getSelectedItem();
		selectedIndex = -1;
		selectedItem = null;
		pcs.firePropertyChange(SELECTED_ITEM_PROPERTY, oldValue, null);
	}

	public void setSelectedIndex(int selectedIndex) {
		assert selectedIndex < playlist.getLength();
		assert selectedIndex >= -1;
		PlaylistItem oldValue = getSelectedItem();
		this.selectedIndex = selectedIndex;
		selectedItem = null;
		pcs.firePropertyChange(SELECTED_ITEM_PROPERTY, oldValue,
		        getSelectedItem());
	}

	public void setSelectedValue(Object value) {
		setSelectedItem(new PlaylistItem(value));
	}

	public void setSelectedItem(PlaylistItem selectedItem) {
		if (playlist.contains(selectedItem)) {
			int index;
			if (selectedIndex > -1) {
				index = playlist.getIndex(selectedItem, selectedIndex);
			} else {
				index = playlist.getIndex(selectedItem);
			}
			setSelectedIndex(index);
		} else {
			PlaylistItem oldValue = getSelectedItem();
			this.selectedItem = selectedItem;
			pcs.firePropertyChange(SELECTED_ITEM_PROPERTY, oldValue,
			        selectedItem);
		}
	}

	public boolean isFirstItemSelected() {
		return getSelectedIndex() == 0;
	}

	public boolean isLastItemSelected() {
		return getSelectedIndex() == playlist.getLength() - 1;
	}

	public boolean isPreviousSelectable() {
		if (selectedItem == null) {
			return selectedIndex > 0;
		} else {
			return !playlist.isEmpty();
		}
	}

	public boolean isNextSelectable() {
		if (selectedItem == null) {
			return selectedIndex < playlist.getLength() - 1;
		} else {
			return !playlist.isEmpty();
		}
	}

	public void selectFirst() {
		setSelectedIndex(0);
	}

	public void selectPrevious() {
		if (!isFirstItemSelected()) {
			if (selectedItem == null) {
				setSelectedIndex(selectedIndex - 1);
			} else {
				setSelectedIndex(selectedIndex);
			}
		}
	}

	public void selectNext() {
		if (!isLastItemSelected()) {
			if (selectedItem == null) {
				setSelectedIndex(selectedIndex + 1);
			} else {
				setSelectedIndex(selectedIndex);
			}
		}
	}

	public void selectLast() {
		setSelectedIndex(playlist.getLength() - 1);
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
