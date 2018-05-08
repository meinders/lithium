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

package lithium.books;

import java.util.*;

/**
 * A model of a chapter, the numbered entity containing verses.
 *
 * @author Gerrit Meinders
 */
public interface Chapter {
    /**
     * Returns the title of the chapter.
     *
     * @return The title of the chapter.
     */
    public String getTitle();

    /**
     * Returns the chapter's number.
     *
     * @return the number
     */
    @Deprecated
    public int getNumber();

    /**
     * Returns a sorted set of all verses in this chapter.
     *
     * @return the verses
     */
    public Collection<Verse> getVerses();

    /**
     * Adds the given verse to the chapter.
     *
     * @param verse the verse
     */
    public void addVerse(Verse verse);

    /**
     * Returns the verse that starts at the given number, if any.
     *
     * @param number The number where the verse starts.
     *
     * @return The verse, or <code>null</code>.
     */
    public Verse getVerse(int number);

}
