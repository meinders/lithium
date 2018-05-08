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

/**
 * A group of lyrics that contains the lyrics, such that their numbers must be
 * locally unique and changes to the lyric are reported by the group.
 *
 * @author Gerrit Meinders
 */
public class ContainerGroup extends Group implements PropertyChangeListener
{
	/** The lyrics contained in this group, by number. */
	private Map<Integer, Lyric> lyrics;

	/**
	 * Constructs a new container group with the given attributes.
	 *
	 * @param displayName the name of the resource that contains the group's
	 *            display name format
	 * @param name the name of the group
	 * @param version the group's version
	 */
	public ContainerGroup(String displayName, String name, String version)
	{
		super(displayName, name, version);
		lyrics = new LinkedHashMap<Integer, Lyric>();
	}

	/**
	 * Returns the lyric with the given number. If there are multiple lyrics,
	 * any of those may be returned, but the returned lyric should be consistent
	 * over multiple calls.
	 *
	 * @param number the lyric's number
	 * @return the lyric, or <code>null</code> if there is no lyric with the
	 *         given number
	 */
	@Override
	public Lyric getLyric(int number)
	{
		return lyrics.get(number);
	}

	/**
	 * Returns all of the lyrics in this group. This does not include the lyrics
	 * from sub-groups.
	 *
	 * @return the lyrics
	 */
	@Override
	public Collection<Lyric> getLyrics()
	{
		return Collections.unmodifiableCollection(lyrics.values());
	}

	/**
	 * Adds the given lyric to the group by some implementation-specific means.
	 *
	 * @param lyric the lyric to be added
	 * @return the lyric that was replaced, if any
	 */
	@Override
	protected Lyric addLyricImpl(Lyric lyric)
	{
		if (lyric == null)
		{
			throw new NullPointerException("lyric");
		}
		Lyric replaced = lyrics.put(lyric.getNumber(), lyric);
		if (replaced != lyric)
		{
			if (replaced != null)
			{
				replaced.removePropertyChangeListener(this);
			}
			lyric.addPropertyChangeListener(this);
		}
		return replaced;
	}

	/**
	 * Removes the given lyric from the group by some implementation-specific
	 * means.
	 *
	 * @param lyric the lyric to be removed
	 * @return whether the lyric existed in the group before removal
	 */
	@Override
	protected boolean removeLyricImpl(Lyric lyric)
	{
		if (lyric == null)
		{
			throw new NullPointerException("lyric");
		}
		boolean exists = getLyric(lyric.getNumber()) == lyric;
		if (exists)
		{
			lyric.removePropertyChangeListener(this);
			lyrics.remove(lyric.getNumber());
		}
		return exists;
	}

	/**
	 * Handles property changes from lyrics and sub-groups contained in the
	 * group.
	 *
	 * This provides automatic propagation of modifications upward in the group
	 * hierarchy. Propagation of 'unmodification' (e.g. loading or saving) is
	 * handled by {@link #setModified(boolean)}.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent e)
	{
		super.propertyChange(e);
		String property = e.getPropertyName();
		if (property == Lyric.MODIFIED_PROPERTY)
		{
			boolean modified = (Boolean) e.getNewValue();
			if (modified)
			{
				setModified(true);
			}
		}
	}
}
