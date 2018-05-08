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

public interface Book {
    /**
     * Returns the book's name.
     *
     * @return the name
     */
    public String getTitle();

    /**
     * Returns the set of all chapters in this book.
     *
     * @return the chapters
     */
    public Set<Chapter> getChapters();

    /**
     * Adds the given chapter to the book.
     *
     * @param chapter the chapter to be added
     */
    public void addChapter(Chapter chapter);

    /**
     * Returns the chapter with the given number.
     *
     * @param number the number
     * @return the chapter
     */
    @Deprecated
    public Chapter getChapter(int number);
}
