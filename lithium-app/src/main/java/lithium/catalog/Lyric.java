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

package lithium.catalog;

import java.beans.*;
import java.util.*;

import lithium.*;

/**
 * The lyrics of a single song with some additional information.
 *
 * @author Gerrit Meinders
 */
public abstract class Lyric implements Comparable<Lyric>
{
	/** The property indicating whether the lyric is modified. */
	public static final String MODIFIED_PROPERTY = "modified";

	/** Provides support for bounds properties. */
	protected PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	/** Indicates whether the lyric is modified. */
	private boolean modified;

	private int modificationCount = 0;

	/**
	 * Constructs a new lyric. Newly created lyrics are considered to be
	 * modified.
	 */
	public Lyric()
	{
		setModified(true);
	}

	/**
	 * Returns the number of the lyric.
	 *
	 * @return the number
	 */
	public abstract int getNumber();

	/**
	 * Sets the title of the lyric.
	 *
	 * @param title the title to be set
	 */
	public abstract void setTitle(String title);

	/**
	 * Returns the title of the lyric.
	 *
	 * @return the title
	 */
	public abstract String getTitle();

	/**
	 * Sets the text of the lyric.
	 *
	 * @param text the text to be set
	 */
	public abstract void setText(String text);

	/**
	 * Returns the text of the lyric.
	 *
	 * @return the text
	 */
	public abstract String getText();

	/**
	 * Sets the original title of the lyric.
	 *
	 * @param originalTitle the original title to be set, or {code null} to
	 *            unset the original title
	 */
	public abstract void setOriginalTitle(String originalTitle);

	/**
	 * Returns the original title of the lyric, if any. If the lyric's title is
	 * the original title, no original title should be set.
	 *
	 * @return the original title, or {@code null} if the lyric doesn't have an
	 *         associated original title
	 */
	public abstract String getOriginalTitle();

	/**
	 * Sets the copyrights of the lyric.
	 *
	 * @param copyrights the copyrights to be set
	 */
	public abstract void setCopyrights(String copyrights);

	/**
	 * Returns the copyrights of the lyric.
	 *
	 * @return the copyrights
	 */
	public abstract String getCopyrights();

	/**
	 * Sets the (musical) keys of the lyric, replacing the current set of keys.
	 *
	 * @param keys the keys to be set
	 */
	public abstract void setKeys(Set<String> keys);

	/**
	 * Returns the (musical) keys of the lyric.
	 *
	 * @return the keys
	 */
	public abstract Set<String> getKeys();

	/**
	 * Adds the given (musical) key to the set of keys associated with the
	 * lyric.
	 *
	 * @param key the key to be added
	 */
	public abstract void addKey(String key);

	/**
	 * Removes the given (musical) key to the set of keys associated with the
	 * lyric.
	 *
	 * @param key the key to be removed
	 */
	public abstract void removeKey(String key);

	/**
	 * Sets the bible references associated with the lyric, replacing the
	 * current set of references.
	 *
	 * @param bibleRefs the bible references to be set
	 */
	public abstract void setBibleRefs(Set<BibleRef> bibleRefs);

	/**
	 * Returns the bible references associated with the lyric.
	 *
	 * @return the bible references
	 */
	public abstract Set<BibleRef> getBibleRefs();

	/**
	 * Adds the given bible reference to the set of bible references associated
	 * with the lyric.
	 *
	 * @param bibleRef the bible reference to be added
	 */
	public abstract void addBibleRef(BibleRef bibleRef);

	/**
	 * Removes the given bible reference to the set of bible references
	 * associated with the lyric.
	 *
	 * @param bibleRef the bible reference to be removed
	 */
	public abstract void removeBibleRef(BibleRef bibleRef);

	/**
	 * Returns whether the lyric is modified.
	 *
	 * @return {@code true} if the lyric is modified; {@code false} otherwise
	 */
	public boolean isModified()
	{
		return modified;
	}

	/**
	 * Sets whether the lyric is modified.
	 *
	 * @param modified whether the lyric is modified
	 */
	public void setModified(boolean modified)
	{
		modificationCount++;
		boolean oldValue = this.modified;
		this.modified = modified;
		pcs.firePropertyChange(MODIFIED_PROPERTY, oldValue, modified);
	}

	/**
	 * Returns the number of modifications since the lyric was created. This
	 * value may be used to determine whether the lyric has changed between
	 * calls to this method.
	 *
	 * @return the number of modifications since the lyric was created
	 */
	public int getModificationCount()
	{
		return modificationCount;
	}

	/**
	 * Returns the localized string representation of the lyric (it's number and
	 * title) for the current default locale.
	 *
	 * @return the lyric's string representation
	 */
	@Override
	public String toString()
	{
		return Resources.get().getString("Lyric.displayName", getNumber(),
		        getTitle());
	}

	/**
	 * Compares this lyric with the specified lyric for order.
	 *
	 * @param l the lyric to be compared
	 * @return a negative integer, zero, or a positive integer as this object is
	 *         less than, equal to, or greater than the specified object
	 */
	public int compareTo(Lyric l)
	{
		int difference = getNumber() - l.getNumber();
		if (difference == 0)
		{
			// be consistent with equals
			return hashCode() - l.hashCode();
		}
		else
		{
			return difference;
		}
	}

	/**
	 * Add a PropertyChangeListener to the listener list.
	 *
	 * @param listener the PropertyChangeListener to be added
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.addPropertyChangeListener(listener);
	}

	/**
	 * Removes a PropertyChangeListener from the listener list.
	 *
	 * @param listener the PropertyChangeListener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		pcs.removePropertyChangeListener(listener);
	}

	/**
	 * Returns the property change support instance. This method is used when
	 * flexible access to bound properties support is needed.
	 *
	 * @return the property change support
	 */
	public PropertyChangeSupport getPropertyChangeSupport()
	{
		return pcs;
	}
}
