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

import java.util.*;

/**
 * A group of lyrics that refers to the lyrics, such that numbers may overlap as
 * long as the lyrics themselves are unique and changes to the lyrics are not
 * considered to be changes to the group.
 *
 * @version 0.9 (2006.02.21)
 * @author Gerrit Meinders
 */
public class ReferenceGroup extends Group {
    /** The lyrics contained in this group. */
    private LinkedHashSet<Lyric> lyrics;

    /**
     * Constructs a new reference group with the given attributes.
     *
     * @param displayName the name of the resource that contains the group's
     *        display name format
     * @param name the name of the group
     * @param version the group's version
     */
    public ReferenceGroup(String displayName, String name, String version) {
        super(displayName, name, version);
        lyrics = new LinkedHashSet<Lyric>();
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
    public Lyric getLyric(int number) {
        for (Lyric lyric : lyrics) {
            if (lyric.getNumber() == number) {
                return lyric;
            }
        }
        return null;
    }

    /**
     * Returns all of the lyrics in this group. This does not include the lyrics
     * from sub-groups.
     *
     * @return the lyrics
     */
    @Override
    public Collection<Lyric> getLyrics() {
        return Collections.unmodifiableSet(lyrics);
    }

    /**
     * Adds the given lyric to the group by some implementation-specific means.
     *
     * @param lyric the lyric to be added
     * @return the lyric that was replaced, if any
     */
    @Override
    protected Lyric addLyricImpl(Lyric lyric) {
        if (lyric == null) {
            throw new NullPointerException("lyric");
        }
        lyrics.add(lyric);
        return null;
    }

    /**
     * Removes the given lyric from the group by some implementation-specific
     * means.
     *
     * @param lyric the lyric to be removed
     * @return whether the lyric existed in the group before removal
     */
    @Override
    protected boolean removeLyricImpl(Lyric lyric) {
        if (lyric == null) {
            throw new NullPointerException("lyric");
        }
        return lyrics.remove(lyric);
    }
}
